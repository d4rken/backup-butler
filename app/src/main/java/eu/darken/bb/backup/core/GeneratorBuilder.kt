package eu.darken.bb.backup.core

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorActivity
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@PerApp
class GeneratorBuilder @Inject constructor(
        @AppContext private val context: Context,
        private val generatorRepo: GeneratorRepo,
        private val editors: @JvmSuppressWildcards Map<Backup.Type, Generator.Editor.Factory<out Generator.Editor>>
) {

    private val hotData = HotData<Map<Generator.Id, Data>>(mutableMapOf())

    init {
        hotData.data
                .observeOn(Schedulers.computation())
                .subscribe { dataMap ->
                    dataMap.entries.forEach { (uuid, data) ->
                        if (data.type != null && data.editor == null) {
                            val editor = editors.getValue(data.type).create(uuid)
                            update(uuid) { it!!.copy(editor = editor) }.blockingGet()
                        }
                    }
                }
    }

    data class Data(
            val id: Generator.Id,
            val type: Backup.Type? = null,
            val editor: Generator.Editor? = null,
            val existing: Boolean = false
    )

    fun getSupportedBackupTypes(): Observable<Collection<Backup.Type>> = Observable.just(Backup.Type.values().toList())

    fun config(id: Generator.Id): Observable<Data> {
        return hotData.data
                .filter { it.containsKey(id) }
                .map { it[id] }
    }

    fun update(id: Generator.Id, action: (Data?) -> Data?): Single<Opt<Data>> = hotData
            .updateRx {
                val mutMap = it.toMutableMap()
                val old = mutMap.remove(id)
                val new = action.invoke(old)
                if (new != null) {
                    mutMap[new.id] = new
                }
                mutMap.toMap()
            }
            .map { Opt(it.newValue[id]) }

    fun remove(id: Generator.Id): Single<Opt<Data>> = Single.just(id)
            .doOnSubscribe { Timber.tag(TAG).d("Removing %s", id) }
            .flatMap { id ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }

    fun save(id: Generator.Id): Single<Generator.Config> = remove(id)
            .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Can't find ID to save: $id")
                it.notNullValue()
            }
            .flatMap {
                if (it.editor == null) throw IllegalStateException("Can't save builder data, NULL editor: $it")
                it.editor.save()
            }
            .flatMap { config ->
                return@flatMap generatorRepo.put(config).map { config }
            }
            .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
            .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
            .map { it }

    fun load(id: Generator.Id): Single<Data> = generatorRepo.configs
            .doOnSubscribe { Timber.tag(TAG).v("Loading %s", id) }
            .firstOrError()
            .map { Opt(it[id]) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Trying to load unknown spec: $id")
                return@map it.value
            }
            .flatMap { config ->
                val editor = editors.getValue(config.generatorType).create(config.generatorId, config)
                val builderData = Data(
                        id = config.generatorId,
                        type = config.generatorType,
                        editor = editor,
                        existing = true
                )
                return@flatMap update(id) { builderData }.map { builderData }
            }
            .doOnSuccess { Timber.tag(TAG).d("Loaded %s: %s", id, it) }
            .doOnError { Timber.tag(TAG).w(it, "Failed to load %s", id) }

    fun startEditor(configId: Generator.Id = Generator.Id()): Completable = load(configId)
            .onErrorResumeNext {
                Timber.tag(TAG).d("No existing spec for id %s, creating new builder.", configId)
                update(configId) { Data(id = configId) }.map { it.value!! }
            }
            .doOnSuccess { data ->
                Timber.tag(TAG).v("Starting editor for ID %s", configId)
                val intent = Intent(context, GeneratorEditorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putGeneratorId(data.id)
                context.startActivity(intent)

            }
            .ignoreElement()

    companion object {
        val TAG = App.logTag("Backup", "ConfigBuilder")
    }
}
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
        private val editors: @JvmSuppressWildcards Map<Generator.Type, Generator.Editor.Factory<out Generator.Editor>>
) {

    private val hotData = HotData<Map<Generator.Id, Data>>(mutableMapOf())
    val builders = hotData.data

    init {
        hotData.data
                .observeOn(Schedulers.computation())
                .subscribe { dataMap ->
                    dataMap.entries.forEach { (uuid, data) ->
                        if (data.generatorType != null && data.editor == null) {
                            val editor = editors.getValue(data.generatorType).create(uuid)
                            update(uuid) { it!!.copy(editor = editor) }.blockingGet()
                        }
                    }
                }
    }

    fun getSupportedBackupTypes(): Observable<Collection<Generator.Type>> = Observable.just(Generator.Type.values().toList())

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
                    mutMap[new.generatorId] = new
                }
                mutMap.toMap()
            }
            .map { Opt(it.newValue[id]) }
            .doOnSuccess { Timber.tag(TAG).v("Generator  updated: %s (%s): %s", id, action, it) }

    fun remove(id: Generator.Id): Single<Opt<Data>> = Single.just(id)
            .doOnSubscribe { Timber.tag(TAG).d("Removing %s", id) }
            .flatMap { id ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }
            .doOnSuccess { Timber.tag(TAG).v("Removed generator: %s", id) }

    fun save(id: Generator.Id): Single<Generator.Config> = remove(id)
            .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Can't find ID to save: $id")
                it.value
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

    fun load(id: Generator.Id): Single<Data> = generatorRepo.get(id)
            .map { optTask ->
                if (!optTask.isNull) optTask.value
                else throw IllegalArgumentException("Task not in repo: $id")
            }
            .flatMap { config ->
                val editor = editors.getValue(config.generatorType).create(config.generatorId)
                editor.load(config).blockingGet()
                val data = Data(
                        generatorId = config.generatorId,
                        generatorType = config.generatorType,
                        editor = editor
                )
                update(id) { data }.map { data }
            }

    fun startEditor(configId: Generator.Id = Generator.Id(), type: Generator.Type? = null): Completable = hotData.data.firstOrError()
            .map { builderData ->
                if (builderData.containsKey(configId)) builderData.getValue(configId)
                else throw IllegalArgumentException("Config builder not in data: $configId")
            }
            .onErrorResumeNext { load(configId) }
            .onErrorResumeNext {
                Timber.tag(TAG).d("No existing generator config for id %s, creating new dataset.", configId)
                update(configId) { Data(generatorId = configId, generatorType = type) }.map { it.value!! }
            }
            .doOnSuccess { data ->
                Timber.tag(TAG).v("Starting editor for ID %s", configId)
                val intent = Intent(context, GeneratorEditorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putGeneratorId(data.generatorId)
                context.startActivity(intent)
            }
            .ignoreElement()

    fun createBuilder(newId: Generator.Id = Generator.Id(), type: Generator.Type?): Single<Data> = Single.fromCallable {
        Data(
                generatorId = newId,
                generatorType = type,
                editor = editors[type]?.create(newId)
        )
    }

    data class Data(
            val generatorId: Generator.Id,
            val generatorType: Generator.Type? = null,
            val editor: Generator.Editor? = null
    )

    companion object {
        val TAG = App.logTag("Backup", "Generator", "Builder")
    }
}
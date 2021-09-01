package eu.darken.bb.backup.core

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorActivity
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorActivityArgs
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.task.core.Task
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@PerApp
class GeneratorBuilder @Inject constructor(
    @AppContext private val context: Context,
    private val generatorRepo: GeneratorRepo,
    private val editors: @JvmSuppressWildcards Map<Backup.Type, GeneratorEditor.Factory<out GeneratorEditor>>
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

    fun getSupportedBackupTypes(): Observable<Collection<Backup.Type>> = Observable.just(Backup.Type.values().toList())

    fun generator(id: Generator.Id): Observable<Data> {
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

    fun remove(id: Generator.Id, releaseResources: Boolean = true): Single<Opt<Data>> = Single.just(id)
        .doOnSubscribe { Timber.tag(TAG).d("Removing %s", id) }
        .flatMap {
            hotData.latest
                .flatMap { preDeleteMap ->
                    update(id) { null }.map { Opt(preDeleteMap[id]) }
                }
        }
        .doOnSuccess { Timber.tag(TAG).v("Removed generator: %s", id) }
        .map { optData ->
            if (releaseResources && optData.isNotNull) {
                optData.value?.editor?.release()?.blockingAwait()
            }
            return@map optData
        }

    fun save(id: Generator.Id): Single<Generator.Config> = remove(id, false)
        .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
        .map {
            checkNotNull(it.value) { "Can't find ID to save: $id" }
        }
        .flatMap {
            checkNotNull(it.editor) { "Can't save builder data, NULL editor: $it" }
            it.editor.save()
        }
        .flatMap { config ->
            return@flatMap generatorRepo.put(config).map { config }
        }
        .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
        .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
        .map { it }

    fun load(id: Generator.Id): Maybe<Data> = generatorRepo.get(id)
        .flatMapSingle { config ->
            val editor = editors.getValue(config.generatorType).create(config.generatorId)
            editor.load(config).blockingAwait()
            val data = Data(
                generatorId = config.generatorId,
                generatorType = config.generatorType,
                editor = editor
            )
            update(id) { data }.map { data }
        }
        .doOnSuccess { Timber.tag(TAG).d("Loaded %s: %s", id, it) }
        .doOnError { Timber.tag(TAG).e(it, "Failed to load %s", id) }

    fun startEditor(
        generatorId: Generator.Id = Generator.Id(),
        type: Backup.Type? = null,
        targetTask: Task.Id? = null
    ): Single<Generator.Id> = hotData.latest
        .flatMapMaybe { Maybe.fromCallable<Data> { it[generatorId] } }
        .switchIfEmpty(
            load(generatorId)
                .doOnSubscribe { Timber.tag(TAG).d("Trying existing generator for %s", generatorId) }
                .doOnSuccess { Timber.tag(TAG).d("Loaded existing generator for %s", generatorId) }
        )
        .switchIfEmpty(
            update(generatorId) {
                Data(
                    generatorId = generatorId,
                    generatorType = type,
                    targetTask = targetTask
                )
            }.map { it.value!! }
                .doOnSubscribe { Timber.tag(TAG).d("Creating new editor for %s", generatorId) }
        )
        .doOnSuccess { data ->
            Timber.tag(TAG).v("Starting editor for ID %s", generatorId)
            val intent = Intent(context, GeneratorEditorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtras(GeneratorEditorActivityArgs(generatorId = data.generatorId).toBundle())
            context.startActivity(intent)
        }
        .map { generatorId }

    data class Data(
        val generatorId: Generator.Id,
        val generatorType: Backup.Type? = null,
        val editor: GeneratorEditor? = null,
        val targetTask: Task.Id? = null
    )

    companion object {
        val TAG = App.logTag("Backup", "Generator", "Builder")
    }
}
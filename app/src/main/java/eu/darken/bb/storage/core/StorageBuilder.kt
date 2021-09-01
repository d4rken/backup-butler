package eu.darken.bb.storage.core

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.storage.ui.editor.StorageEditorActivity
import eu.darken.bb.storage.ui.editor.StorageEditorActivityArgs
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@PerApp
class StorageBuilder @Inject constructor(
    @AppContext private val context: Context,
    private val refRepo: StorageRefRepo,
    private val editors: @JvmSuppressWildcards Map<Storage.Type, StorageEditor.Factory<out StorageEditor>>
) {

    private val hotData = HotData<Map<Storage.Id, Data>>(mutableMapOf())
    val builders = hotData.data

    init {
        hotData.data
            .observeOn(Schedulers.computation())
            .subscribe { dataMap ->
                dataMap.entries.forEach { (uuid, data) ->
                    if (data.storageType != null && data.editor == null) {
                        val editor = editors.getValue(data.storageType).create(uuid)
                        update(uuid) { it!!.copy(editor = editor) }.blockingGet()
                    }
                }
            }
    }

    data class Data(
        val storageId: Storage.Id,
        val storageType: Storage.Type? = null,
        val editor: StorageEditor? = null
    )

    fun getSupportedStorageTypes(): Observable<Collection<Storage.Type>> =
        Observable.just(Storage.Type.values().toList())

    fun storage(id: Storage.Id): Observable<Data> {
        return hotData.data
            .filter { it.containsKey(id) }
            .map { it[id] }
    }

    fun update(id: Storage.Id, action: (Data?) -> Data?): Single<Opt<Data>> = hotData
        .updateRx {
            val mutMap = it.toMutableMap()
            val oldStorage = mutMap.remove(id)
            val newStorage = action.invoke(oldStorage)
            if (newStorage != null) {
                mutMap[newStorage.storageId] = newStorage
            }
            mutMap.toMap()
        }
        .map { Opt(it.newValue[id]) }
        .doOnSuccess { Timber.tag(TAG).v("Storage updated: %s (%s): %s", id, action, it) }

    fun remove(id: Storage.Id, isAbort: Boolean = true): Single<Opt<Data>> = Single.just(id)
        .doOnSubscribe { Timber.tag(TAG).d("Removing %s", id) }
        .flatMap {
            hotData.latest
                .flatMap { preDeleteMap ->
                    update(id) { null }.map { Opt(preDeleteMap[id]) }
                }
        }
        .doOnSuccess { Timber.tag(TAG).v("Removed storage: %s", id) }
        .map { optData ->
            if (isAbort && optData.isNotNull) {
                optData.value?.editor?.abort()?.blockingAwait()
            }
            return@map optData
        }

    fun save(id: Storage.Id): Single<Storage.Ref> = remove(id, false)
        .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
        .map {
            checkNotNull(it.value) { "Can't find ID to save: $id" }
        }
        .flatMap {
            checkNotNull(it.editor) { "Can't save builder data NULL editor: $it" }
            it.editor.save()
        }
        .flatMap { (ref, _) ->
            return@flatMap refRepo.put(ref).map { ref }
        }
        .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
        .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
        .map { it }

    fun load(id: Storage.Id): Maybe<Data> = refRepo.get(id)
        .flatMapSingle { ref: Storage.Ref ->
            val editor = editors.getValue(ref.storageType).create(ref.storageId)
            editor.load(ref).blockingGet()
            val builderData = Data(
                storageId = ref.storageId,
                storageType = ref.storageType,
                editor = editor
            )
            update(id) { builderData }.map { builderData }
        }
        .doOnSuccess { Timber.tag(TAG).d("Loaded %s: %s", id, it) }
        .doOnError { Timber.tag(TAG).e(it, "Failed to load %s", id) }

    fun startEditor(storageId: Storage.Id = Storage.Id()): Completable = hotData.latest
        .flatMapMaybe { Maybe.fromCallable<Data> { it[storageId] } }
        .switchIfEmpty(
            load(storageId)
                .doOnSubscribe { Timber.tag(TAG).d("Trying existing storage for %s", storageId) }
                .doOnSuccess { Timber.tag(TAG).d("Loaded existing storage for %s", storageId) }
        )
        .switchIfEmpty(
            update(storageId) { Data(storageId = storageId) }.map { it.value!! }
                .doOnSubscribe { Timber.tag(TAG).d("Creating new editor for %s", storageId) }
        )
        .doOnSuccess { data ->
            Timber.tag(TAG).d("Starting editor for ID %s", storageId)
            val intent = Intent(context, StorageEditorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val navArgs = StorageEditorActivityArgs(storageId = data.storageId)
            intent.putExtras(navArgs.toBundle())
            context.startActivity(intent)
        }
        .ignoreElement()


    companion object {
        val TAG = App.logTag("Storage", "Builder")
    }
}
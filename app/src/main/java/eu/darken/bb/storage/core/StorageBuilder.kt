package eu.darken.bb.storage.core

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.storage.ui.editor.StorageEditorActivity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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

    fun getSupportedStorageTypes(): Observable<Collection<Storage.Type>> = Observable.just(Storage.Type.values().toList())

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

    fun remove(id: Storage.Id): Single<Opt<Data>> = Single.just(id)
            .doOnSubscribe { Timber.tag(TAG).d("Removing %s", id) }
            .flatMap { storageId ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }
            .doOnSuccess { Timber.tag(TAG).v("Removed storage: %s", id) }

    fun save(id: Storage.Id): Single<Storage.Ref> = remove(id)
            .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Can't find ID to save: $id")
                it.value
            }
            .flatMap {
                if (it.editor == null) throw IllegalStateException("Can't save builder data NULL editor: $it")
                it.editor.save()
            }
            .flatMap { (ref, _) ->
                return@flatMap refRepo.put(ref).map { ref }
            }
            .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
            .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
            .map { it }

    fun load(id: Storage.Id): Single<Data> = refRepo.get(id)
            .map { optTask ->
                if (!optTask.isNull) optTask.value
                else throw IllegalArgumentException("Ref not in repo: $id")
            }
            .flatMap { ref ->
                val editor = editors.getValue(ref.storageType).create(ref.storageId)
                editor.load(ref).blockingGet()
                val builderData = Data(
                        storageId = ref.storageId,
                        storageType = ref.storageType,
                        editor = editor
                )
                return@flatMap update(id) { builderData }.map { builderData }
            }
            .doOnSuccess { Timber.tag(TAG).d("Loaded %s: %s", id, it) }
            .doOnError { Timber.tag(TAG).w(it, "Failed to load %s", id) }

    fun startEditor(storageId: Storage.Id = Storage.Id()): Completable = hotData.data.firstOrError()
            .map { builderData ->
                if (builderData.containsKey(storageId)) builderData.getValue(storageId)
                else throw IllegalArgumentException("StorageId not builder data: $storageId")
            }
            .onErrorResumeNext { load(storageId) }
            .onErrorResumeNext {
                Timber.tag(TAG).d("No existing ref for id %s, creating new builder.", storageId)
                update(storageId) { Data(storageId = storageId) }.map { it.value!! }
            }
            .doOnSuccess { data ->
                Timber.tag(TAG).v("Starting editor for ID %s", storageId)
                val intent = Intent(context, StorageEditorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putStorageId(data.storageId)
                context.startActivity(intent)
            }
            .ignoreElement()


    companion object {
        val TAG = App.logTag("Storage", "Builder")
    }
}
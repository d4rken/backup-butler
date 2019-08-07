package eu.darken.bb.storage.core

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.storage.ui.editor.StorageEditorActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@PerApp
class StorageBuilder @Inject constructor(
        @AppContext private val context: Context,
        private val refRepo: StorageRefRepo,
        private val editors: @JvmSuppressWildcards Map<BackupStorage.Type, StorageEditor.Factory<out StorageEditor>>
) {

    private val hotData = HotData<Map<BackupStorage.Id, Data>>(mutableMapOf())

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
            val storageId: BackupStorage.Id,
            val storageType: BackupStorage.Type? = null,
            val editor: StorageEditor? = null
    )

    fun getSupportedStorageTypes(): Observable<Collection<BackupStorage.Type>> = Observable.just(BackupStorage.Type.values().toList())

    fun storage(id: BackupStorage.Id): Observable<Data> {
        return hotData.data
                .filter { it.containsKey(id) }
                .map { it[id] }
    }

    fun update(id: BackupStorage.Id, action: (Data?) -> Data?): Single<Opt<Data>> = hotData
            .updateRx {
                val mutMap = it.toMutableMap()
                val oldStorage = mutMap.remove(id)
                val newStorage = action.invoke(oldStorage)
                if (newStorage != null) {
                    mutMap[newStorage.storageId] = newStorage
                }
                mutMap.toMap()
            }
            .map { Opt(it[id]) }

    fun remove(id: BackupStorage.Id): Single<Opt<Data>> = Single.just(id)
            .doOnSubscribe { Timber.tag(TAG).d("Removing %s", id) }
            .flatMap { storageId ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }

    fun save(id: BackupStorage.Id): Single<StorageRef> = remove(id)
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

    fun load(id: BackupStorage.Id): Single<Data> = refRepo.references
            .doOnSubscribe { Timber.tag(TAG).v("Loading %s", id) }
            .firstOrError()
            .map { Opt(it[id]) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Trying to load unknown storage: $id")
                return@map it.value
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

    fun startEditor(storageId: BackupStorage.Id = BackupStorage.Id()) {
        load(storageId)
                .onErrorResumeNext {
                    Timber.tag(TAG).d("No existing ref for id %s, creating new builder.", storageId)
                    update(storageId) { Data(storageId = storageId) }.map { it.value!! }
                }
                .subscribe { data ->
                    Timber.tag(TAG).v("Starting editor for ID %s", storageId)
                    val intent = Intent(context, StorageEditorActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putStorageId(data.storageId)
                    context.startActivity(intent)
                }
    }

    companion object {
        val TAG = App.logTag("Storage", "Builder")
    }
}
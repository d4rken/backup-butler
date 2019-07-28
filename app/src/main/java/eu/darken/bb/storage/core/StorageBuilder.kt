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
import java.util.*
import javax.inject.Inject

@PerApp
class StorageBuilder @Inject constructor(
        @AppContext private val context: Context,
        private val refRepo: StorageRefRepo
) {

    private val hotData = HotData<Map<UUID, Data>>(mutableMapOf())

    data class Data(
            val storageId: UUID,
            val storageType: BackupStorage.Type? = null,
            val ref: StorageRef? = null
    )

    fun getSupportedStorageTypes(): Observable<Collection<BackupStorage.Type>> = Observable.just(BackupStorage.Type.values().toList())

    fun storage(id: UUID, create: (() -> Data)? = null): Observable<Data> {
        var consumed = create == null
        return hotData.data
                .doOnNext {
                    if (!it.containsKey(id) && !consumed) {
                        consumed = true
                        update(id) { create!!.invoke() }.subscribeOn(Schedulers.io()).subscribe()
                    }
                }
                .flatMapSingle { data ->
                    return@flatMapSingle if (!data.containsKey(id) && !consumed) {
                        consumed = true
                        update(id) { create!!.invoke() }.map { data }
                    } else {
                        Single.just(data)
                    }
                }
                .filter { it.containsKey(id) }
                .map { it[id] }
    }

    fun update(id: UUID, action: (Data?) -> Data?): Single<Opt<Data>> = hotData
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

    fun remove(id: UUID): Single<Opt<Data>> = Single.just(id)
            .flatMap { id ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }

    fun save(id: UUID): Single<StorageRef> = remove(id)
            .map {
                if (it.isNull) throw IllegalArgumentException("Can't find ID to save: $id")
                it.value
            }
            .flatMap { data ->
                if (data.ref == null) throw IllegalArgumentException("Can't save, ref is missing: $data")
                return@flatMap refRepo.put(data.ref).map { data.ref }
            }

    fun load(id: UUID): Single<Data> = refRepo.references
            .firstOrError()
            .map { Opt(it[id]) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Trying to load unknown storage: $id")
                return@map it.value
            }
            .flatMap { ref ->
                val builderData = Data(
                        storageId = ref.storageId,
                        storageType = ref.storageType,
                        ref = ref
                )
                return@flatMap update(id) { builderData }.map { builderData }
            }


    fun startEditor(storageId: UUID = UUID.randomUUID()) {
        val intent = Intent(context, StorageEditorActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putStorageId(storageId)
        context.startActivity(intent)
    }

    companion object {
        val TAG = App.logTag("Storage", "Builder")
    }
}
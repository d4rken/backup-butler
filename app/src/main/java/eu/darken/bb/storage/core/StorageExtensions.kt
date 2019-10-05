package eu.darken.bb.storage.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backup.core.BackupSpec
import io.reactivex.Observable

object StorageExtensions {
    internal const val STORAGEID_KEY = "storage.id"
}


fun Intent.putStorageId(id: Storage.Id) = apply { putExtra(StorageExtensions.STORAGEID_KEY, id) }
fun Intent.getStorageId(): Storage.Id? = getParcelableExtra(StorageExtensions.STORAGEID_KEY) as Storage.Id?
fun Intent.putStorageIds(ids: Collection<Storage.Id>) = apply { putExtra(StorageExtensions.STORAGEID_KEY, ArrayList(ids)) }
fun Intent.getStorageIds(): Collection<Storage.Id>? = getParcelableArrayListExtra(StorageExtensions.STORAGEID_KEY)

fun Bundle.putStorageId(id: Storage.Id) = apply { putParcelable(StorageExtensions.STORAGEID_KEY, id) }
fun Bundle.getStorageId(): Storage.Id? = getParcelable(StorageExtensions.STORAGEID_KEY) as Storage.Id?
fun Bundle.putStorageIds(ids: Collection<Storage.Id>) = apply { putParcelableArrayList(StorageExtensions.STORAGEID_KEY, ArrayList(ids)) }
fun Bundle.getStorageIds(): Collection<Storage.Id>? = getParcelableArrayList(StorageExtensions.STORAGEID_KEY)

fun Storage.itemsOpt(vararg specIds: BackupSpec.Id): Observable<Collection<BackupSpec.InfoOpt>> = this.items(*specIds)
        .map { items ->
            specIds.map { id ->
                val item = items.find { it.specId == id }
                BackupSpec.InfoOpt(storageId = this.storageId, specId = id, info = item)
            }
        }
        .startWith(specIds.map { BackupSpec.InfoOpt(storageId = this.storageId, specId = it) })
        .map { it as Collection<BackupSpec.InfoOpt> }

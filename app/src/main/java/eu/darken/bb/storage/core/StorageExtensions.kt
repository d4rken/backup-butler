package eu.darken.bb.storage.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import kotlinx.coroutines.flow.*

object StorageExtensions {
    internal const val STORAGEID_KEY = "storage.id"
}


fun Intent.putStorageId(id: Storage.Id) = apply { putExtra(StorageExtensions.STORAGEID_KEY, id) }
fun Intent.getStorageId(): Storage.Id? = getParcelableExtra(StorageExtensions.STORAGEID_KEY) as Storage.Id?
fun Intent.putStorageIds(ids: Collection<Storage.Id>) =
    apply { putExtra(StorageExtensions.STORAGEID_KEY, ArrayList(ids)) }

fun Intent.getStorageIds(): Collection<Storage.Id>? = getParcelableArrayListExtra(StorageExtensions.STORAGEID_KEY)

fun Bundle.putStorageId(id: Storage.Id) = apply { putParcelable(StorageExtensions.STORAGEID_KEY, id) }
fun Bundle.getStorageId(): Storage.Id? = getParcelable(StorageExtensions.STORAGEID_KEY) as Storage.Id?

fun Bundle.putStorageIds(ids: Collection<Storage.Id>) =
    apply { putParcelableArrayList(StorageExtensions.STORAGEID_KEY, ArrayList(ids)) }

fun Bundle.getStorageIds(): Collection<Storage.Id>? = getParcelableArrayList(StorageExtensions.STORAGEID_KEY)

fun Storage.specInfosOpt(
    vararg specIds: BackupSpec.Id,
    live: Boolean = false
): Flow<Collection<BackupSpec.InfoOpt>> = flowOf(specIds)
    .flatMapConcat { items ->
        if (items.isEmpty()) return@flatMapConcat flowOf(emptyList<Backup.InfoOpt>())

        val statusObs = items.map { specId ->
            specInfo(specId)
                .let { if (live) it else it.take(1) }
                .map { BackupSpec.InfoOpt(it) }
                .catch {
                    BackupSpec.InfoOpt(storageId, specId, error = it)
                        .run { emit(this) }
                }
        }
        return@flatMapConcat combine(statusObs) { it.asList() }
    }
    .onStart {
        val opts = specIds.map {
            BackupSpec.InfoOpt(
                storageId = this@specInfosOpt.storageId,
                specId = it
            )
        }
        emit(opts)
    }
    .map { it as Collection<BackupSpec.InfoOpt> }


fun Storage.backupInfosOpt(
    vararg backupIds: Pair<BackupSpec.Id, Backup.Id>,
    live: Boolean = false
): Flow<Collection<Backup.InfoOpt>> = flowOf(backupIds)
    .flatMapConcat { items ->
        if (items.isEmpty()) return@flatMapConcat flowOf(emptyList<Backup.InfoOpt>())

        val statusObs = items.map { (specId, backupId) ->
            backupInfo(specId, backupId)
                .let { if (live) it else it.take(1) }
                .map { Backup.InfoOpt(it) }
                .catch {
                    Backup.InfoOpt(storageId, specId, backupId, error = it)
                        .run { emit(this) }
                }
        }
        return@flatMapConcat combine(statusObs) { it.asList() }
    }
    .onStart {
        val opts = (backupIds.map {
            Backup.InfoOpt(
                storageId = this@backupInfosOpt.storageId,
                specId = it.first,
                backupId = it.second
            )
        })
        emit(opts)
    }
    .map { it }

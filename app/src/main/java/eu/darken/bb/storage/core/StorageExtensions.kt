package eu.darken.bb.storage.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

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
): Observable<Collection<BackupSpec.InfoOpt>> = Observable.just(specIds)
    .flatMap { items ->
        if (items.isEmpty()) return@flatMap Observable.just(emptyList<Backup.InfoOpt>())

        val statusObs = items.map { specId ->
            specInfo(specId)
                .observeOn(Schedulers.computation())
                .compose { if (live) it else it.take(1) }
                .map { BackupSpec.InfoOpt(it) }
                .onErrorReturn { BackupSpec.InfoOpt(storageId, specId, error = it) }
        }
        return@flatMap Observable.combineLatest<BackupSpec.InfoOpt, Collection<BackupSpec.InfoOpt>>(statusObs) {
            it.asList() as Collection<BackupSpec.InfoOpt>
        }
    }
    .startWithItem(specIds.map { BackupSpec.InfoOpt(storageId = this.storageId, specId = it) })
    .map { it as Collection<BackupSpec.InfoOpt> }


fun Storage.backupInfosOpt(
    vararg backupIds: Pair<BackupSpec.Id, Backup.Id>,
    live: Boolean = false
): Observable<Collection<Backup.InfoOpt>> = Observable.just(backupIds)
    .flatMap { items ->
        if (items.isEmpty()) return@flatMap Observable.just(emptyList<Backup.InfoOpt>())

        val statusObs = items.map { (specId, backupId) ->
            backupInfo(specId, backupId)
                .observeOn(Schedulers.computation())
                .compose { if (live) it else it.take(1) }
                .map { Backup.InfoOpt(it) }
                .onErrorReturn { Backup.InfoOpt(storageId, specId, backupId, error = it) }
        }
        return@flatMap Observable.combineLatest<Backup.InfoOpt, Collection<Backup.InfoOpt>>(statusObs) {
            it.asList() as Collection<Backup.InfoOpt>
        }
    }
    .startWithItem(backupIds.map {
        Backup.InfoOpt(
            storageId = this.storageId,
            specId = it.first,
            backupId = it.second
        )
    })
    .map { it }
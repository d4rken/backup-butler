package eu.darken.bb.storage.core

import android.content.Context
import dagger.Reusable
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

@Reusable
class StorageManager @Inject constructor(
        @AppContext private val context: Context,
        private val refRepo: StorageRefRepo,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards BackupStorage.Factory>
) {

    private val repoCache = mutableMapOf<BackupStorage.Id, BackupStorage>()

    init {
        // TODO remove removed refs from cache
    }

    fun info(storageId: BackupStorage.Id): Observable<Opt<StorageInfo>> {
        return refRepo.references
                .flatMap { map ->
                    val ref = map[storageId]
                    if (ref == null) return@flatMap Observable.just(Opt(null))
                    else return@flatMap info(ref).map { Opt(it) }
                }
    }

    fun info(storageRef: StorageRef): Observable<StorageInfo> = getStorage(storageRef)
            .flatMapObservable { it.info() }
            .onErrorReturn { StorageInfo(ref = storageRef, error = it) }


    fun infos(): Observable<Collection<StorageInfo>> = refRepo.references
            .map { it.values }
            .flatMap { refs ->
                val statusObs = refs.map { info(it) }
                return@flatMap Observable.combineLatest<StorageInfo, List<StorageInfo>>(statusObs) {
                    return@combineLatest it.asList() as List<StorageInfo>
                }
            }

    private fun getStorage(ref: StorageRef): Single<BackupStorage> = Single.fromCallable {
        synchronized(repoCache) {
            var repo = repoCache[ref.storageId]
            if (repo != null) return@fromCallable repo

            val factory = storageFactories.find { it.isCompatible(ref) }
            if (factory == null) throw IllegalArgumentException("No factory compatible with $ref")
            repo = factory.create(ref)
            repoCache[ref.storageId] = repo
            return@fromCallable repo
        }
    }

}
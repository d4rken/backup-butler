package eu.darken.bb.storage.core

import android.content.Context
import android.content.Intent
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

@Reusable
class StorageManager @Inject constructor(
        @AppContext private val context: Context,
        private val refRepo: StorageRefRepo,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards Storage.Factory>
) {

    private val repoCache = mutableMapOf<Storage.Id, Storage>()

    init {
        // TODO remove removed refs from cache
    }

    fun info(id: Storage.Id): Observable<StorageInfo> = refRepo.get(id)
            .flatMapObservable { optRef -> info(optRef.notNullValue("No storage for id: $id")) }

    fun info(storageRef: Storage.Ref): Observable<StorageInfo> = getStorage(storageRef)
            .flatMap { it.info() }
            .doOnError { Timber.tag(TAG).e(it) }
            .onErrorReturn { StorageInfo(ref = storageRef, error = it) }

    fun infos(): Observable<Collection<StorageInfo>> = refRepo.references
            .map { it.values }
            .switchMap { refs ->
                return@switchMap if (refs.isEmpty()) {
                    Observable.just(emptyList())
                } else {
                    val statusObs = refs.map { info(it) }
                    Observable.combineLatest<StorageInfo, List<StorageInfo>>(statusObs) { it.asList() as List<StorageInfo> }
                }
            }

    fun getStorage(id: Storage.Id): Observable<Storage> = refRepo.get(id)
            .flatMapObservable { optRef -> getStorage(optRef.notNullValue("No storage for id: $id")) }

    fun detach(id: Storage.Id): Single<Storage.Ref> = Completable
            .fromCallable {
                synchronized(repoCache) {
                    val removed = repoCache.remove(id)
                    Timber.tag(TAG).d("Evicted from cache: %s", removed)
                }
            }
            .andThen(refRepo.remove(id))
            .map { it.notNullValue() }
            .doOnSubscribe { Timber.tag(TAG).i("Detaching %s", id) }

    fun wipe(id: Storage.Id): Single<Storage.Ref> = getStorage(id)
            .switchMapCompletable { it.wipe() }
            .andThen(detach(id))

    private fun getStorage(ref: Storage.Ref): Observable<Storage> = Observable.fromCallable {
        synchronized(repoCache) {
            var repo = repoCache[ref.storageId]
            if (repo != null) return@fromCallable repo

            val factory = storageFactories.find { it.isCompatible(ref) }
            if (factory == null) throw IllegalArgumentException("No factory compatible with $ref")
            repo = factory.create(ref, null)
            repoCache[ref.storageId] = repo
            return@fromCallable repo
        }
    }

    fun startViewer(storageId: Storage.Id) {
        val intent = Intent(context, StorageViewerActivity::class.java)
        intent.putStorageId(storageId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    companion object {
        private val TAG = App.logTag("Storage", "Manager")
    }
}
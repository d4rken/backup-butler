package eu.darken.bb.storage.core

import android.content.Context
import android.content.Intent
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.MissingFileException
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@Reusable
class StorageManager @Inject constructor(
        @AppContext private val context: Context,
        private val refRepo: StorageRefRepo,
        private val storageFactories: @JvmSuppressWildcards Map<Storage.Type, Storage.Factory<out Storage>>,
        private val storageEditors: @JvmSuppressWildcards Map<Storage.Type, StorageEditor.Factory<out StorageEditor>>
) {

    private val repoCache = mutableMapOf<Storage.Id, Storage>()

    init {
        // TODO remove removed refs from cache
    }

    fun info(id: Storage.Id): Observable<StorageInfo> = refRepo.get(id)
            .flatMapObservable { optRef -> info(optRef.notNullValue("No storage for id: $id")) }

    fun infos(): Observable<Collection<StorageInfo>> = refRepo.references
            .map { it.values }
            .switchMap { refs ->
                return@switchMap if (refs.isEmpty()) {
                    Observable.just(emptyList())
                } else {
                    val statusObs = refs.map {
                        // Parallel loading
                        info(it).subscribeOn(Schedulers.io())
                    }
                    Observable.combineLatest<StorageInfo, List<StorageInfo>>(statusObs) { it.asList() as List<StorageInfo> }
                }
            }

    fun info(storageRef: Storage.Ref): Observable<StorageInfo> = getStorage(storageRef)
            .switchMap {
                it.info().startWith(StorageInfo(ref = storageRef, config = it.storageConfig))
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .startWith(StorageInfo(ref = storageRef))
            .onErrorReturn { StorageInfo(ref = storageRef, error = it) }

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

            val storageFactory = storageFactories.getValue(ref.storageType)
            val storageEditor = storageEditors.getValue(ref.storageType).create(ref.storageId)
            val config = storageEditor.load(ref).blockingGet()
            if (config.isNull) throw MissingFileException(ref.path)

            repo = storageFactory.create(ref, config.notNullValue())

//            repoCache[ref.storageId] = repo
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
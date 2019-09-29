package eu.darken.bb.storage.core

import android.content.Context
import android.content.Intent
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.MissingFileException
import eu.darken.bb.storage.ui.list.StorageInfoOpt
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
        refRepo.modifiedIds.subscribeOn(Schedulers.io())
                .subscribe {
                    synchronized(repoCache) {
                        repoCache.remove(it)
                    }
                }
    }

    fun info(id: Storage.Id): Observable<StorageInfo> = refRepo.get(id)
            .flatMapObservable { optRef -> info(optRef.notNullValue("No storage for id: $id")) }

    fun infos(): Observable<Collection<StorageInfo>> = infos(wantedIds = null)
            .map { infos -> infos.map { info -> info.info!! } }

    fun infos(wantedIds: Collection<Storage.Id>? = null): Observable<Collection<StorageInfoOpt>> = Observable
            .fromCallable {
                if (wantedIds == null) return@fromCallable refRepo.references

                return@fromCallable Observable.just(wantedIds)
                        .flatMapIterable { x -> x }
                        .flatMapSingle { id ->
                            refRepo.get(id).map { optRef -> Pair(id, optRef.value) }
                        }
                        .toList()
                        .map { it.toMap() }
                        .toObservable()
            }
            .switchMap { it }
            .switchMap { refMap ->
                if (refMap.isEmpty()) return@switchMap Observable.just(emptyList<StorageInfoOpt>())

                val statusObs = refMap.map { (id, ref) ->
                    if (ref != null) {
                        info(ref)
                                .subscribeOn(Schedulers.io())
                                .map { StorageInfoOpt(id, it) }
                    } else {
                        Observable.just(StorageInfoOpt(id))
                    }
                }
                return@switchMap Observable.combineLatest<StorageInfoOpt, List<StorageInfoOpt>>(statusObs) {
                    it.asList() as List<StorageInfoOpt>
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
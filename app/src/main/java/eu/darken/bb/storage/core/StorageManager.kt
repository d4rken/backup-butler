package eu.darken.bb.storage.core

import android.content.Context
import android.content.Intent
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.rx.blockingGetUnWrapped
import eu.darken.bb.common.rx.onErrorMixLast
import eu.darken.bb.common.rx.singleOrError
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import eu.darken.bb.storage.ui.viewer.StorageViewerActivityArgs
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
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

    // TODO shouldn't this return StorageInfoOpt due to checking via Storage.Id
//    fun info(id: Storage.Id): Observable<Storage.InfoOpt> = refRepo.get(id)
//            .singleOrError(IllegalArgumentException("Can't find storage for $id"))
//            .flatMapObservable { info(it) }
//
//    fun infos(): Observable<Collection<Storage.InfoOpt>> = infos(wantedIds = null)
//            .map { infos -> infos.map { info -> info.info!! } }

    fun infos(wantedIds: Collection<Storage.Id>? = null): Observable<Collection<Storage.InfoOpt>> = Observable
            .fromCallable {
                if (wantedIds == null) return@fromCallable refRepo.references

                return@fromCallable Observable.just(wantedIds)
                        .flatMapIterable { x -> x }
                        .flatMapSingle { id ->
                            refRepo.get(id).map { Pair<Storage.Id, Storage.Ref?>(id, it) }
                                    .switchIfEmpty(Single.just(Pair<Storage.Id, Storage.Ref?>(id, null)))
                        }
                        .toList()
                        .map { it.toMap() }
                        .toObservable()
            }
            .switchMap { it }
            .switchMap { refMap ->
                if (refMap.isEmpty()) return@switchMap Observable.just(emptyList<Storage.InfoOpt>())

                val statusObs = refMap.map { (id, ref) ->
                    if (ref != null) {
                        info(ref)
                                .subscribeOn(Schedulers.io())
                                .map { Storage.InfoOpt(id, it) }
                    } else {
                        Observable.just(Storage.InfoOpt(id))
                    }
                }
                return@switchMap Observable.combineLatest<Storage.InfoOpt, List<Storage.InfoOpt>>(statusObs) {
                    @Suppress("UNCHECKED_CAST")
                    it.asList() as List<Storage.InfoOpt>
                }
            }

    private fun info(ref: Storage.Ref): Observable<Storage.Info> = getStorage(ref)
            .switchMap { it.info().startWithItem(Storage.Info(ref.storageId, ref.storageType, it.storageConfig)) }
            .doOnError { Timber.tag(TAG).e(it) }
            .startWithItem(Storage.Info(ref.storageId, ref.storageType))
            .onErrorMixLast { last, error ->
                // because we have startWith
                last!!.copy(error = error)
            }

    fun getStorage(id: Storage.Id): Observable<Storage> = refRepo.get(id)
            .singleOrError(IllegalArgumentException("Can't find storage for $id"))
            .flatMapObservable { getStorage(it) }

    private fun getStorage(ref: Storage.Ref): Observable<Storage> = Observable.fromCallable {
        synchronized(repoCache) {
            var repo = repoCache[ref.storageId]
            if (repo != null) return@fromCallable repo

            val storageFactory = storageFactories.getValue(ref.storageType)
            val storageEditor = storageEditors.getValue(ref.storageType).create(ref.storageId)

            val config = storageEditor.load(ref).blockingGetUnWrapped()
            repo = storageFactory.create(ref, config)

            repoCache[ref.storageId] = repo
            return@fromCallable repo
        }
    }

    fun detach(id: Storage.Id, wipe: Boolean = false): Single<Storage.Ref> = Single
            .fromCallable {
                val storage = try {
                    getStorage(id).blockingFirst()
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to get storage for detach.")
                    null
                }
                try {
                    storage?.detach(wipe)?.blockingAwait()
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to execute detach on storage.")
                }
                synchronized(repoCache) {
                    val removed = repoCache.remove(id)
                    Timber.tag(TAG).d("Evicted from cache: %s", removed)
                }
                val ref = refRepo.remove(id).blockingGet()
                ref.notNullValue()
            }
            .doOnSubscribe { Timber.tag(TAG).i("Detaching %s", id) }

    fun startViewer(storageId: Storage.Id): Completable = Completable.fromCallable {
        val intent = Intent(context, StorageViewerActivity::class.java)
        intent.putExtras(StorageViewerActivityArgs(storageId = storageId).toBundle())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    companion object {
        private val TAG = App.logTag("Storage", "Manager")
    }
}
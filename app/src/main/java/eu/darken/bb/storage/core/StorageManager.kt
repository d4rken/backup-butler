package eu.darken.bb.storage.core

import android.content.Context
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.INFO
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.onErrorMixLast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val refRepo: StorageRefRepo,
    private val storageFactories: @JvmSuppressWildcards Map<Storage.Type, Storage.Factory<out Storage>>,
    private val storageEditors: @JvmSuppressWildcards Map<Storage.Type, StorageEditor.Factory<out StorageEditor>>
) {

    private val lock = Mutex()
    private val repoCache = mutableMapOf<Storage.Id, Storage>()

    init {
        refRepo.modifiedIds
            .onEach {
                lock.withLock {
                    repoCache.remove(it)
                }
            }
            .launchIn(appScope)
    }

    // TODO shouldn't this return StorageInfoOpt due to checking via Storage.Id
//    fun info(id: Storage.Id): Observable<Storage.InfoOpt> = refRepo.get(id)
//            .singleOrError(IllegalArgumentException("Can't find storage for $id"))
//            .flatMapObservable { info(it) }
//
//    fun infos(): Observable<Collection<Storage.InfoOpt>> = infos(wantedIds = null)
//            .map { infos -> infos.map { info -> info.info!! } }

    fun infos(wantedIds: Collection<Storage.Id>? = null): Flow<Collection<Storage.InfoOpt>> = flow {
        if (wantedIds == null) {
            emit(refRepo.references)
            return@flow
        }


        val idRefs = wantedIds.map { id ->
            id to refRepo.get(id)
        }.toMap()

        emit(flowOf(idRefs))
    }
        .flatMapLatest { it }
        .flatMapLatest { refMap ->
            if (refMap.isEmpty()) return@flatMapLatest flowOf(emptyList<Storage.InfoOpt>())

            val statusObs = refMap.map { (id, ref) ->
                if (ref != null) {
                    info(ref).map { Storage.InfoOpt(id, it) }
                } else {
                    flowOf(Storage.InfoOpt(id))
                }
            }
            return@flatMapLatest combine(statusObs) { (it.asList()) }
        }

    private fun info(ref: Storage.Ref): Flow<Storage.Info> = flow { emit(getStorage(ref)) }
        .flatMapConcat {
            it.info().onStart {
                emit(Storage.Info(ref.storageId, ref.storageType, it.storageConfig))
            }
        }
        .catch {
            log(TAG, ERROR) { "Error while getting storage infor for $ref:\n${it.asLog()}" }
            throw it
        }
        .onStart { emit(Storage.Info(ref.storageId, ref.storageType)) }
        .onErrorMixLast { last, error ->
            // because we have startWith
            last!!.copy(error = error)
        }

    suspend fun getStorage(id: Storage.Id): Storage {
        val ref = refRepo.get(id) ?: throw IllegalArgumentException("Can't find storage for $id")

        return getStorage(ref)
    }

    private suspend fun getStorage(ref: Storage.Ref): Storage = lock.withLock {
        var repo = repoCache[ref.storageId]
        if (repo != null) return@withLock repo

        val storageFactory = storageFactories.getValue(ref.storageType)
        val storageEditor = storageEditors.getValue(ref.storageType).create(ref.storageId)

        val config = storageEditor.load(ref)
        repo = storageFactory.create(ref, config)

        repoCache[ref.storageId] = repo
        return@withLock repo
    }

    suspend fun detach(id: Storage.Id, wipe: Boolean = false): Storage.Ref = withContext(dispatcherProvider.IO) {
        log(TAG, INFO) { "Detaching $id" }
        val storage = try {
            getStorage(id)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get storage for detach.")
            null
        }

        try {
            storage?.detach(wipe)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to execute detach on storage.")
        }

        lock.withLock {
            val removed = repoCache.remove(id)
            Timber.tag(TAG).d("Evicted from cache: %s", removed)

            refRepo.remove(id)!!
        }
    }


    companion object {
        private val TAG = logTag("Storage", "Manager")
    }
}
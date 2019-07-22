package eu.darken.bb.repos.core

import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

@Reusable
class RepoManager @Inject constructor(
        private val refRepo: RepoRefRepo,
        @RepoFactory private val repoFactories: Set<@JvmSuppressWildcards BackupRepo.Factory>
) {

    private val repoCache = mutableMapOf<UUID, BackupRepo>()

    init {

    }

    fun status(repoRef: RepoRef): Observable<RepoStatus> = getRepo(repoRef)
            .flatMapObservable { it.status() }
            .map { RepoStatus(ref = repoRef, info = it) }
            .onErrorReturn { RepoStatus(ref = repoRef, info = null, error = it) }


    fun status(): Observable<Collection<RepoStatus>> = refRepo.references
            .map { it.values }
            .flatMap { refs ->
                val statusObs = refs.map { status(it) }
                return@flatMap Observable.combineLatest<RepoStatus, List<RepoStatus>>(statusObs) {
                    return@combineLatest it.asList() as List<RepoStatus>
                }
            }

    private fun getRepo(ref: RepoRef): Single<BackupRepo> = Single.fromCallable {
        synchronized(repoCache) {
            var repo = repoCache[ref.repoId]
            if (repo != null) return@fromCallable repo

            val factory = repoFactories.find { it.isCompatible(ref) }
            if (factory == null) throw IllegalArgumentException("No factory compatible with $ref")
            repo = factory.create(ref)
            repoCache[ref.repoId] = repo
            return@fromCallable repo
        }
    }
}
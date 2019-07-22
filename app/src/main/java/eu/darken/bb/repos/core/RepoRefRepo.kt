package eu.darken.bb.repos.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.AppComponent
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.opt
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AppComponent.Scope
class RepoRefRepo @Inject constructor(
        @AppContext context: Context,
        moshi: Moshi
) {
    private val preferences: SharedPreferences = context.getSharedPreferences("repo_references", Context.MODE_PRIVATE)
    private val repoRefAdapter = moshi.adapter(RepoRef::class.java)
    private val internalRefs = mutableMapOf<UUID, RepoRef>()
    private val refPublisher = BehaviorSubject.create<Map<UUID, RepoRef>>()

    val references: Observable<Map<UUID, RepoRef>> = refPublisher.hide()

    init {
        preferences.all.forEach {
            val ref = repoRefAdapter.fromJson(it.value as String)!!
            internalRefs[ref.repoId] = ref
        }
        refPublisher.onNext(internalRefs)
    }

    @Synchronized fun add(ref: RepoRef): Single<Opt<RepoRef>> = Single.fromCallable {
        val oldRef = internalRefs.put(ref.repoId, ref)
        Timber.d("add(ref=%s) -> old=%s", ref, oldRef)
        update()
        return@fromCallable oldRef.opt()
    }

    @Synchronized fun remove(refId: UUID): Single<Opt<RepoRef>> = Single.fromCallable {
        val old = internalRefs.remove(refId)
        Timber.d("remove(refId=%s) -> old=%s", refId, old)
        update()
        if (old == null) Timber.tag(TAG).w("Tried to delete non-existant RepoRef: %s", refId)
        return@fromCallable old.opt()
    }


    @Synchronized private fun update() {
        internalRefs.values.forEach {
            preferences.edit().putString("${it.repoId}", repoRefAdapter.toJson(it)).apply()
        }
        refPublisher.onNext(internalRefs)
    }

    companion object {
        val TAG = App.logTag("Repo", "RefRepo")
    }
}
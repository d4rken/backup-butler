package eu.darken.bb.storage.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.opt
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@PerApp
class StorageRefRepo @Inject constructor(
        @AppContext context: Context,
        moshi: Moshi
) {
    private val preferences: SharedPreferences = context.getSharedPreferences("repo_references", Context.MODE_PRIVATE)
    private val repoRefAdapter = moshi.adapter(StorageRef::class.java)
    private val internalRefs = mutableMapOf<UUID, StorageRef>()
    private val refPublisher = BehaviorSubject.create<Map<UUID, StorageRef>>()

    val references: Observable<Map<UUID, StorageRef>> = refPublisher.hide()

    init {
        preferences.all.forEach {
            val ref = repoRefAdapter.fromJson(it.value as String)!!
            internalRefs[ref.storageId] = ref
        }
        refPublisher.onNext(internalRefs)
    }

    @Synchronized fun add(ref: StorageRef): Single<Opt<StorageRef>> = Single.fromCallable {
        val oldRef = internalRefs.put(ref.storageId, ref)
        Timber.d("put(ref=%s) -> old=%s", ref, oldRef)
        update()
        return@fromCallable oldRef.opt()
    }

    @Synchronized fun remove(refId: UUID): Single<Opt<StorageRef>> = Single.fromCallable {
        val old = internalRefs.remove(refId)
        Timber.d("remove(refId=%s) -> old=%s", refId, old)
        update()
        if (old == null) Timber.tag(TAG).w("Tried to delete non-existant StorageRef: %s", refId)
        return@fromCallable old.opt()
    }


    @Synchronized private fun update() {
        internalRefs.values.forEach {
            preferences.edit().putString("${it.storageId}", repoRefAdapter.toJson(it)).apply()
        }
        refPublisher.onNext(internalRefs)
    }

    companion object {
        val TAG = App.logTag("Repo", "RefRepo")
    }
}
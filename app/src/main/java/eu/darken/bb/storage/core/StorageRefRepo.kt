package eu.darken.bb.storage.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.opt
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

@PerApp
class StorageRefRepo @Inject constructor(
        @AppContext context: Context,
        moshi: Moshi
) {
    private val preferences: SharedPreferences = context.getSharedPreferences("backup_storage_references", Context.MODE_PRIVATE)
    private val refAdapter = moshi.adapter(Storage.Ref::class.java)
    private val internalData = HotData<Map<Storage.Id, Storage.Ref>> {
        val internalRefs = mutableMapOf<Storage.Id, Storage.Ref>()
        preferences.all.forEach {
            val ref = refAdapter.fromJson(it.value as String)!!
            internalRefs[ref.storageId] = ref
        }
        internalRefs
    }
    private val affectedIdPub = PublishSubject.create<Storage.Id>()
    internal val modifiedIds = affectedIdPub.hide()

    val references = internalData.data

    init {
        internalData.data
                .subscribeOn(Schedulers.io())
                .subscribe { data ->
                    preferences.edit().clear().apply()
                    data.values.forEach {
                        preferences.edit().putString("${it.storageId}", refAdapter.toJson(it)).apply()
                    }
                }
    }

    fun get(id: Storage.Id): Single<Opt<Storage.Ref>> = internalData.data
            .firstOrError()
            .map { Opt(it[id]) }

    fun put(ref: Storage.Ref): Single<Opt<Storage.Ref>> {
        var oldValue: Storage.Ref? = null
        return internalData
                .updateRx { data ->
                    data.toMutableMap().apply {
                        oldValue = put(ref.storageId, ref)
                    }
                }
                .map { oldValue.opt() }
                .doOnSuccess { Timber.d("put(ref=%s) -> old=%s", ref, it.value) }
                .doOnSuccess { affectedIdPub.onNext(ref.storageId) }
    }

    fun remove(refId: Storage.Id): Single<Opt<Storage.Ref>> {
        var oldValue: Storage.Ref? = null
        return internalData
                .updateRx { data ->
                    data.toMutableMap().apply {
                        oldValue = remove(refId)
                    }
                }
                .map { oldValue.opt() }
                .doOnSuccess {
                    Timber.d("remove(refId=%s) -> old=%s", refId, it.value)
                    if (it.isNull) Timber.tag(TAG).w("Tried to delete non-existant StorageRef: %s", refId)
                }
                .doOnSuccess { affectedIdPub.onNext(refId) }
    }

    companion object {
        val TAG = App.logTag("Storage", "RefRepo")
    }
}
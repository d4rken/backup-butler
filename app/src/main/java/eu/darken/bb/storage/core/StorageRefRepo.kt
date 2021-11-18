package eu.darken.bb.storage.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.collections.mutate
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRefRepo @Inject constructor(
    @ApplicationContext context: Context,
    moshi: Moshi,
    @AppScope private val appScope: CoroutineScope,
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("backup_storage_references", Context.MODE_PRIVATE)
    private val refAdapter = moshi.adapter(Storage.Ref::class.java)

    private val internalData = DynamicStateFlow(TAG, appScope) {
        val internalRefs = mutableMapOf<Storage.Id, Storage.Ref>()
        preferences.all.forEach {
            val ref = refAdapter.fromJson(it.value as String)!!
            internalRefs[ref.storageId] = ref
        }
        internalRefs.toMap()
    }

    private val affectedIdPub = MutableStateFlow<Storage.Id?>(null)
    internal val modifiedIds: Flow<Storage.Id> = affectedIdPub.filterNotNull()

    val references = internalData.flow

    init {
        internalData.flow
            .onStart { log(TAG, VERBOSE) { "Monitoring refs for async storage" } }
            .onEach { data ->
                preferences.edit().clear().apply()
                data.values.forEach {
                    preferences.edit().putString("${it.storageId}", refAdapter.toJson(it)).apply()
                }
            }
            .launchIn(appScope)
    }

    suspend fun get(id: Storage.Id): Storage.Ref? = internalData.value()[id]

    suspend fun put(ref: Storage.Ref): Storage.Ref? {
        var oldValue: Storage.Ref? = null

        internalData.updateBlocking {
            oldValue = this[ref.storageId]
            mutate {
                put(ref.storageId, ref)?.also {
                    log(TAG) { "Overwriting existing ref: $it" }
                }
            }
        }

        affectedIdPub.value = ref.storageId
        log(TAG, VERBOSE) { "put(ref=$ref) -> old=$oldValue" }

        return oldValue
    }

    suspend fun remove(refId: Storage.Id): Storage.Ref? {
        var oldValue: Storage.Ref? = null

        internalData.updateBlocking {
            oldValue = this[refId]
            mutate {
                remove(refId)
            }
        }

        log(TAG, VERBOSE) { "remove(refId=$refId) -> old=$oldValue" }

        if (oldValue == null) log(TAG, WARN) { "Tried to delete non-existant StorageRef: $refId" }

        affectedIdPub.value = refId

        return oldValue
    }

    companion object {
        val TAG = logTag("Storage", "RefRepo")
    }
}
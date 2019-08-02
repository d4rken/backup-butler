package eu.darken.bb.backups.core

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.backups.ui.editor.BackupEditorActivity
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@PerApp
class BackupBuilder @Inject constructor(
        @AppContext private val context: Context,
        private val configRepo: BackupConfigRepo,
        private val editors: @JvmSuppressWildcards Map<Backup.Type, BackupConfigEditor.Factory<out BackupConfigEditor>>
) {

    private val hotData = HotData<Map<UUID, Data>>(mutableMapOf())

    init {

    }

    data class Data(
            val id: UUID,
            val type: Backup.Type? = null,
            val editor: BackupConfigEditor? = null,
            val existing: Boolean = false
    )

    fun getSupportedBackupTypes(): Observable<Collection<Backup.Type>> = Observable.just(Backup.Type.values().toList())

    fun config(id: UUID): Observable<Data> {
        return hotData.data
                .filter { it.containsKey(id) }
                .map { it[id] }
    }

    fun update(id: UUID, action: (Data?) -> Data?): Single<Opt<Data>> = hotData
            .updateRx {
                val mutMap = it.toMutableMap()
                val old = mutMap.remove(id)
                val new = action.invoke(old)
                if (new != null) {
                    mutMap[new.id] = new
                }
                mutMap.toMap()
            }
            .map { Opt(it[id]) }

    fun remove(id: UUID): Single<Opt<Data>> = Single.just(id)
            .doOnSubscribe { Timber.tag(TAG).d("Removing %s", id) }
            .flatMap { id ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }

    fun save(id: UUID): Single<BackupConfig> = remove(id)
            .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Can't find ID to save: $id")
                it.notNullValue()
            }
            .flatMap {
                if (it.editor == null) throw IllegalStateException("Can't save builder data, NULL editor: $it")
                it.editor.save()
            }
            .flatMap { config ->
                return@flatMap configRepo.put(config).map { config }
            }
            .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
            .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
            .map { it }

    fun load(id: UUID): Single<Data> = configRepo.configs
            .doOnSubscribe { Timber.tag(TAG).v("Loading %s", id) }
            .firstOrError()
            .map { Opt(it[id]) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Trying to load unknown config: $id")
                return@map it.value
            }
            .flatMap { config ->
                val editor = editors.getValue(config.configType).create(config.configId)
                editor.load(config).blockingGet()
                val builderData = Data(
                        id = config.configId,
                        type = config.configType,
                        editor = editor,
                        existing = true
                )
                return@flatMap update(id) { builderData }.map { builderData }
            }
            .doOnSuccess { Timber.tag(TAG).d("Loaded %s: %s", id, it) }
            .doOnError { Timber.tag(TAG).w(it, "Failed to load %s", id) }

    fun startEditor(configId: UUID = UUID.randomUUID()) {
        load(configId)
                .onErrorResumeNext {
                    Timber.tag(TAG).d("No existing config for id %s, creating new builder.", configId)
                    update(configId) { Data(id = configId) }.map { it.value!! }
                }
                .subscribe { data ->
                    Timber.tag(TAG).v("Starting editor for ID %s", configId)
                    val intent = Intent(context, BackupEditorActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putConfigId(data.id)
                    context.startActivity(intent)
                }
    }

    companion object {
        val TAG = App.logTag("Backup", "ConfigBuilder")
    }
}
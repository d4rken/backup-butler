package eu.darken.bb.backups.core.app

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backups.core.BackupConfig
import eu.darken.bb.backups.core.BackupConfigEditor
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

class AppBackupConfigEditor @AssistedInject constructor(
        moshi: Moshi,
        @Assisted private val configId: UUID
) : BackupConfigEditor {

    override fun load(config: BackupConfig): Completable {
        TODO("not implemented")
    }

    override fun save(): Single<BackupConfig> {
        TODO("not implemented")
    }

    @AssistedInject.Factory
    interface Factory : BackupConfigEditor.Factory<AppBackupConfigEditor>
}
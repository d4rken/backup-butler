package eu.darken.bb.settings.ui.support

import android.content.Intent
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.EmailTool
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.InstallId
import eu.darken.bb.common.smart.SmartVDC
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SupportFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    val installId: InstallId,
    val backupButler: BackupButler,
    val emailTool: EmailTool
) : SmartVDC() {

    val emailEvent = SingleLiveEvent<Intent>()
    val clipboardEvent = SingleLiveEvent<String>()

    fun sendSupportMail() {
        Observable
            .fromCallable {
                val bodyInfo = StringBuilder("\n\n\n")

                bodyInfo.append("--- Infos for the developer ---\n")

                val appInfo = backupButler.appInfo
                val versionStr = "${appInfo.versionName} (${appInfo.versionCode}) [${appInfo.gitSha}]"
                bodyInfo.append("App version: ").append(versionStr).append("\n")

                bodyInfo.append("Update history: ").append(backupButler.updateHistory).append("\n")
                bodyInfo.append("Device: ").append(Build.FINGERPRINT).append("\n")
                bodyInfo.append("Install ID: ").append(installId.installId.toString()).append("\n")

                val email = EmailTool.Email(
                    receipients = listOf("support@darken.eu"),
                    subject = "[Backup Butler] Question/Suggestion/Request\n",
                    body = bodyInfo.toString()
                )
                return@fromCallable emailTool.build(email)
            }
            .subscribeOn(Schedulers.io())
            .subscribe { emailEvent.postValue(it) }
    }

    fun copyInstallID() {
        clipboardEvent.postValue(installId.installId.toString())
    }
}
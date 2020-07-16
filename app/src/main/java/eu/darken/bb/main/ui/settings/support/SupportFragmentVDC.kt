package eu.darken.bb.main.ui.settings.support

import android.content.Intent
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.BackupButler
import eu.darken.bb.common.EmailTool
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.InstallId
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class SupportFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
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
                            subject = "[SD Maid] Question/Suggestion/Request\n",
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

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<SupportFragmentVDC>

}
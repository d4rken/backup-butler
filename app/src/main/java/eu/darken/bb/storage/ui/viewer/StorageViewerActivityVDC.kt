package eu.darken.bb.storage.ui.viewer

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.ifFresh
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.content.ItemContentsFragment
import eu.darken.bb.storage.ui.viewer.item.StorageItemFragment
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import kotlin.reflect.KClass


class StorageViewerActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        storageManager: StorageManager
) : SmartVDC() {

    val pageEvent = SingleLiveEvent<PageData>()
    val errorEvent = SingleLiveEvent<Throwable>()
    val finishActivity = SingleLiveEvent<Boolean>()

    private val stater: Stater<State> = Stater {
        handle.ifFresh {
            pageEvent.postValue(PageData(page = PageData.Page.CONTENT, storageId = storageId))
        }
        State(storageId = storageId)
    }
    val state = stater.liveData

    init {
        storageManager.infos(listOf(storageId)).subscribeOn(Schedulers.io())
                .map { it.single() }
                .subscribe { optInfo ->
                    stater.update {
                        it.copy(
                                storageId = storageId,
                                storageType = optInfo.info?.storageType,
                                label = optInfo.info?.config?.label ?: ""
                        )
                    }
                    if (optInfo.anyError != null) {
                        errorEvent.postValue(optInfo.anyError)
                        finishActivity.postValue(true)
                    }
                }
                .withScopeVDC(this)
    }

    fun goTo(pageData: PageData) {
        pageEvent.postValue(pageData)
    }

    data class State(
            val storageId: Storage.Id,
            val label: String = "",
            val storageType: Storage.Type? = null,
            val loading: Boolean = true
    )

    @Keep @Parcelize
    data class PageData(val page: Page, val storageId: Storage.Id, val backupSpecId: BackupSpec.Id? = null) : Parcelable {
        enum class Page(val fragmentClass: KClass<out Fragment>) {
            CONTENT(StorageItemFragment::class),
            DETAILS(ItemContentsFragment::class)
        }
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageViewerActivityVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageViewerActivityVDC
    }
}
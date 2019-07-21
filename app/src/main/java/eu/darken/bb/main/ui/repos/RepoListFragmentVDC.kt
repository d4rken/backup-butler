package eu.darken.bb.main.ui.repos

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.repos.RepoInfo
import eu.darken.bb.backup.repos.RepoManager
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.main.core.service.BackupService
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables

class RepoListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val repoManager: RepoManager,
        @AppContext private val context: Context
) : SmartVDC() {

    val viewState: LiveData<ViewState> = Observables
            .zip(repoManager.infos(), Observable.just(""))
            .map { (repos, upgradeData) ->
                return@map ViewState(
                        repos = repos.toList()
                )
            }
            .toLiveData()

    init {

    }

    fun test() {
        context.startService(Intent(context, BackupService::class.java))
    }

    data class ViewState(
            val repos: List<RepoInfo>
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<RepoListFragmentVDC>
}
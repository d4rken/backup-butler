package eu.darken.bb.tasks.ui.tasklist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.tasks.core.BackupTask
import eu.darken.bb.tasks.core.BackupTaskRepo
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables

class TaskListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val taskRepo: BackupTaskRepo,
        @AppContext private val context: Context
) : SmartVDC() {

    val viewState: LiveData<ViewState> = Observables
            .zip(taskRepo.tasks.map { it.values }, Observable.just(""))
            .map { (repos, upgradeData) ->
                return@map ViewState(
                        repos = repos.toList()
                )
            }
            .toLiveData()
    val newTaskEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    init {

    }

    fun newTask() {
        newTaskEvent.postValue(true)
    }

    data class ViewState(
            val repos: List<BackupTask>
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<TaskListFragmentVDC>
}
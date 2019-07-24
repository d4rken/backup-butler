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
import eu.darken.bb.tasks.core.TaskBuilder
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import timber.log.Timber
import java.util.*

class TaskListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val taskRepo: BackupTaskRepo,
        private val taskBuilder: TaskBuilder,
        @AppContext private val context: Context
) : SmartVDC() {

    val viewState: LiveData<ViewState> = Observables
            .combineLatest(taskRepo.tasks.map { it.values }, Observable.just(""))
            .map { (repos, upgradeData) ->
                return@map ViewState(
                        repos = repos.toList()
                )
            }
            .toLiveData()
    val editTaskEvent = SingleLiveEvent<EditActions>()

    init {

    }

    fun newTask() {
        taskBuilder.startEditor()
    }

    fun editTask(item: BackupTask) {
        Timber.tag(TAG).d("editTask(%s)", item)
        editTaskEvent.postValue(EditActions(
                taskId = item.taskId,
                allowEdit = true,
                allowDelete = true
        ))
    }

    data class ViewState(
            val repos: List<BackupTask>
    )

    data class EditActions(
            val taskId: UUID,
            val allowEdit: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<TaskListFragmentVDC>
}
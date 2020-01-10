package eu.darken.bb.task.core.common.requirements

import android.content.Context
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.files.core.DeviceEnvironment
import eu.darken.bb.task.core.Task
import io.reactivex.Single
import javax.inject.Inject

@PerApp
class RequirementsManager @Inject constructor(
        @AppContext private val context: Context,
        private val deviceEnvironment: DeviceEnvironment
) {

    fun reqsFor(taskType: Task.Type, taskId: Task.Id? = null): Single<List<Requirement>> = Single.fromCallable {

        listOf(
                PermissionRequirement.createStorageReq(context)
        )
    }

}
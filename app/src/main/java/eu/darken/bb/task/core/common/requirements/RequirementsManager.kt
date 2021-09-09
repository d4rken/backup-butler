package eu.darken.bb.task.core.common.requirements

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.files.core.DeviceEnvironment
import eu.darken.bb.task.core.Task
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequirementsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceEnvironment: DeviceEnvironment
) {

    fun reqsFor(taskType: Task.Type, taskId: Task.Id? = null): Single<List<Requirement>> = Single.fromCallable {

        listOf(
            PermissionRequirement.createStorageReq(context)
        )
    }

}
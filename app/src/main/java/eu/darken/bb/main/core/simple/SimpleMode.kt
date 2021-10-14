package eu.darken.bb.main.core.simple

import eu.darken.bb.common.HotData
import eu.darken.bb.task.core.Task
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleMode @Inject constructor() {

    private val appsDataInternal = HotData { AppsTaskData() }
    val appsData: Observable<AppsTaskData> = appsDataInternal.data

    data class AppsTaskData(
        val taskId: Task.Id? = null,
    )

    private val filesDataInternal = HotData { FilesTaskData() }
    val filesData: Observable<FilesTaskData> = filesDataInternal.data

    data class FilesTaskData(
        val taskId: Task.Id? = null,
    )

}
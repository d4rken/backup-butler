package eu.darken.bb.common.rx

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

object SchedulersCustom {

    fun customScheduler(poolSize: Int, poolName: String? = null): Scheduler {
        val executor = if (poolName != null) {
            Executors.newFixedThreadPool(poolSize, NamedThreadFactory(poolName))
        } else {
            Executors.newFixedThreadPool(poolSize)
        }
        return Schedulers.from(executor)
    }

}
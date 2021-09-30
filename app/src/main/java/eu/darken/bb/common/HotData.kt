package eu.darken.bb.common

import android.annotation.SuppressLint
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.rx.SchedulersCustom
import eu.darken.bb.common.rx.filterEqual
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject
import java.util.*

/**
 * Threadsafe data/state updater
 */
@SuppressLint("CheckResult")
open class HotData<T : Any>(
    private val debug: Boolean = true,
    private val tag: String? = null,
    private val scheduler: Scheduler = createDefaultScheduler(tag),
    private val initial: () -> T
) {

    private val ll = tag?.let { "$it:HotData" } ?: logTag("HotData")
    private val valueLock = Any()
    private val updatePub = PublishSubject.create<UpdateAction<T>>().toSerialized()
    private val statePub = BehaviorSubject.create<State<T>>().toSerialized()

    @Volatile private var currentState: State<T>? = null

    private val actionIdSet = mutableSetOf<UUID>()

    init {
        updatePub
            .observeOn(scheduler)
            .subscribe({ action ->
                if (debug) log(ll, VERBOSE) { "Will update with $action" }
                synchronized(valueLock) {
                    if (currentState == null) tryInitValue()
                    val oldState = currentState!!
                    if (debug) log(ll, VERBOSE) { "Updating $oldState with $action" }

                    if (debug) {
                        require(!actionIdSet.contains(oldState.actionId))
                        actionIdSet.add(oldState.actionId)
                    }

                    val newState = try {
                        action.modify(oldState.data)
                            ?.let { State(data = it, actionId = action.id) }
                            ?: oldState.copy(actionId = action.id)
                    } catch (e: Exception) {
                        log(ll, WARN) { "UpdateAction $action failed: ${e.asLog()}" }
                        action.errorHandler(e)
                        oldState
                    }

                    if (debug) log(ll) { "Updated $oldState -> $newState" }

                    currentState = newState
                    statePub.onNext(newState)
                }
            }, {
                log(ll, ERROR) { "Error while updating value: ${it.asLog()}" }
                statePub.onError(it)
            })
    }

    private fun tryInitValue() = synchronized(valueLock) {
        if (currentState != null) return@synchronized
        if (debug) log(ll, VERBOSE) { "Providing initial value..." }
        try {
            State(actionId = UUID.randomUUID(), data = initial()).also {
                if (debug) log(ll, VERBOSE) { "Initial value: $it" }
                currentState = it
                statePub.onNext(it)
            }
        } catch (e: Exception) {
            log(ll, ERROR) { "Initial value provider failed: ${e.asLog()}" }
            statePub.onError(e)
            throw RuntimeException("initialValueProvider failed", e)
        }
    }

    val data: Observable<T> = statePub
        .doOnSubscribe {
            scheduler.scheduleDirect {
                if (debug) log(ll, VERBOSE) { "data: scheduleDirect executing" }
                tryInitValue()
            }
        }
        .filterEqual { old, new -> old.dataId != new.dataId }
        .map { it.data }
        .hide()

    val latest: Single<T>
        get() = data.firstOrError()

    val snapshot: T
        get() {
            scheduler.scheduleDirect {
                if (debug) log(ll, VERBOSE) { "latest: scheduleDirect executing" }
                tryInitValue()
            }
            return statePub.blockingFirst().data
        }


    fun update(
        errorHandler: (Throwable) -> Unit,
        action: (T) -> T?
    ) {
        updateRx(
            action = action,
            errorHandler = errorHandler
        ).subscribe()
    }

    /**
     * When you return null, no data update will be triggered and the old value remains
     */
    fun update(action: (T) -> T?) {
        update(
            errorHandler = { throw RuntimeException("Unhandled update exception", it) },
            action = action
        )
    }

    /**
     * When you return null, no data update will be triggered and the old value remains
     */
    fun updateRx(action: (T) -> T?): Single<Update<T>> = updateRx(
        action = action,
        errorHandler = null
    )

    /**
     * Guarantees that the updated data is visible to other subscribers when it is emitted
     */
    private fun updateRx(
        action: (T) -> T?,
        errorHandler: ((Throwable) -> Unit)?
    ): Single<Update<T>> = Single.create<Update<T>> { emitter ->
        val updateActionId = UUID.randomUUID()
        val wrap: (T) -> T? = { oldValue ->
                val newValue = action.invoke(oldValue)

                val compDisp = CompositeDisposable()

                val replayer = ReplaySubject.create<State<T>>()
                replayer
                    //Wait for our action to have been processed
                    .filter { it.actionId === updateActionId }
                    .take(1)
                    .doFinally { compDisp.dispose() }
                    .subscribe { emitter.onSuccess(Update(oldValue, newValue ?: oldValue)) }
                    .also { compDisp.add(it) }

                statePub
                    .doFinally { compDisp.dispose() }
                    .subscribe(
                        { replayer.onNext(it) },
                        { replayer.onError(it) },
                        { replayer.onComplete() }
                    )
                    .also { compDisp.add(it) }

                emitter.setDisposable(compDisp)

                newValue
        }
        updatePub.onNext(
            UpdateAction(
                modify = wrap,
                id = updateActionId,
                errorHandler = { errorHandler?.invoke(it) ?: emitter.tryOnError(it) }
            )
        )

    }.subscribeOn(scheduler)

    fun close() {
        updatePub.onComplete()
        statePub.onComplete()
    }

    data class Update<T>(val oldValue: T, val newValue: T)

    data class UpdateAction<T>(
        val id: UUID = UUID.randomUUID(),
        val errorHandler: (Throwable) -> Unit,
        val modify: (T) -> T?
    )

    data class State<T>(
        val dataId: UUID = UUID.randomUUID(),
        val actionId: UUID,
        val data: T
    )

    companion object {
        fun createDefaultScheduler(name: String? = null) = SchedulersCustom.customScheduler(1, name)
    }
}
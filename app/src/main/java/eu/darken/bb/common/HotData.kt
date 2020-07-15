package eu.darken.bb.common

import android.annotation.SuppressLint
import com.jakewharton.rx.replayingShare
import eu.darken.bb.App
import eu.darken.bb.common.rx.SchedulersCustom
import eu.darken.bb.common.rx.filterEqual
import eu.darken.bb.common.rx.latest
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.util.*

/**
 * Threadsafe data/state updater
 */
@SuppressLint("CheckResult")
open class HotData<T>(
        initialValue: Single<T>,
        private val scheduler: Scheduler = createDefaultScheduler()
) {

    constructor(
            initialValue: T
    ) : this(Single.just(initialValue))

    constructor(
            name: String,
            initialValue: () -> T
    ) : this(Single.fromCallable(initialValue), createDefaultScheduler(name))

    constructor(
            scheduler: Scheduler,
            initialValue: () -> T
    ) : this(Single.fromCallable(initialValue), scheduler)

    private val updatePub = PublishSubject.create<UpdateAction<T>>().toSerialized()
    private val statePub: Subject<State<T>> = BehaviorSubject.create<State<T>>().toSerialized()

    var debugOutput: Boolean = false

    init {
        initialValue
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .doOnError { Timber.tag(TAG).e(it, "Error while providing initial value.") }
                .subscribe(
                        { value -> statePub.onNext(State(data = value, actionId = UUID.randomUUID())) },
                        { statePub.onError(it) }
                )

        updatePub
                .observeOn(scheduler)
                .concatMap { action ->
                    statePub.take(1).map { oldState ->
                        val newData = action.modify(oldState.data)
                        if (debugOutput) Timber.tag(TAG).v("Update ${oldState.data} -> $newData")
                        if (newData != null) {
                            State(
                                    data = newData,
                                    actionId = action.id
                            )
                        } else {
                            oldState.copy(actionId = action.id)
                        }
                    }
                }
                .doOnError { Timber.tag(TAG).e(it, "Error while updating value.") }
                .subscribe(
                        {
                            @Suppress("UNCHECKED_CAST")
                            statePub.onNext(it as State<T>)
                        },
                        { statePub.onError(it) }
                )
    }

    val data: Observable<T> = statePub
            .observeOn(scheduler)
            .filterEqual { old, new -> old.dataId != new.dataId }
            .map { it.data }
            .replayingShare()
            .hide()

    val latest: Single<T> = statePub
            .observeOn(scheduler)
            .map { it.data }
            .latest()
            .hide()

    val snapshot: T
        get() = statePub
                .map { it.data }
                .latest()
                .blockingGet()

    /**
     * When you return null, no data update will be triggered and the old value remains
     */
    fun update(action: (T) -> T?) {
        update(UpdateAction(modify = action))
    }

    fun update(updateAction: UpdateAction<T>) {
        updatePub.onNext(updateAction)
    }

    /**
     * When you return null, no data update will be triggered and the old value remains
     */
    fun updateRx(action: (T) -> T?): Single<Update<T>> =
            updateRx(UpdateAction(modify = action))

    /**
     * Guarantees that the updated data is visible to other subscribers when it is emitted
     */
    fun updateRx(
            updateAction: UpdateAction<T>
    ): Single<Update<T>> = Single.create<Update<T>> { emitter ->
        val wrap: (T) -> T? = { oldValue ->
            try {
                val newValue = updateAction.modify.invoke(oldValue)

                val compDisp = CompositeDisposable()

                val replayer = ReplaySubject.create<State<T>>()
                replayer
                        //Wait for our action to have been processed
                        .filter { it.actionId === updateAction.id }
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
            } catch (e: Throwable) {
                emitter.tryOnError(e)
                oldValue
            }
        }

        update(UpdateAction(modify = wrap, id = updateAction.id))
    }.subscribeOn(scheduler)

    fun close() {
        updatePub.onComplete()
        statePub.onComplete()
    }

    data class Update<T>(val oldValue: T, val newValue: T)

    data class UpdateAction<T>(
            val modify: (T) -> T?,
            val id: UUID = UUID.randomUUID()
    )

    data class State<T>(
            val data: T,
            val dataId: UUID = UUID.randomUUID(),
            val actionId: UUID
    )

    companion object {
        private val TAG = App.logTag("HotData")
        fun createDefaultScheduler(name: String? = null) = SchedulersCustom.customScheduler(1, name)
    }
}
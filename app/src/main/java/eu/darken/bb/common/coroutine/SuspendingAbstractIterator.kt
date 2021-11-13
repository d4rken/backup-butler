package eu.darken.bb.common.coroutine

private enum class State {
    Ready,
    NotReady,
    Done,
    Failed
}

/**
 * A base class to simplify implementing iterators so that implementations only have to implement [computeNext]
 * to implement the iterator, calling [done] when the iteration is complete.
 */
abstract class SuspendingAbstractIterator<T> : SuspendingIterator<T> {
    private var state = State.NotReady
    private var nextValue: T? = null

    override suspend fun hasNext(): Boolean {
        require(state != State.Failed)
        return when (state) {
            State.Done -> false
            State.Ready -> true
            else -> tryToComputeNext()
        }
    }

    override suspend fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        state = State.NotReady
        @Suppress("UNCHECKED_CAST")
        return nextValue as T
    }

    private suspend fun tryToComputeNext(): Boolean {
        state = State.Failed
        computeNext()
        return state == State.Ready
    }

    /**
     * Computes the next item in the iterator.
     *
     * This callback method should call one of these two methods:
     *
     * * [setNext] with the next value of the iteration
     * * [done] to indicate there are no more elements
     *
     * Failure to call either method will result in the iteration terminating with a failed state
     */
    protected abstract suspend fun computeNext(): Unit

    /**
     * Sets the next value in the iteration, called from the [computeNext] function
     */
    protected fun setNext(value: T): Unit {
        nextValue = value
        state = State.Ready
    }

    /**
     * Sets the state to done so that the iteration terminates.
     */
    protected fun done() {
        state = State.Done
    }
}

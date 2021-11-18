package testhelper.coroutine

import eu.darken.bb.common.coroutine.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class CoroutineTestRule(val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) : TestWatcher() {

    val testDispatcherProvider = object : DispatcherProvider {
        override val Default: CoroutineContext
            get() = testDispatcher
        override val Main: CoroutineContext
            get() = testDispatcher
        override val MainImmediate: CoroutineContext
            get() = testDispatcher
        override val Unconfined: CoroutineContext
            get() = testDispatcher
        override val IO: CoroutineContext
            get() = testDispatcher
    }

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}
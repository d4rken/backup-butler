package eu.darken.bb.common

import eu.darken.bb.common.debug.logging.log
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import java.io.IOException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

class HotDataTest : BaseTest() {

    @Test
    fun `test callback constructor`() {
        val callback = mockk<() -> String>()
        every { callback.invoke() } returns "testval"

        val hotData = HotData(debug = true, initial = callback)
        hotData.snapshot shouldBe "testval"
        verify { callback.invoke() }
    }

    @Test
    fun `test error while providing initial value`() {
        val error = IOException()
        val hotData = HotData(debug = true) { throw error }
        Assertions.assertThrows(RuntimeException::class.java) {
            hotData.latest.blockingGet()
        }
    }

    @Test
    fun `test value constructor`() {
        val hotData = HotData(debug = true) { "strawberry" }
        hotData.snapshot shouldBe "strawberry"
    }

    @Test
    fun `test close`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }
        val testSub = hotData.data.test()
        testSched.triggerActions()

        testSub.assertNotComplete()
        hotData.close()
        testSched.triggerActions()

        testSub.assertNoErrors()
        testSub.assertComplete()
    }

    @Test
    fun `test init blocking constructor`() {
        val pub = BehaviorSubject.create<String>()
        val hotData = HotData(debug = true) { pub.blockingFirst() }

        val testSched = TestScheduler()
        val testSub = hotData.data.observeOn(testSched).timeout(1, TimeUnit.SECONDS, testSched).test()
        testSched.advanceTimeBy(10, TimeUnit.SECONDS)
        testSub.assertError(TimeoutException::class.java)

        val testObs = hotData.data.test()
        pub.onNext("cake")
        testObs.awaitCount(1).assertValue { it == "cake" }
    }

    @Test
    fun `test sync value access`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }

        hotData.data.test() // Otherwise direct access on this scheduler deadlocks with snapshot/blockingGet()

        testSched.triggerActions()
        hotData.snapshot shouldBe "strawberry"

        hotData.update {
            it shouldBe "strawberry"
            "apple"
        }
        testSched.triggerActions()

        hotData.snapshot shouldBe "apple"
    }

    @Test
    fun `test sync value access2`() {
        val hotData = HotData(debug = true) { "strawberry" }

        await.until { hotData.snapshot == "strawberry" }

        hotData.update {
            it shouldBe "strawberry"
            "apple"
        }

        await.until { hotData.snapshot == "apple" }
    }

    @Test
    fun `test updating - ongoing observers`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }

        val permObs = hotData.data.test()
        testSched.triggerActions()
        permObs.assertValue("strawberry")

        hotData.update {
            it shouldBe "strawberry"
            "apple"
        }

        testSched.triggerActions()
        permObs.assertValues("strawberry", "apple")
    }

    @Test
    fun `test updating - oneshots`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }

        hotData.latest.test()
            .also { testSched.triggerActions() }
            .assertValue("strawberry")
        hotData.snapshot shouldBe "strawberry"

        hotData.update {
            it shouldBe "strawberry"
            "apple"
        }
        testSched.triggerActions()
        testSched.triggerActions()
        hotData.latest.test()
            .also { testSched.triggerActions() }
            .assertValue("apple")
        hotData.snapshot shouldBe "apple"
    }

    @Test
    fun `test updating - while unsubscribed`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }

        hotData.update {
            it shouldBe "strawberry"
            "apple"
        }
        testSched.triggerActions()
        hotData.latest.test().assertValue("apple")
        hotData.snapshot shouldBe "apple"
    }

    @Test
    fun `test updating - error`() {
        val thrownError = IOException()
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }
        val testObs = hotData.data.test()

        // Default handler just rethrows
        hotData.update { throw thrownError }

        testSched.triggerActions()
        testObs.assertError { it is RuntimeException && it.cause == thrownError }
    }

    @Test
    fun `test updating - custom error handler`() {
        val thrownError = IOException()
        var seenError: Throwable? = null
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }
        val testObs = hotData.data.test()
        hotData.update({ seenError = it }) {
            throw thrownError
        }

        testSched.triggerActions()
        testObs.assertValue("strawberry")
        seenError shouldBe thrownError
    }

    @Test
    fun `test rx updating - one shot observers`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }

        hotData.latest.test()
            .also { testSched.triggerActions() }
            .assertValue("strawberry")
        hotData.snapshot shouldBe "strawberry"

        val testSub = hotData.updateRx {
            it shouldBe "strawberry"
            "apple"
        }.test()

        testSched.triggerActions()
        testSub.assertComplete().assertValue(HotData.Update("strawberry", "apple"))

        hotData.latest.test()
            .also { testSched.triggerActions() }
            .assertValue("apple")
        hotData.snapshot shouldBe "apple"
    }

    @Test
    fun `test rx updating - ongoing observers`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }

        val permObserver = hotData.data.test()
        testSched.triggerActions()
        permObserver.assertValue("strawberry")

        val testSub = hotData.updateRx {
            it shouldBe "strawberry"
            "apple"
        }.test()

        testSched.triggerActions()
        testSub.assertComplete().assertValue(HotData.Update("strawberry", "apple"))

        permObserver.assertValues("strawberry", "apple")
    }

    @Test
    fun `test rx update failing`() {
        val testSched = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testSched) { "strawberry" }
        val valueObs = hotData.latest.test()
        testSched.triggerActions()
        valueObs.assertValue("strawberry")

        val error = IllegalArgumentException()
        val updateObs = hotData.updateRx {
            it shouldBe "strawberry"
            throw error
        }.test()
        testSched.triggerActions()
        updateObs.assertError(error)

        valueObs.assertValue("strawberry")
    }

    data class RcData(
        val value: Int = 0
    )

    @Test
    fun `test update racecondition`() {
        val hotData = HotData(debug = true) { RcData() }

        val threadCount = 16
        val perThread = 100

        val testObs = hotData.data.test()
        val tokenCount = threadCount * perThread

        (1..threadCount).forEach {
            thread {
                log { "Thread $it starting" }
                var todo = perThread
                while (todo > 0) {
                    hotData.update {
                        it.copy(value = it.value + 1)
                    }
                    todo--
                }
                log { "Thread $it done" }
            }
        }

        testObs.awaitCount(tokenCount + 1)

        testObs.values()[tokenCount] shouldBe RcData(value = tokenCount)
    }

    @Test
    fun `test update racecondition2`() {
        val hotData = HotData(debug = true) { RcData() }
        val testObs = hotData.data.test()

        val tokenCount = 100

        repeat(tokenCount) {
            hotData.update {
                it.copy(value = it.value + 1)
            }
        }

        testObs.awaitCount(tokenCount + 1)

        testObs.values()[tokenCount] shouldBe RcData(value = tokenCount)
    }

    @Test
    fun `test update racecondition3`() {
        val hotData = HotData(debug = true) { RcData() }
        val testObs = hotData.data.test()

        val tokenCount = 100
        val semaphore = Semaphore(0)

        repeat(tokenCount) {
            hotData.update {
                semaphore.tryAcquire(5, TimeUnit.SECONDS) shouldBe true
                it.copy(value = it.value + 1)
            }
        }

        semaphore.release(tokenCount)

        testObs.awaitCount(tokenCount + 1)

        testObs.values()[tokenCount] shouldBe RcData(value = tokenCount)
    }

    @Test
    fun `test update racecondition4`() {
        val testScheduler = TestScheduler()
        val hotData = HotData(debug = true, scheduler = testScheduler) { RcData() }
        val testObs = hotData.data.test()

        val tokenCount = 100

        repeat(tokenCount) {
            hotData.update {
                it.copy(value = it.value + 1)
            }
        }

        testScheduler.triggerActions()
        testObs.awaitCount(tokenCount + 1)

        testObs.values()[tokenCount] shouldBe RcData(value = tokenCount)
    }
}
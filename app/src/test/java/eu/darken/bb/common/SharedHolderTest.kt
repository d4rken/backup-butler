package eu.darken.bb.common

import eu.darken.bb.common.sharedresource.SharedResource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import testhelper.coroutine.runBlockingTest2

class SharedHolder2Test2 : BaseTest() {

    @Test
    fun `test init normal`() = runBlockingTest2(allowUncompleted = true) {
        val testValue = Any()

        val sr = SharedResource("tag", this, callbackFlow {
            send(testValue)
            awaitClose()
        })

        sr.isAlive shouldBe false

        val token = sr.get()
        sr.isAlive shouldBe true
        token.item shouldBe testValue

        token.close()

        advanceUntilIdle()

        sr.isAlive shouldBe false
    }

    @Test
    fun `test error while providing initial value`() = runBlockingTest2(allowUncompleted = true) {
        shouldThrow<Exception> {
            val sr = SharedResource(
                "tag",
                this,
                callbackFlow<Any> { throw IllegalArgumentException() })
            sr.get()
        }
    }

    @Test
    fun `test staggered close`() = runBlockingTest2(allowUncompleted = true) {
        val sr = SharedResource("tag", this, callbackFlow {
            send(Any())
            awaitClose()
        })

        val token1 = sr.get()
        val token2 = sr.get()
        token1.item shouldBe token2.item
        sr.isAlive shouldBe true
        token1.close()
        sr.isAlive shouldBe true
        token2.close()
        sr.isAlive shouldBe false
    }

    @Test
    fun `test close all`() = runBlockingTest2(allowUncompleted = true) {
        val sr = SharedResource("tag", this, callbackFlow {
            send(Any())
            awaitClose()
        })

        sr.get()
        sr.get()

        sr.isAlive shouldBe true
        sr.close()
        sr.isAlive shouldBe false
    }

    @Test
    fun `test child resource cascading close`() = runBlockingTest2(allowUncompleted = true) {
        val sr1 = SharedResource("sr1", this, callbackFlow {
            send(Any())
            awaitClose()
        })
        val sr2 = SharedResource("sr2", this, callbackFlow {
            send(Any())
            awaitClose()
        })

        val token1 = sr1.get()
        val token2 = sr2.get()
        sr1.addChild(token2)

        advanceUntilIdle()

        sr1.isAlive shouldBe true
        sr2.isAlive shouldBe true

        token1.close()
        advanceUntilIdle()

        sr1.isAlive shouldBe false
        sr2.isAlive shouldBe false
    }

    @Test
    fun `test child resource premature close`() = runBlockingTest2(allowUncompleted = true) {
        val sr1 = SharedResource("tag", this, callbackFlow {
            send(Any())
            awaitClose()
        })
        val sr2 = SharedResource("tag", this, callbackFlow {
            send(Any())
            awaitClose()
        })

        val token1 = sr1.get()
        val token2 = sr2.get()
        sr1.addChild(token2)

        sr1.isAlive shouldBe true
        sr2.isAlive shouldBe true
        token2.close()
        sr1.isAlive shouldBe true
        sr2.isAlive shouldBe false
        token1.close()
        sr1.isAlive shouldBe false
        sr2.isAlive shouldBe false
    }

    @Test
    fun `test child resource added to closed parent`() = runBlockingTest2(allowUncompleted = true) {
        val sr1 = SharedResource("tag", this, callbackFlow {
            send(Any())
            awaitClose()
        })
        val sr2 = SharedResource("tag", this, callbackFlow {
            send(Any())
            awaitClose()
        })

        val token2 = sr2.get()
        sr1.isAlive shouldBe false
        sr2.isAlive shouldBe true

        sr1.addChild(token2)
        sr1.isAlive shouldBe false
        sr2.isAlive shouldBe false
    }

    @Test
    fun `test accessing closed resource`() = runBlockingTest2(allowUncompleted = true) {
        val sr1 = SharedResource("tag", this, callbackFlow {
            send(Any())
            awaitClose()
        })
        val token = sr1.get()
        token.item
        token.close()
        shouldThrow<IllegalStateException> {
            token.item
        }
    }

}
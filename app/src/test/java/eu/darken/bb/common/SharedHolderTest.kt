package eu.darken.bb.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.BaseTest

class SharedHolderTest : BaseTest() {

    @Test
    fun `test init normal`() {
        val testValue = Any()
        val sr = SharedHolder<Any>("tag") {
            it.onAvailable(testValue)
        }
        sr.isAlive shouldBe false

        val token = sr.get()
        sr.isAlive shouldBe true
        token.item shouldBe testValue

        token.close()
        sr.isAlive shouldBe false
    }

    @Test
    fun `test error while providing initial value`() {
        shouldThrow<Exception> {
            val sr = SharedHolder<Any>("tag") {
                it.onError(IllegalArgumentException())
            }
            sr.get()
        }
    }


    @Test
    fun `test staggered close`() {
        val sr = SharedHolder<Any>("tag") { it.onAvailable(Any()) }

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
    fun `test close all`() {
        val sr = SharedHolder<Any>("tag") { it.onAvailable(Any()) }

        sr.get()
        sr.get()

        sr.isAlive shouldBe true
        sr.closeAll()
        sr.isAlive shouldBe false
    }

    @Test
    fun `test child resource cascading close`() {
        val sr1 = SharedHolder<Any>("tag") { it.onAvailable(Any()) }
        val sr2 = SharedHolder<Any>("tag") { it.onAvailable(Any()) }

        val token1 = sr1.get()
        val token2 = sr2.get()
        sr1.addChildResource(token2)

        sr1.isAlive shouldBe true
        sr2.isAlive shouldBe true
        token1.close()
        sr1.isAlive shouldBe false
        sr2.isAlive shouldBe false
    }

    @Test
    fun `test child resource premature close`() {
        val sr1 = SharedHolder<Any>("tag") { it.onAvailable(Any()) }
        val sr2 = SharedHolder<Any>("tag") { it.onAvailable(Any()) }

        val token1 = sr1.get()
        val token2 = sr2.get()
        sr1.addChildResource(token2)

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
    fun `test child resource added to closed parent`() {
        val sr1 = SharedHolder<Any>("tag") { it.onAvailable(Any()) }
        val sr2 = SharedHolder<Any>("tag") { it.onAvailable(Any()) }

        val token2 = sr2.get()
        sr1.isAlive shouldBe false
        sr2.isAlive shouldBe true

        sr1.addChildResource(token2)
        sr1.isAlive shouldBe false
        sr2.isAlive shouldBe false
    }

    @Test
    fun `test accessing closed resource`() {
        val sr1 = SharedHolder<Any>("tag") { it.onAvailable(Any()) }
        val token = sr1.get()
        token.item
        token.close()
        shouldThrow<IllegalStateException> {
            token.item
        }
    }

}
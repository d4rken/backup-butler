package eu.darken.bb.common

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

class SharedResourceTest {

    @Test
    fun `test init normal`() {
        val testValue = Any()
        val sr = SharedResource<Any>("tag") {
            it.onAvailable(testValue)
        }
        sr.isOpen shouldBe false

        val token = sr.get()
        sr.isOpen shouldBe true
        token.resource shouldBe testValue

        token.close()
        sr.isOpen shouldBe false
    }

    @Test
    fun `test error while providing initial value`() {
        shouldThrow<Exception> {
            val sr = SharedResource<Any>("tag") {
                it.onError(IllegalArgumentException())
            }
            sr.get()
        }
    }


    @Test
    fun `test staggered close`() {
        val sr = SharedResource<Any>("tag") { it.onAvailable(Any()) }

        val token1 = sr.get()
        val token2 = sr.get()
        token1.resource shouldBe token2.resource
        sr.isOpen shouldBe true
        token1.close()
        sr.isOpen shouldBe true
        token2.close()
        sr.isOpen shouldBe false
    }

    @Test
    fun `test close all`() {
        val sr = SharedResource<Any>("tag") { it.onAvailable(Any()) }

        sr.get()
        sr.get()

        sr.isOpen shouldBe true
        sr.closeAll()
        sr.isOpen shouldBe false
    }

    @Test
    fun `test child resource cascading close`() {
        val sr1 = SharedResource<Any>("tag") { it.onAvailable(Any()) }
        val sr2 = SharedResource<Any>("tag") { it.onAvailable(Any()) }

        val token1 = sr1.get()
        val token2 = sr2.get()
        sr1.addChildResource(token2)

        sr1.isOpen shouldBe true
        sr2.isOpen shouldBe true
        token1.close()
        sr1.isOpen shouldBe false
        sr2.isOpen shouldBe false
    }

    @Test
    fun `test child resource premature close`() {
        val sr1 = SharedResource<Any>("tag") { it.onAvailable(Any()) }
        val sr2 = SharedResource<Any>("tag") { it.onAvailable(Any()) }

        val token1 = sr1.get()
        val token2 = sr2.get()
        sr1.addChildResource(token2)

        sr1.isOpen shouldBe true
        sr2.isOpen shouldBe true
        token2.close()
        sr1.isOpen shouldBe true
        sr2.isOpen shouldBe false
        token1.close()
        sr1.isOpen shouldBe false
        sr2.isOpen shouldBe false
    }

    @Test
    fun `test child resource added to closed parent`() {
        val sr1 = SharedResource<Any>("tag") { it.onAvailable(Any()) }
        val sr2 = SharedResource<Any>("tag") { it.onAvailable(Any()) }

        val token2 = sr2.get()
        sr1.isOpen shouldBe false
        sr2.isOpen shouldBe true

        sr1.addChildResource(token2)
        sr1.isOpen shouldBe false
        sr2.isOpen shouldBe false
    }

    @Test
    fun `test accessing closed resource`() {
        val sr1 = SharedResource<Any>("tag") { it.onAvailable(Any()) }
        val token = sr1.get()
        token.resource
        token.close()
        shouldThrow<IllegalStateException> {
            token.resource
        }
    }

}
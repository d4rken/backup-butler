package eu.darken.bb.backup.backups.repos.local

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import testhelper.BaseTest

class LocalStorageRepoTest : BaseTest() {

    @get:Rule var rule: MockitoRule = MockitoJUnit.rule()

    @Before
    @Throws(Exception::class)
    override fun setup() {
        super.setup()
    }

    @After
    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun `test apk backup`() {

    }

    @Test
    fun `test apk split backups`() {

    }

}
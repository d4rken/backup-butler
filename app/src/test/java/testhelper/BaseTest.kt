package testhelper

import org.junit.After
import org.junit.Before
import timber.log.Timber


open class BaseTest {
    @Before
    @Throws(Exception::class)
    open fun setup() {
        Timber.plant(JUnitTree())
    }

    @After
    @Throws(Exception::class)
    open fun tearDown() {

    }
}

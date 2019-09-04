package testhelper

import org.junit.After
import org.junit.Before
import timber.log.Timber


abstract class BaseTest {
    @Before
    open fun setup() {
        Timber.plant(JUnitTree())
    }

    @After
    open fun tearDown() {

    }
}

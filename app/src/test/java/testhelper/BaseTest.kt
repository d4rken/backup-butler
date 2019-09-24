package testhelper

import timber.log.Timber


abstract class BaseTest {
    init {
        Timber.plant(JUnitTree())
    }
}

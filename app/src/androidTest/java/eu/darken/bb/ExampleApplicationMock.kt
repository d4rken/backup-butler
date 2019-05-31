package eu.darken.bb

import android.app.Activity

class ExampleApplicationMock : App() {

    fun setActivityComponentSource(injector: ManualInjector<Activity>) {
        this.activityInjector = injector
    }
}

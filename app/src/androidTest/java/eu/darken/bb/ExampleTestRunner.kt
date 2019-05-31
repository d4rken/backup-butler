package eu.darken.bb

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

import com.github.tmurakami.dexopener.DexOpener

class ExampleTestRunner : AndroidJUnitRunner() {

    @Throws(IllegalAccessException::class, ClassNotFoundException::class, InstantiationException::class)
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        DexOpener.install(this)
        return super.newApplication(cl, "eu.darken.bb.ExampleApplicationMock", context)
    }
}
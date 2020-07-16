package eu.darken.bb.common.rx

import android.view.View
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

fun View.clicksDebounced(): Observable<Unit> {
    return this.clicks().throttleFirst(250, TimeUnit.MILLISECONDS)
}
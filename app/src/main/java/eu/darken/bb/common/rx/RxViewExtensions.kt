package eu.darken.bb.common.rx

import android.widget.Button
import android.widget.ImageButton
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

fun Button.clicksDebounced(): Observable<Unit> {
    return this.clicks().throttleFirst(250, TimeUnit.MILLISECONDS)
}

fun ImageButton.clicksDebounced(): Observable<Unit> {
    return this.clicks().throttleFirst(250, TimeUnit.MILLISECONDS)
}
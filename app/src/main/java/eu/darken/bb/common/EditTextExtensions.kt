package eu.darken.bb.common

import android.widget.EditText
import com.jakewharton.rxbinding4.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding4.widget.textChangeEvents
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

fun EditText.setTextIfDifferent(newText: String) {
    if (this.text.toString() == newText) return
    this.setText(newText)
    setText(newText)
    setSelection(text.length)
}

fun EditText.setTextIfDifferentAndNotFocused(newText: String) {
    if (hasFocus()) return
    setTextIfDifferent(newText)
}

fun EditText.userTextChangeEvents(): Observable<TextViewTextChangeEvent> {
    return textChangeEvents().skipInitialValue().throttleLatest(250, TimeUnit.MILLISECONDS)
        .filter { it.view.hasFocus() }
}
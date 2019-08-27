package eu.darken.bb.common

import android.widget.EditText
import com.jakewharton.rxbinding3.widget.TextViewTextChangeEvent
import com.jakewharton.rxbinding3.widget.textChangeEvents
import io.reactivex.Observable

fun EditText.setTextIfDifferent(text: String) {
    if (this.text.toString() != text) this.setText(text)
}

fun EditText.userTextChangeEvents(): Observable<TextViewTextChangeEvent> {
    return textChangeEvents().skipInitialValue().filter { it.view.hasFocus() }
}
package eu.darken.bb.common

import android.widget.EditText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import reactivecircus.flowbinding.android.widget.TextChangeEvent
import reactivecircus.flowbinding.android.widget.textChangeEvents

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

// TODO Flow does not have throttleLatest
fun EditText.userTextChangeEvents(): Flow<TextChangeEvent> = textChangeEvents()
    .skipInitialValue()
    .debounce(50)
    .filter { it.view.hasFocus() }
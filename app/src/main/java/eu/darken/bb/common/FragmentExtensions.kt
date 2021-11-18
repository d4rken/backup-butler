package eu.darken.bb.common

import android.widget.Toast
import androidx.fragment.app.Fragment
import eu.darken.bb.R
import eu.darken.bb.common.error.localized

fun Fragment.todoToast() {
    Toast.makeText(requireContext(), R.string.general_todo_msg, Toast.LENGTH_LONG).show()
}

fun Fragment.toastError(throwable: Throwable?) {
    if (throwable != null) {
        Toast.makeText(requireContext(), throwable.localized(requireContext()).asText(), Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(requireContext(), R.string.general_error_label, Toast.LENGTH_LONG).show()
    }
}
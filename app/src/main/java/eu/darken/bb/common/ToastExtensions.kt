package eu.darken.bb.common

import android.widget.Toast
import androidx.fragment.app.Fragment
import eu.darken.bb.R

fun Fragment.todoToast() {
    Toast.makeText(requireContext(), R.string.todo, Toast.LENGTH_LONG).show()
}

fun Fragment.toastError(throwable: Throwable?) {
    if (throwable != null) {
        Toast.makeText(requireContext(), throwable.tryLocalizedErrorMessage(requireContext()), Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show()
    }
}
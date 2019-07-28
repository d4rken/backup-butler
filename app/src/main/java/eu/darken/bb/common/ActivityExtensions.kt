package eu.darken.bb.common

import android.app.Activity
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Activity.requireActionBar(): ActionBar {
    return (this as AppCompatActivity).supportActionBar!!
}

fun Fragment.requireActivityActionBar(): ActionBar {
    return (requireActivity() as AppCompatActivity).supportActionBar!!
}
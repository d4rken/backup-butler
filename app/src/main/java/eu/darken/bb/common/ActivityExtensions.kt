package eu.darken.bb.common

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import eu.darken.bb.R
import kotlin.reflect.KClass

fun Activity.requireActionBar(): ActionBar {
    return (this as AppCompatActivity).supportActionBar!!
}

fun Fragment.requireActivityActionBar(): ActionBar {
    return (requireActivity() as AppCompatActivity).supportActionBar!!
}

fun Activity.view(): View {
    return findViewById(android.R.id.content)
}

fun AppCompatActivity.showFragment(
        fragmentClass: KClass<out Fragment>,
        tag: String = fragmentClass.qualifiedName!!,
        @IdRes targetLayout: Int = R.id.fragment_frame,
        arguments: Bundle? = null,
        backStackPrevious: Boolean = false) {

    var fragment = supportFragmentManager.findFragmentByTag(tag)
    if (fragment == null) {
        fragment = supportFragmentManager.fragmentFactory.instantiate(this.javaClass.classLoader!!, fragmentClass.qualifiedName!!)
    }

    fragment.arguments = arguments
    val trans = supportFragmentManager.beginTransaction()
    if (backStackPrevious) trans.addToBackStack(null)
    trans.replace(targetLayout, fragment, tag)
    trans.commit()
}

fun Activity.todoToast() {
    Toast.makeText(this, R.string.general_todo_msg, Toast.LENGTH_LONG).show()
}
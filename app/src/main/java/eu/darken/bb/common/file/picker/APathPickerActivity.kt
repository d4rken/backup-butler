package eu.darken.bb.common.file.picker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.ensureContentView
import eu.darken.bb.common.file.picker.local.LocalPickerFragmentArgs
import eu.darken.bb.common.file.picker.types.TypesPickerFragmentArgs
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject

class APathPickerActivity : SmartActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    val navArgs by navArgs<APathPickerActivityArgs>()

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: APathPickerActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as APathPickerActivityVDC.Factory
        factory.create(handle, navArgs.options)
    })

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val sharedVM by lazy { ViewModelProvider(this).get(SharedPickerVM::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.pathpicker_activity)

        sharedVM.typeEvent.observe2(this) { vdc.onTypePicked(it) }
        sharedVM.resultEvent.observe2(this) { vdc.onResult(it) }

        vdc.launchSAFEvents.observe2(this) { intent ->
            startActivityForResult(intent, 18)
        }

        vdc.launchLocalEvents.observe2(this) {
            ensureContentView(R.layout.pathpicker_activity)
            val args = LocalPickerFragmentArgs(options = it)
            if (!navController.isGraphSet()) {
                val graph = navController.navInflater.inflate(R.navigation.picker_nav)
                graph.startDestination = R.id.localPickerFragment
                navController.setGraph(graph, args.toBundle())
                setupActionBarWithNavController(navController)
            } else {
                navController.navigate(R.id.action_typesPickerFragment_to_localPickerFragment, args.toBundle())
            }
        }

        vdc.launchTypesEvents.observe2(this) {
            ensureContentView(R.layout.pathpicker_activity)
            val args = TypesPickerFragmentArgs(options = it)
            if (!navController.isGraphSet()) {
                val graph = navController.navInflater.inflate(R.navigation.picker_nav)
                graph.startDestination = R.id.typesPickerFragment
                navController.setGraph(graph, args.toBundle())
                setupActionBarWithNavController(navController)
            } else {
                navController.popBackStack()
            }
        }

        vdc.resultEvents.observe2(this) { (result, finish) ->
            val resultCode = when {
                result.isSuccess -> Activity.RESULT_OK
                else -> Activity.RESULT_CANCELED
            }
            val data: Intent = APathPicker.toActivityResult(result)
            setResult(resultCode, data)
            if (finish) finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            18 -> vdc.onSAFPickerResult(data?.data)
            else -> throw NotImplementedError()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
package eu.darken.bb.common.files.ui.picker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.ensureContentView
import eu.darken.bb.common.files.ui.picker.local.LocalPickerFragmentArgs
import eu.darken.bb.common.files.ui.picker.types.TypesPickerFragmentArgs
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.databinding.PathpickerActivityBinding

@AndroidEntryPoint
class APathPickerActivity : SmartActivity() {
    val navArgs by navArgs<APathPickerActivityArgs>()

    private val vdc: APathPickerActivityVDC by viewModels()

    private val navController by lazy { findNavController(R.id.nav_host) }
    private val graph by lazy { navController.navInflater.inflate(R.navigation.picker_nav) }
    private val appBarConf by lazy {
        AppBarConfiguration.Builder()
            .setFallbackOnNavigateUpListener {
                finish()
                true
            }
            .build()
    }
    private lateinit var ui: PathpickerActivityBinding

    private val sharedVM by lazy { ViewModelProvider(this).get(SharedPickerVM::class.java) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = PathpickerActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)

        sharedVM.typeEvent.observe2(this) { vdc.onTypePicked(it) }
        sharedVM.resultEvent.observe2(this) { vdc.onResult(it) }

        vdc.launchSAFEvents.observe2(this) { intent ->
            startActivityForResult(intent, 18)
        }

        vdc.launchLocalEvents.observe2(this) { options ->
            ensureContentView(R.layout.pathpicker_activity)
            val args = LocalPickerFragmentArgs(options = options)
            if (!navController.isGraphSet()) {
                if (options.allowedTypes.size > 1) {
                    graph.startDestination = R.id.typesPickerFragment
                } else {
                    graph.startDestination = R.id.localPickerFragment
                }
                navController.setGraph(graph, args.toBundle())
                setupActionBarWithNavController(navController, appBarConf)
                if (options.allowedTypes.size > 1) {
                    navController.navigate(R.id.localPickerFragment, args.toBundle())
                }
            } else {
                navController.navigate(R.id.action_typesPickerFragment_to_localPickerFragment, args.toBundle())
            }
        }

        vdc.launchTypesEvents.observe2(this) {
            ensureContentView(R.layout.pathpicker_activity)
            val args = TypesPickerFragmentArgs(options = it)
            if (!navController.isGraphSet()) {
                graph.startDestination = R.id.typesPickerFragment
                navController.setGraph(graph, args.toBundle())
                setupActionBarWithNavController(navController, appBarConf)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConf) || super.onSupportNavigateUp()
    }
}
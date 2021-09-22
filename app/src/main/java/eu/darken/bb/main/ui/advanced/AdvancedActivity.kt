package eu.darken.bb.main.ui.advanced

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.findNavController
import eu.darken.bb.databinding.MainAdvancedActivityBinding

@AndroidEntryPoint
class AdvancedActivity : AppCompatActivity() {

    private val vdc: AdvancedActivityVDC by viewModels()
    private lateinit var binding: MainAdvancedActivityBinding
    private val navController by lazy { supportFragmentManager.findNavController(R.id.nav_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainAdvancedActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

package it.macgood.vkfilemanager.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import it.macgood.core.extension.viewBinding
import it.macgood.vkfilemanager.R
import it.macgood.vkfilemanager.databinding.ActivityMainBinding
import it.macgood.vkfilemanager.presentation.filemanager.FileManagerFragment.Companion.TAG

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        setupActionBarWithNavController(navController)


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        val preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        Log.d(TAG, "onDestroy: ${System.currentTimeMillis()}")
        preferences.edit().putLong(CLOSE_APP_TIME_PREFERENCE, System.currentTimeMillis()).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val APP_PREFERENCES = "APP_PREFERENCES"
        const val FIRST_OPEN_APP_PREFERENCE = "FIRST_OPEN_APP_PREFERENCES"
        const val CLOSE_APP_TIME_PREFERENCE = "CLOSE_APP_TIME_PREFERENCE"
    }
}
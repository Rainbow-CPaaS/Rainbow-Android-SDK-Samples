package com.ale.rainbowsample.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ale.rainbowsample.R
import com.ale.rainbowsample.components.SearchToolbar
import com.ale.rainbowsample.databinding.ActivityHomeBinding
import com.ale.rainbowsample.utils.getThemeColor
import com.ale.rainbowsdk.CallLogs
import com.ale.rainbowsdk.RainbowSdk
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors

class HomeActivity : AppCompatActivity(), SearchCallback {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 50
    }

    private lateinit var binding: ActivityHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var controller: NavController

    private val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
        binding.avatar.isVisible = destination.id in appBarConfiguration.topLevelDestinations
        binding.navView.isVisible = destination.id in appBarConfiguration.topLevelDestinations

        if (destination.id !in appBarConfiguration.topLevelDestinations) {
            binding.title.text = destination.label
        } else {
            binding.title.text = getString(R.string.app_name)
        }
    }

    private val missedCallCounterListener = object : CallLogs.ICallLogsListener {
        override fun notifyMissedCallCounterChange() {
            runOnUiThread { displayMissedCallCounter() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(SurfaceColors.SURFACE_2.getColor(this), SurfaceColors.SURFACE_2.getColor(this))
        )

        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment
        controller = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_conversations, R.id.navigation_contacts, R.id.navigation_rooms, R.id.navigation_callLogs
            )
        )

        setupActionBarWithNavController(controller, appBarConfiguration)
        navView.setupWithNavController(controller)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        window.navigationBarColor = getThemeColor(com.google.android.material.R.attr.colorSurfaceContainer)

        binding.avatar.displayContact(RainbowSdk().user().getConnectedUser())
        binding.avatar.setOnClickListener {
            controller.navigate(R.id.navigation_profile)
        }

        askForPermissions()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        controller.addOnDestinationChangedListener(listener)
        RainbowSdk().callLogs().registerCallLogsListener(missedCallCounterListener)
        displayMissedCallCounter()
    }

    override fun onPause() {
        super.onPause()
        controller.addOnDestinationChangedListener(listener)
        RainbowSdk().callLogs().unregisterCallLogsListener(missedCallCounterListener)
    }

    private fun askForPermissions() {
        val requestPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
            requestPermissions.add(Manifest.permission.READ_CONTACTS)

        if (requestPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toTypedArray<String>(), REQUEST_CODE_PERMISSIONS)
        }
    }

    fun setAppBarScrollId(view: View) {
        binding.appBar.liftOnScrollTargetViewId = view.id
    }

    override fun onSearchOpened() {
        binding.searchToolbar.clearText()
        binding.searchToolbar.display(binding.searchAction.x + (binding.searchAction.width / 2.0f), binding.searchAction.y + (binding.searchAction.height / 2.0f))
        binding.navView.isVisible = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragmentActivityHome) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = insets.bottom }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onSearchClosed() {
        binding.navView.isVisible = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragmentActivityHome) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = 0 }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun getSearchToolbar(): SearchToolbar {
        return binding.searchToolbar
    }

    override fun getSearchButton(): ImageView {
        return binding.searchAction
    }

    private fun displayMissedCallCounter() {
        val counter = RainbowSdk().callLogs().missedCounter
        val badge = binding.navView.getOrCreateBadge(R.id.navigation_callLogs)

        if (counter > 0) {
            badge.isVisible = true
            badge.number = counter
        } else {
            badge.isVisible = false
        }
    }
}
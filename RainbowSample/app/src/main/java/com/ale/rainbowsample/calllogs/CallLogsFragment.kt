package com.ale.rainbowsample.calllogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ale.rainbowsample.R
import com.ale.rainbowsample.activities.HomeActivity
import com.ale.rainbowsample.activities.SearchCallback
import com.ale.rainbowsample.databinding.FragmentCallLogsBinding
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.utils.viewLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration

class CallLogsFragment : Fragment() {

    private var binding: FragmentCallLogsBinding by viewLifecycle {
        binding.calllogsList.adapter = null
    }

    private val callLogsViewModel: CallLogsViewModel by viewModels()

    private lateinit var callLogsAdapter: CallLogsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallLogsBinding.inflate(inflater, container, false)
        (activity as? HomeActivity)?.setAppBarScrollId(binding.calllogsList)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? SearchCallback)?.getSearchButton()?.isVisible = false

        initializeCallLogsAdapter()

        collectLifecycleFlow(callLogsViewModel.uiState) { uiState ->
            callLogsAdapter.submitList(uiState.callLogs)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.calllog_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.filter_calllog -> displayFiltersDialog()
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onPause() {
        super.onPause()
        callLogsViewModel.markAllCallLogsAsRead()
    }

    private fun displayFiltersDialog() {
        val filters = arrayOf(getString(R.string.all), getString(R.string.missed))
        var selectedFilterIndex = when(callLogsViewModel.uiState.value.currentFilter) {
            CallLogFilter.ALL -> 0
            CallLogFilter.MISSED -> 1
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.filter_call_logs))
            .setSingleChoiceItems(filters, selectedFilterIndex) { _, which ->
                selectedFilterIndex = which
            }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val filter = when(selectedFilterIndex) {
                    0 -> CallLogFilter.ALL
                    else -> CallLogFilter.MISSED
                }

                callLogsViewModel.updateFilter(filter)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun initializeCallLogsAdapter() {
        callLogsAdapter = CallLogsAdapter()
        binding.calllogsList.adapter = callLogsAdapter
        binding.calllogsList.addItemDecoration(MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
            dividerThickness = 1
            isLastItemDecorated = false
        })
    }
}
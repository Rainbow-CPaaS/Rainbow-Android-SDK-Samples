package com.ale.rainbowsample.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ale.rainbowsample.R
import com.ale.rainbowsample.activities.HomeActivity
import com.ale.rainbowsample.activities.SearchCallback
import com.ale.rainbowsample.components.SearchToolbar
import com.ale.rainbowsample.databinding.FragmentConversationsBinding
import com.ale.rainbowsample.utils.HorizontalMarginItemDecoration
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.utils.viewLifecycle
import com.ale.rainbowsdk.RainbowSdk
import com.ale.util.dp
import com.google.android.material.divider.MaterialDividerItemDecoration


class ConversationsFragment : Fragment() {

    private var binding: FragmentConversationsBinding by viewLifecycle {
        binding.favoritesList.adapter = null
        binding.conversationsList.adapter = null
    }

    private val conversationsViewModel: ConversationsViewModel by viewModels()

    private lateinit var conversationsAdapter : ConversationsAdapter
    private lateinit var favoritesAdapter : FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConversationsBinding.inflate(inflater, container, false)
        (activity as? HomeActivity)?.setAppBarScrollId(binding.nestedScroll)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeConversationsAdapter()
        initializeFavoritesAdapter()
        initializeSearch()

        collectLifecycleFlow(conversationsViewModel.uiState) { uiState ->
            conversationsAdapter.submitList(uiState.conversations)
            favoritesAdapter.submitList(uiState.favorites)
        }

        requireActivity().onBackPressedDispatcher.addCallback(getViewLifecycleOwner(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!closeSearchIfNeeded()) {
                    if (!NavHostFragment.findNavController(this@ConversationsFragment).popBackStack()) {
                        requireActivity().finish()
                    }
                }
            }
        })
    }

    private fun initializeSearch() {
        val searchCallback =  (requireActivity() as? SearchCallback) ?: return
        searchCallback.getSearchButton().isVisible = true
        searchCallback.getSearchButton().setOnClickListener {
            searchCallback.onSearchOpened()
            searchCallback.getSearchToolbar().listener = object : SearchToolbar.Listener {
                override fun onSearchTextChange(text: String) {
                    // TODO search
                    println("**** search in conversations fragments")

                }

                override fun onSearchClosed() {
                    searchCallback.onSearchClosed()
                }
            }
        }
    }

    private fun closeSearchIfNeeded() : Boolean {
        if (isSearchOpen()) {
            (requireActivity() as SearchCallback).getSearchToolbar().collapse()
            (requireActivity() as SearchCallback).onSearchClosed()
            return true
        }

        return false
    }

    private fun isSearchOpen() : Boolean {
        return (requireActivity() as SearchCallback).getSearchToolbar().isVisible
    }

    private fun initializeConversationsAdapter() {
        conversationsAdapter = ConversationsAdapter()
        binding.conversationsList.adapter = conversationsAdapter

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
            dividerThickness = 1
            isLastItemDecorated = false
        }

        binding.conversationsList.addItemDecoration(divider)
    }

    private fun initializeFavoritesAdapter() {
        favoritesAdapter = FavoritesAdapter()
        binding.favoritesList.adapter = favoritesAdapter

        val divider = HorizontalMarginItemDecoration().apply {
            dividerThickness = 8.dp
            isLastItemDecorated = false
        }

        binding.favoritesList.addItemDecoration(divider)
        binding.favoritesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
}
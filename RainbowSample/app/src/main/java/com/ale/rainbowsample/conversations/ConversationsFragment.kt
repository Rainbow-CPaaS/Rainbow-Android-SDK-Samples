package com.ale.rainbowsample.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ale.rainbowsample.databinding.FragmentConversationsBinding
import com.ale.rainbowsample.utils.HorizontalMarginItemDecoration
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.utils.viewLifecycle
import com.ale.util.dp
import com.google.android.material.divider.MaterialDividerItemDecoration


class ConversationsFragment : Fragment() {

    private var binding: FragmentConversationsBinding by viewLifecycle()
    private val conversationsViewModel: ConversationsViewModel by viewModels()

    private lateinit var conversationsAdapter : ConversationsAdapter
    private lateinit var favoritesAdapter : FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConversationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeConversationsAdapter()
        initializeFavoritesAdapter()

        collectLifecycleFlow(conversationsViewModel.uiState) { uiState ->
            conversationsAdapter.submitList(uiState.conversations)
            favoritesAdapter.submitList(uiState.favorites)
        }
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
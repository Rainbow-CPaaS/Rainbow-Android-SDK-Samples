package com.ale.rainbowsample.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.FragmentConversationsBinding
import com.ale.rainbowsample.utils.collectLifecycleFlow
import com.ale.rainbowsample.utils.viewLifecycle
import com.google.android.material.divider.MaterialDividerItemDecoration


class ConversationsFragment : Fragment() {

    private var binding: FragmentConversationsBinding by viewLifecycle()
    private val conversationsViewModel: ConversationsViewModel by viewModels()

    private lateinit var adapter : ConversationsAdapter

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

        initializeAdapter()

        collectLifecycleFlow(conversationsViewModel.uiState) { uiState ->
            val newList = mutableListOf<ConversationsAdapter.ConversationsAdapterItemType>()

            newList.add(
                ConversationsAdapter.ConversationsAdapterItemType.FavoritesItem(uiState.favorites)
            )

            newList.addAll(
                uiState.conversations.map {
                    ConversationsAdapter.ConversationsAdapterItemType.ConversationItem(it)
                }
            )

            adapter.submitList(newList)
        }
    }

    private fun initializeAdapter() {
        adapter = ConversationsAdapter()
        binding.conversationsList.adapter = adapter

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
            dividerThickness = 1
            isLastItemDecorated = false
        }

        binding.conversationsList.addItemDecoration(divider)
    }
}
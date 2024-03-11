package com.ale.rainbowsample.conversations

import androidx.lifecycle.ViewModel
import com.ale.infra.list.IItemListChangeListener
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.system.measureTimeMillis

class ConversationsViewModel : ViewModel() {

    private val conversationsChangeListener = IItemListChangeListener {
        println("**** conversationsChangeListener")
        onConversationsChanged()
    }
    private val favoritesChangeListener = IItemListChangeListener {
        println("**** favoritesChangeListener")
        onConversationsChanged()
    }

    private val _uiState = MutableStateFlow(
        ConversationsUiState(
            conversations = emptyList(),
            favorites = emptyList()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        RainbowSdk().im().allConversations.registerChangeListener(conversationsChangeListener)
        RainbowSdk().favorites().favorites.registerChangeListener(favoritesChangeListener)

        onConversationsChanged()
    }

    override fun onCleared() {
        RainbowSdk().im().allConversations.unregisterChangeListener(conversationsChangeListener)
        RainbowSdk().favorites().favorites.unregisterChangeListener(favoritesChangeListener)
    }

    private fun onConversationsChanged() {

        val measure = measureTimeMillis {

            // We are filtering list to not display conversations related to webinar
            val conversations = RainbowSdk().im().allConversations.copyOfDataList.filterNot { conv -> conv.isWebinar }
            val favorites = RainbowSdk().favorites().favorites.copyOfDataList

            _uiState.update {
                it.copy(
                    conversations = conversations.mapToConversationsUiState(),
                    favorites = favorites.mapToFavoritesUiState()
                )
            }
        }

        println("**** $measure")
    }
}
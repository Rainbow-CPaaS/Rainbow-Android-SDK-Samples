package com.ale.rainbowsample.conversations

import com.ale.infra.contact.RainbowPresence
import com.ale.infra.manager.IMMessage
import com.ale.infra.manager.favorites.IRainbowFavorite
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.rainbowsdk.RainbowSdk

data class ConversationsUiState(
    val conversations: List<ConversationUiState>,
    val favorites: List<FavoriteUiState>
)

data class ConversationUiState(
    val conversation: IRainbowConversation,
    val lastMessage: IMMessage,
    val presence: RainbowPresence? = null,
    val displayName: String = "",
)

data class FavoriteUiState(
    val favorite: IRainbowFavorite,
    val name: String,
    val position: Int
)

fun List<IRainbowConversation>.mapToConversationsUiState(): List<ConversationUiState> {
    return this.map {
        ConversationUiState(
            conversation = it,
            lastMessage = it.lastMessage,
            displayName = it.contact?.getDisplayName("Unknown") ?: it.room?.getDisplayName("Unknown") ?: ""
        )
    }
}

fun List<IRainbowFavorite>.mapToFavoritesUiState(): List<FavoriteUiState> {
    return this.map {
        FavoriteUiState(
            favorite = it,
            name = it.contact?.getDisplayName("Unknown") ?: it.room?.getDisplayName("Unknown") ?: "",
            position = it.position
        )
    }
}
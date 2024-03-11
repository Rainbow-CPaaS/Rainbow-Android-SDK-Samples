package com.ale.rainbowsample.profile

import com.ale.infra.contact.RainbowPresence

data class ProfileUiState(
    val lastAvatarUpdate: String? = null,
    val userPresence: RainbowPresence? = null,
    val isLoading: Boolean = false,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickName: String? = null,
    val company: String? = null,
)
package com.ale.rainbowsample.calllogs

import androidx.lifecycle.ViewModel
import com.ale.infra.list.IItemListChangeListener
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CallLogsViewModel: ViewModel() {

    private val callLogsListener = IItemListChangeListener { onCallLogsChanged() }

    private val _uiState = MutableStateFlow(
        CallLogsUiState(
            callLogs = emptyList()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        RainbowSdk().callLogs().callLogs.registerChangeListener(callLogsListener)
        RainbowSdk().callLogs().fetchCallLogs(null)
    }

    override fun onCleared() {
        RainbowSdk().callLogs().callLogs.unregisterChangeListener(callLogsListener)
    }

    fun markAllCallLogsAsRead() {
        RainbowSdk().callLogs().markAllCallLogsAsRead()
    }

    fun updateFilter(newFilter: CallLogFilter) {
        _uiState.update {
            it.copy(
                currentFilter = newFilter
            )
        }

        onCallLogsChanged()
    }

    private fun onCallLogsChanged() {
        val callLogs = when(_uiState.value.currentFilter) {
            CallLogFilter.ALL ->  RainbowSdk().callLogs().callLogs.copyOfDataList
            CallLogFilter.MISSED ->  RainbowSdk().callLogs().missedCallLogs
        }

        _uiState.update {
            it.copy(
                callLogs = callLogs
            )
        }
    }
}
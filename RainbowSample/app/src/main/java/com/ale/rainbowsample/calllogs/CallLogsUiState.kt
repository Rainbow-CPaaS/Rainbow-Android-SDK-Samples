package com.ale.rainbowsample.calllogs

import com.ale.infra.xmpp.packetextension.calllog.CallLog

data class CallLogsUiState(
    val callLogs: List<CallLog>,
    val currentFilter: CallLogFilter = CallLogFilter.ALL
)

enum class CallLogFilter {
    ALL, MISSED
}
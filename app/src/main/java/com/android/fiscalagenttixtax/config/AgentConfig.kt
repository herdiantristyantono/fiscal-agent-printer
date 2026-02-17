package com.android.fiscalagenttixtax.config

import android.content.Context

data class AgentConfig(
    val agentId: String,
    val enableOfflineMode: Boolean
) {
    companion object {
        fun default(context: Context): AgentConfig {
            return AgentConfig(
                agentId = android.os.Build.SERIAL ?: "UNKNOWN",
                enableOfflineMode = true
            )
        }
    }
}
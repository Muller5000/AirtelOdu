package com.example.airteldatamonitor.data.model

enum class NetworkMode(
    val code: String,
    val title: String,
    val description: String,
    val is5gCapable: Boolean
) {
    AUTO_5G_4G_3G(
        code = "1F",
        title = "Auto (5G / 4G / 3G)",
        description = "Automatically connects to the fastest cellular network available (Recommended)",
        is5gCapable = true
    ),
    FIVE_G_PREFERRED(
        code = "1C",
        title = "5G Preferred (5G / 4G)",
        description = "Prefers 5G SA/NSA networks and falls back to 4G LTE when needed",
        is5gCapable = true
    ),
    FOUR_G_ONLY(
        code = "4",
        title = "4G LTE Only",
        description = "Locks modem strictly to 4G LTE for stable, uninterrupted broadband",
        is5gCapable = false
    ),
    FIVE_G_SA_ONLY(
        code = "10",
        title = "5G SA Only",
        description = "Forces 5G Standalone network (Requires pure 5G carrier core)",
        is5gCapable = true
    ),
    FIVE_G_NSA_ONLY(
        code = "C",
        title = "5G NSA Only",
        description = "Forces 5G Non-Standalone network with 4G anchor",
        is5gCapable = true
    ),
    FOUR_G_THREE_G(
        code = "6",
        title = "4G / 3G Auto",
        description = "Connects to 4G LTE with 3G fallback in fringe signal areas",
        is5gCapable = false
    );

    companion object {
        fun fromCode(code: String): NetworkMode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FOUR_G_ONLY
        }
    }
}

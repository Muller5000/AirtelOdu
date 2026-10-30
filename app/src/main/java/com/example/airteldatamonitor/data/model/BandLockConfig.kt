package com.example.airteldatamonitor.data.model

data class BandOption(
    val bandNumber: Int,
    val name: String,
    val frequencyLabel: String,
    val is5G: Boolean = false,
    val isSelected: Boolean = false
)

data class BandLockState(
    val is4gLockEnabled: Boolean = false,
    val is5gLockEnabled: Boolean = false,
    val supported4gBands: List<BandOption> = emptyList(),
    val supported5gBands: List<BandOption> = emptyList(),
    val currentConnectedBand: String = ""
)

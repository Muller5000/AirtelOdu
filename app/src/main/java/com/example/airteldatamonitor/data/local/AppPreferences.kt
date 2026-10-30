package com.example.airteldatamonitor.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.airteldatamonitor.data.model.DataUsage
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.RouterInfo
import com.example.airteldatamonitor.data.model.RouterSettings
import com.example.airteldatamonitor.data.model.SignalMetrics

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("airtel_odu_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): RouterSettings {
        val modeStr = prefs.getString("sim_mode", DemoSimulationMode.NORMAL.name)
        val simMode = try {
            DemoSimulationMode.valueOf(modeStr ?: DemoSimulationMode.NORMAL.name)
        } catch (_: Exception) {
            DemoSimulationMode.NORMAL
        }

        return RouterSettings(
            routerIp = prefs.getString("router_ip", "192.168.1.1") ?: "192.168.1.1",
            httpPort = prefs.getInt("http_port", 80),
            adminPasswordMasked = prefs.getString("admin_pass", "admin") ?: "admin",
            refreshIntervalSec = prefs.getInt("refresh_interval", 10),
            lowDataThresholdPercent = prefs.getInt("low_data_threshold", 80),
            enableDataAlerts = prefs.getBoolean("enable_alerts", true),
            autoReconnect = prefs.getBoolean("auto_reconnect", true),
            simulationMode = simMode,
            isLiveRouterMode = prefs.getBoolean("is_live_mode", true),
            isDarkMode = prefs.getBoolean("is_dark_mode", true),
            monthlyPlanGb = prefs.getFloat("monthly_plan_gb", 150.0f).toDouble(),
            planName = prefs.getString("plan_name", "Airtel Broadband") ?: "Airtel Broadband"
        )
    }

    fun saveSettings(settings: RouterSettings) {
        prefs.edit()
            .putString("router_ip", settings.routerIp)
            .putInt("http_port", settings.httpPort)
            .putString("admin_pass", settings.adminPasswordMasked)
            .putInt("refresh_interval", settings.refreshIntervalSec)
            .putInt("low_data_threshold", settings.lowDataThresholdPercent)
            .putBoolean("enable_alerts", settings.enableDataAlerts)
            .putBoolean("auto_reconnect", settings.autoReconnect)
            .putString("sim_mode", settings.simulationMode.name)
            .putBoolean("is_live_mode", settings.isLiveRouterMode)
            .putBoolean("is_dark_mode", settings.isDarkMode)
            .putFloat("monthly_plan_gb", settings.monthlyPlanGb.toFloat())
            .putString("plan_name", settings.planName)
            .apply()
    }

    fun loadCachedUsage(): DataUsage? {
        if (!prefs.contains("cached_used_gb")) return null
        val totalPlan = prefs.getFloat("cached_total_plan", 150.0f).toDouble()
        val used = prefs.getFloat("cached_used_gb", 0.0f).toDouble()
        val remaining = (totalPlan - used).coerceAtLeast(0.0)

        return DataUsage(
            planName = prefs.getString("cached_plan_name", "Airtel Broadband") ?: "Airtel Broadband",
            totalPlanGb = totalPlan,
            usedGb = used,
            remainingGb = remaining,
            billingCycleStart = prefs.getString("cached_cycle_start", "Oct 01, 2026") ?: "Oct 01, 2026",
            billingCycleEnd = prefs.getString("cached_cycle_end", "Oct 31, 2026") ?: "Oct 31, 2026",
            daysRemainingInCycle = prefs.getInt("cached_days_remaining", 29),
            dailyAverageGb = prefs.getFloat("cached_daily_avg", 0.6f).toDouble(),
            projectedMonthEndGb = prefs.getFloat("cached_projected_end", 20.0f).toDouble(),
            isDemoData = prefs.getBoolean("cached_is_demo", false)
        )
    }

    fun saveCachedUsage(usage: DataUsage) {
        prefs.edit()
            .putString("cached_plan_name", usage.planName)
            .putFloat("cached_total_plan", usage.totalPlanGb.toFloat())
            .putFloat("cached_used_gb", usage.usedGb.toFloat())
            .putString("cached_cycle_start", usage.billingCycleStart)
            .putString("cached_cycle_end", usage.billingCycleEnd)
            .putInt("cached_days_remaining", usage.daysRemainingInCycle)
            .putFloat("cached_daily_avg", usage.dailyAverageGb.toFloat())
            .putFloat("cached_projected_end", usage.projectedMonthEndGb.toFloat())
            .putBoolean("cached_is_demo", usage.isDemoData)
            .apply()
    }

    fun loadCachedRouterInfo(): RouterInfo? {
        if (!prefs.contains("cached_model_name")) return null
        return RouterInfo(
            modelName = prefs.getString("cached_model_name", "Airtel ZLT W154R Plus") ?: "Airtel ZLT W154R Plus",
            primaryBand = prefs.getString("cached_primary_band", "Band 7") ?: "Band 7",
            signal = SignalMetrics(
                bars = prefs.getInt("cached_bars", 4),
                rsrpDbm = prefs.getInt("cached_rsrp", -81),
                rsrqDb = prefs.getInt("cached_rsrq", -9),
                sinrDb = prefs.getFloat("cached_sinr", 15.0f).toDouble()
            ),
            uptimeFormatted = prefs.getString("cached_uptime", "6h 47m") ?: "6h 47m"
        )
    }

    fun saveCachedRouterInfo(info: RouterInfo) {
        prefs.edit()
            .putString("cached_model_name", info.modelName)
            .putString("cached_primary_band", info.primaryBand)
            .putInt("cached_bars", info.signal.bars)
            .putInt("cached_rsrp", info.signal.rsrpDbm)
            .putInt("cached_rsrq", info.signal.rsrqDb)
            .putFloat("cached_sinr", info.signal.sinrDb.toFloat())
            .putString("cached_uptime", info.uptimeFormatted)
            .apply()
    }
}

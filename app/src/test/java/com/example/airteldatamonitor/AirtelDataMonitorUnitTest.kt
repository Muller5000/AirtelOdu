package com.example.airteldatamonitor

import com.example.airteldatamonitor.data.model.ConnectedDevice
import com.example.airteldatamonitor.data.model.DemoSimulationMode
import com.example.airteldatamonitor.data.model.DeviceCategory
import com.example.airteldatamonitor.data.model.DeviceConnectionType
import com.example.airteldatamonitor.data.model.DeviceStatus
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.model.UsagePeriod
import com.example.airteldatamonitor.data.repository.RouterRepository
import com.example.airteldatamonitor.data.router.DemoRouterApiClient
import com.example.airteldatamonitor.data.router.ZltW154RRouterApiClient
import com.example.airteldatamonitor.ui.screens.DeviceFilter
import com.example.airteldatamonitor.ui.screens.DevicesViewModel
import com.example.airteldatamonitor.ui.screens.HomeViewModel
import com.example.airteldatamonitor.ui.screens.SettingsViewModel
import com.example.airteldatamonitor.ui.screens.UsageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AirtelDataMonitorUnitTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var demoApi: DemoRouterApiClient
    private lateinit var zltApi: ZltW154RRouterApiClient
    private lateinit var repository: RouterRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        demoApi = DemoRouterApiClient()
        zltApi = ZltW154RRouterApiClient(host = "127.0.0.1", port = 9999, timeoutMs = 500)
        repository = RouterRepository(demoApi, zltApi)
        repository.setLiveRouterMode(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDemoApiClient_returnsProperlyLabeledData() = runTest {
        val infoResult = demoApi.getRouterInfo()
        assertTrue(infoResult.isSuccess)
        val info = infoResult.getOrThrow()
        assertTrue(info.isDemoData)
        assertEquals("Airtel ZLT W154R Plus", info.modelName)
        assertEquals("192.168.8.1", info.ipAddress)

        val usageResult = demoApi.getDataUsage()
        assertTrue(usageResult.isSuccess)
        val usage = usageResult.getOrThrow()
        assertTrue(usage.isDemoData)
        assertEquals(150.0, usage.totalPlanGb, 0.01)

        val devicesResult = demoApi.getConnectedDevices()
        assertTrue(devicesResult.isSuccess)
        val devices = devicesResult.getOrThrow()
        assertTrue(devices.isNotEmpty())
        assertTrue(devices.all { it.isDemoData })
    }

    @Test
    fun testZltW154RRouterApiClient_failsGracefullyWhenUnreachable() = runTest {
        // Without router hardware on test loopback port, client should fail gracefully without crashing
        val unreachableClient = ZltW154RRouterApiClient(host = "192.0.2.1", port = 80, timeoutMs = 300)
        val result = unreachableClient.testConnection("192.0.2.1", 80)
        assertTrue(result.isFailure)
        val errorMsg = result.exceptionOrNull()?.message ?: ""
        assertTrue(errorMsg.contains("Cannot reach router") || errorMsg.contains("192.0.2.1"))
    }

    @Test
    fun testConnectedDevice_supportsNullPerDeviceMetricsHonoringHardwareLimitations() {
        val realDevice = ConnectedDevice(
            id = "AA:BB:CC:DD:EE:FF",
            name = "User Galaxy S23",
            ipAddress = "192.168.8.105",
            macAddress = "AA:BB:CC:DD:EE:FF",
            connectionType = DeviceConnectionType.WIFI_5GHZ,
            status = DeviceStatus.CONNECTED,
            downloadSpeedMbps = null, // Firmware leaves flow at 0
            uploadSpeedMbps = null,
            dataConsumedGb = null,
            category = DeviceCategory.SMARTPHONE,
            isDemoData = false
        )

        assertFalse(realDevice.isDemoData)
        assertNull(realDevice.downloadSpeedMbps)
        assertNull(realDevice.uploadSpeedMbps)
        assertNull(realDevice.dataConsumedGb)
        assertEquals("User Galaxy S23", realDevice.name)
    }

    @Test
    fun testHomeViewModel_loadsInitialDataSuccessfully() = runTest {
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals("Airtel ZLT W154R Plus", data.routerInfo.modelName)
        assertEquals(8, data.connectedDeviceCount)
    }

    @Test
    fun testHomeViewModel_switchesToErrorState() = runTest {
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.switchSimulationMode(DemoSimulationMode.ERROR)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Error when ERROR mode simulated", state is UiState.Error)
    }

    @Test
    fun testDevicesViewModel_filtersDevicesProperly() = runTest {
        val viewModel = DevicesViewModel(repository)
        advanceUntilIdle()

        val devices = DemoRouterApiClient.sampleDevices

        // Search query test
        viewModel.setSearchQuery("MacBook")
        val filtered = viewModel.filterDevices(devices)
        assertEquals(1, filtered.size)
        assertEquals("MacBook Pro 16\"", filtered.first().name)

        // Connection type filter test
        viewModel.setSearchQuery("")
        viewModel.setFilter(DeviceFilter.LAN)
        val lanDevices = viewModel.filterDevices(devices)
        assertTrue(lanDevices.all { it.connectionType == DeviceConnectionType.ETHERNET })
    }

    @Test
    fun testUsageViewModel_switchesPeriods() = runTest {
        val viewModel = UsageViewModel(repository)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(UsagePeriod.DAILY, (state as UiState.Success).data.period)

        viewModel.selectPeriod(UsagePeriod.WEEKLY)
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(UsagePeriod.WEEKLY, (state as UiState.Success).data.period)
    }

    @Test
    fun testSettingsViewModel_updatesConfigurationAndTogglesMode() = runTest {
        val viewModel = SettingsViewModel(repository)

        viewModel.updateRouterIp("192.168.8.1")
        assertEquals("192.168.8.1", viewModel.settings.value.routerIp)

        viewModel.updatePassword("newAdminPass123")
        assertEquals("newAdminPass123", viewModel.settings.value.adminPasswordMasked)

        viewModel.updateRefreshInterval(15)
        assertEquals(15, viewModel.settings.value.refreshIntervalSec)

        // Toggle Live Mode
        viewModel.toggleLiveRouterMode(true)
        assertTrue(viewModel.settings.value.isLiveRouterMode)

        viewModel.toggleLiveRouterMode(false)
        assertFalse(viewModel.settings.value.isLiveRouterMode)

        // Test connection in demo mode
        viewModel.testConnection()
        advanceUntilIdle()

        assertNotNull(viewModel.testFeedback.value)
        assertTrue(viewModel.testFeedback.value!!.contains("Connected"))
    }

    @Test
    fun testSettings_updateCustomDataPlan() = runTest {
        val viewModel = SettingsViewModel(repository)
        viewModel.updateDataPlan("Custom Unlimited 75GB", 75.0)

        assertEquals("Custom Unlimited 75GB", viewModel.settings.value.planName)
        assertEquals(75.0, viewModel.settings.value.monthlyPlanGb, 0.01)

        val usageState = repository.fetchDataUsage()
        assertTrue(usageState is UiState.Success)
        val usage = (usageState as UiState.Success).data
        assertEquals("Custom Unlimited 75GB", usage.planName)
        assertEquals(75.0, usage.totalPlanGb, 0.01)
    }

    @Test
    fun testSpeedTestViewModel_initialStateAndReset() = runTest {
        val speedViewModel = com.example.airteldatamonitor.ui.screens.SpeedTestViewModel(repository)
        assertEquals(com.example.airteldatamonitor.data.speedtest.SpeedTestStage.IDLE, speedViewModel.testState.value.stage)
        assertEquals(0.0, speedViewModel.testState.value.currentSpeedMbps, 0.01)

        speedViewModel.cancelSpeedTest()
        assertEquals(com.example.airteldatamonitor.data.speedtest.SpeedTestStage.IDLE, speedViewModel.testState.value.stage)
    }

    @Test
    fun testNetworkMode_switching() = runTest {
        val settingsViewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        assertEquals(com.example.airteldatamonitor.data.model.NetworkMode.FOUR_G_ONLY, settingsViewModel.currentNetworkMode.value)

        settingsViewModel.changeNetworkMode(com.example.airteldatamonitor.data.model.NetworkMode.AUTO_5G_4G_3G)
        advanceUntilIdle()

        assertEquals(com.example.airteldatamonitor.data.model.NetworkMode.AUTO_5G_4G_3G, settingsViewModel.currentNetworkMode.value)
        assertNotNull(settingsViewModel.networkSwitchFeedback.value)
        assertTrue(settingsViewModel.networkSwitchFeedback.value!!.contains("Successfully"))
    }

    @Test
    fun testRebootRouter_flow() = runTest {
        val settingsViewModel = SettingsViewModel(repository)
        advanceUntilIdle()

        assertFalse(settingsViewModel.isRebooting.value)
        settingsViewModel.rebootRouter()
        advanceUntilIdle()

        assertFalse(settingsViewModel.isRebooting.value)
        assertNotNull(settingsViewModel.rebootFeedback.value)
        assertTrue(settingsViewModel.rebootFeedback.value!!.contains("Router restarted successfully") || settingsViewModel.rebootFeedback.value!!.contains("rebooted"))
    }

    @Test
    fun testDevicesViewModel_blockAndRenameFlow() = runTest {
        val viewModel = DevicesViewModel(repository)
        advanceUntilIdle()

        val initialDevices = (viewModel.uiState.value as UiState.Success).data
        val targetDevice = initialDevices.first()

        // 1. Rename device
        viewModel.renameDevice(targetDevice, "Living Room Smart TV")
        advanceUntilIdle()

        val updatedDevices = (viewModel.uiState.value as UiState.Success).data
        val renamed = updatedDevices.first { it.macAddress == targetDevice.macAddress }
        assertEquals("Living Room Smart TV", renamed.displayName)

        // 2. Block device
        viewModel.toggleBlockDevice(renamed)
        advanceUntilIdle()

        assertNotNull(viewModel.actionFeedback.value)
        assertTrue(viewModel.actionFeedback.value!!.contains("blocked"))

        // 3. Unblock device
        val blockedDevice = (viewModel.uiState.value as UiState.Success).data.first { it.macAddress == targetDevice.macAddress }
        viewModel.toggleBlockDevice(blockedDevice)
        advanceUntilIdle()

        assertNotNull(viewModel.actionFeedback.value)
        assertTrue(viewModel.actionFeedback.value!!.contains("unblocked"))
    }

    @Test
    fun testBandLockViewModel_initialLoadAndPresets() = runTest {
        val viewModel = com.example.airteldatamonitor.ui.screens.BandLockViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertTrue(data.supported4gBands.isNotEmpty())
        assertTrue(data.supported5gBands.isNotEmpty())

        // Test Max Speed preset
        viewModel.applyPreset(com.example.airteldatamonitor.ui.screens.BandPreset.MAX_SPEED)
        assertTrue(viewModel.is4gLockEnabled.value)
        assertTrue(viewModel.is5gLockEnabled.value)
        assertTrue(viewModel.selected4gBands.value.contains(3))
        assertTrue(viewModel.selected5gBands.value.contains(78))

        // Test Indoors preset
        viewModel.applyPreset(com.example.airteldatamonitor.ui.screens.BandPreset.STRONGEST_INDOORS)
        assertTrue(viewModel.is4gLockEnabled.value)
        assertFalse(viewModel.is5gLockEnabled.value)
        assertTrue(viewModel.selected4gBands.value.contains(28))

        // Test Auto All preset
        viewModel.applyPreset(com.example.airteldatamonitor.ui.screens.BandPreset.AUTO_ALL_BANDS)
        assertFalse(viewModel.is4gLockEnabled.value)
        assertFalse(viewModel.is5gLockEnabled.value)
    }

    @Test
    fun testBandLockViewModel_applyChangesFlow() = runTest {
        val viewModel = com.example.airteldatamonitor.ui.screens.BandLockViewModel(repository)
        advanceUntilIdle()

        viewModel.toggle4gLock(true)
        viewModel.toggleBand(3, is5G = false)
        viewModel.applyChanges()
        advanceUntilIdle()

        assertNotNull(viewModel.actionFeedback.value)
        assertTrue(viewModel.actionFeedback.value!!.contains("successfully locked"))
    }

    @Test
    fun testSmsCenterViewModel_initialLoadAndActions() = runTest {
        val viewModel = com.example.airteldatamonitor.ui.screens.SmsCenterViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        val state = (viewModel.uiState.value as UiState.Success).data
        assertTrue(state.messages.isNotEmpty())
        assertTrue(state.totalCount > 0)

        // Test Mark as Read
        viewModel.markAsRead("1")
        advanceUntilIdle()

        // Test Filter
        viewModel.setFilter(com.example.airteldatamonitor.ui.screens.SmsFilter.AIRTEL_OFFICIAL)
        assertEquals(com.example.airteldatamonitor.ui.screens.SmsFilter.AIRTEL_OFFICIAL, viewModel.selectedFilter.value)

        // Test USSD Dial
        viewModel.dialUssd("*310#")
        advanceUntilIdle()

        val ussdSession = viewModel.ussdSession.value
        assertFalse(ussdSession.isDialing)
        assertEquals("*310#", ussdSession.lastDialedCode)
        assertNotNull(ussdSession.latestResponse)
        assertTrue(ussdSession.latestResponse!!.contains("Balance"))
    }
}


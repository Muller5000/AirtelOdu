package com.example.airteldatamonitor.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.airteldatamonitor.data.model.RouterSmsMessage
import com.example.airteldatamonitor.data.model.SmsCenterState
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsCenterScreen(
    viewModel: SmsCenterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()
    val ussdSession by viewModel.ussdSession.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: SMS Inbox, 1: USSD Dial
    var showComposeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Router SMS & USSD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Airtel SIM Card Inbox & Service Codes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            text = "←",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadMessages() },
                        enabled = !isRefreshing
                    ) {
                        Text(
                            text = "↻",
                            fontSize = 22.sp,
                            color = StatusInfo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AirtelRed,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp) }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📩 SMS Messages",
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == 0) AirtelRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val unread = (uiState as? UiState.Success)?.data?.unreadCount ?: 0
                            if (unread > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AirtelRed,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$unread",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            text = "⚡ USSD Services",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 1) AirtelRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // Feedback banner
            AnimatedVisibility(
                visible = actionFeedback != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                actionFeedback?.let { feedback ->
                    val isErr = feedback.startsWith("✗")
                    val bg = if (isErr) StatusError.copy(alpha = 0.15f) else NeonGreenAccent.copy(alpha = 0.15f)
                    val border = if (isErr) StatusError.copy(alpha = 0.5f) else NeonGreenAccent.copy(alpha = 0.5f)
                    val textCol = if (isErr) StatusError else NeonGreenAccent

                    Surface(
                        color = bg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(1.dp, border, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = feedback,
                                color = textCol,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearFeedback() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    color = textCol,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (activeTab == 0) {
                SmsInboxTab(
                    uiState = uiState,
                    selectedFilter = selectedFilter,
                    onSelectFilter = { viewModel.setFilter(it) },
                    onMarkRead = { viewModel.markAsRead(it) },
                    onMarkAllRead = { viewModel.markAllAsRead() },
                    onDelete = { viewModel.deleteMessage(it) },
                    onComposeClick = { showComposeDialog = true },
                    onRetry = { viewModel.loadMessages() }
                )
            } else {
                UssdDialTab(
                    session = ussdSession,
                    onDialCode = { viewModel.dialUssd(it) },
                    onSendSms = { phone, text ->
                        viewModel.sendSms(phone, text) { success ->
                            if (success) {
                                viewModel.loadMessages()
                            }
                        }
                    },
                    onSwitchToSmsTab = { activeTab = 0 }
                )
            }
        }
    }

    if (showComposeDialog) {
        ComposeSmsDialog(
            onDismiss = { showComposeDialog = false },
            onSend = { phone, content ->
                viewModel.sendSms(phone, content) { success ->
                    if (success) {
                        showComposeDialog = false
                        viewModel.loadMessages()
                    }
                }
            }
        )
    }
}

@Composable
fun SmsInboxTab(
    uiState: UiState<SmsCenterState>,
    selectedFilter: SmsFilter,
    onSelectFilter: (SmsFilter) -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDelete: (String) -> Unit,
    onComposeClick: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = StatusInfo)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Reading SMS from router SIM...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚠", color = StatusError, fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Unable to Read Router SMS", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(uiState.message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Retry", color = StatusInfo)
                    }
                }
            }
        }
        is UiState.Success -> {
            val state = uiState.data
            val filteredMessages = remember(state.messages, selectedFilter) {
                when (selectedFilter) {
                    SmsFilter.ALL -> state.messages
                    SmsFilter.UNREAD -> state.messages.filter { !it.isRead }
                    SmsFilter.AIRTEL_OFFICIAL -> state.messages.filter {
                        it.sender.contains("Airtel", ignoreCase = true) ||
                        it.sender.contains("ERC", ignoreCase = true) ||
                        it.sender.contains("MEGA", ignoreCase = true)
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar with Actions & Filter Chips
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Action row: Section title on left, Mark All Read & + Compose on right
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "MESSAGES (${filteredMessages.size})",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (state.unreadCount > 0) {
                                    Text(
                                        text = "Read All",
                                        color = StatusInfo,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StatusInfo.copy(alpha = 0.12f))
                                            .border(1.dp, StatusInfo.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                            .clickable { onMarkAllRead() }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                                Text(
                                    text = "+ Compose",
                                    color = NeonGreenAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreenAccent.copy(alpha = 0.15f))
                                        .border(1.dp, NeonGreenAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .clickable { onComposeClick() }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Filter Chips row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChipCompact(
                                label = "All (${state.totalCount})",
                                isSelected = selectedFilter == SmsFilter.ALL,
                                onClick = { onSelectFilter(SmsFilter.ALL) }
                            )
                            FilterChipCompact(
                                label = "Unread (${state.unreadCount})",
                                isSelected = selectedFilter == SmsFilter.UNREAD,
                                onClick = { onSelectFilter(SmsFilter.UNREAD) }
                            )
                            FilterChipCompact(
                                label = "Airtel Official",
                                isSelected = selectedFilter == SmsFilter.AIRTEL_OFFICIAL,
                                onClick = { onSelectFilter(SmsFilter.AIRTEL_OFFICIAL) }
                            )
                        }
                    }
                }

                if (filteredMessages.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📭", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No SMS messages found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    items(filteredMessages, key = { it.id }) { msg ->
                        SmsMessageCard(
                            message = msg,
                            onMarkRead = { onMarkRead(msg.id) },
                            onDelete = { onDelete(msg.id) },
                            onCopyText = { text ->
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("SMS Content", text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onCopyOtp = { otp ->
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("OTP Code", otp))
                                Toast.makeText(context, "Copied OTP: $otp", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun SmsMessageCard(
    message: RouterSmsMessage,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onCopyText: (String) -> Unit,
    onCopyOtp: (String) -> Unit
) {
    val isAirtel = message.sender.contains("Airtel", ignoreCase = true) ||
            message.sender.contains("ERC", ignoreCase = true) ||
            message.sender.contains("MEGA", ignoreCase = true)

    val borderColor = if (!message.isRead) StatusInfo.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
    val bg = if (!message.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!message.isRead) onMarkRead()
            }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Sender, Unread Dot, Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (!message.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusInfo)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAirtel) AirtelRed else MaterialTheme.colorScheme.onSurface
                    )
                    if (isAirtel) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AirtelRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, AirtelRed.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "OFFICIAL",
                                color = AirtelRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "🗑",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onDelete() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body text
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // OTP Detection Banner
            val otp = message.extractedOtp
            if (otp != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = NeonGreenAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreenAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔑", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detected OTP: ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Text(
                                text = otp,
                                color = NeonGreenAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Copy Code",
                            color = NeonGreenAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onCopyOtp(otp) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer row: Timestamp on left, Copy full message on right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Text(
                    text = "📋 Copy Text",
                    color = StatusInfo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onCopyText(message.content) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChipCompact(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) StatusInfo.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) StatusInfo else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) StatusInfo else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun UssdDialTab(
    session: UssdSessionState,
    onDialCode: (String) -> Unit,
    onSendSms: (String, String) -> Unit = { _, _ -> },
    onSwitchToSmsTab: () -> Unit = {}
) {
    var customCode by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 0. Airtel 4G LTE Compatibility & SMS Balance Inquiries Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusWarning.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ️", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AIRTEL 4G LTE BROADBAND NOTICE",
                            color = StatusWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "USSD (*310#, *312#) requires 2G/3G circuit-switched voice channels. Because Airtel broadband routers operate on pure LTE data without CSFB, USSD requests time out on cell towers.\n\nRecommended: Check your balance and bundles using SMS below (Airtel sends the response directly to your SMS tab).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                onSendSms("141", "STATUS")
                                onSwitchToSmsTab()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreenAccent.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreenAccent.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📩 Check Bal (SMS)", color = NeonGreenAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                onSendSms("141", "HELP")
                                onSwitchToSmsTab()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusInfo.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusInfo.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📩 Plans & Info", color = StatusInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1. Quick USSD Shortcuts Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AIRTEL QUICK SHORTCODES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UssdQuickButton(
                            code = "*312#",
                            label = "Buy Data",
                            tint = StatusInfo,
                            modifier = Modifier.weight(1f),
                            onClick = { onDialCode("*312#") }
                        )
                        UssdQuickButton(
                            code = "*310#",
                            label = "Airtime Bal",
                            tint = AirtelRed,
                            modifier = Modifier.weight(1f),
                            onClick = { onDialCode("*310#") }
                        )
                        UssdQuickButton(
                            code = "*323#",
                            label = "Data Bal",
                            tint = StatusWarning,
                            modifier = Modifier.weight(1f),
                            onClick = { onDialCode("*323#") }
                        )
                    }
                }
            }
        }

        // 2. Custom USSD Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CUSTOM USSD DIALER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = customCode,
                            onValueChange = { customCode = it },
                            placeholder = { Text("e.g. *141# or 1", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    keyboardController?.hide()
                                    if (customCode.isNotBlank()) {
                                        onDialCode(customCode)
                                        customCode = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StatusInfo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = StatusInfo
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                if (customCode.isNotBlank()) {
                                    onDialCode(customCode)
                                    customCode = ""
                                }
                            },
                            enabled = customCode.isNotBlank() && !session.isDialing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AirtelRed,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            if (session.isDialing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text("Send", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Live USSD Terminal Output Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusInfo.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (session.isDialing) StatusWarning else NeonGreenAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "USSD NETWORK TERMINAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                        if (session.lastDialedCode.isNotEmpty()) {
                            Text(
                                text = "Code: ${session.lastDialedCode}",
                                color = StatusInfo,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (session.isDialing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = StatusInfo,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Connecting to Airtel cellular USSD gateway...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else if (session.latestResponse != null) {
                        Text(
                            text = session.latestResponse,
                            color = NeonGreenAccent,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text = "Ready. Tap any quick shortcode above or enter a code to dial.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UssdQuickButton(
    code: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = tint,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ComposeSmsDialog(
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var recipient by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Compose SMS via Router SIM",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Message will be sent using the cellular SIM card inside your Airtel router.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Recipient Phone Number") },
                    placeholder = { Text("e.g. 08021234567 or 141") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusInfo,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = StatusInfo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Message Content") },
                    placeholder = { Text("Type your message here...") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusInfo,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = StatusInfo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${content.length}/160 chars",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(recipient, content) },
                enabled = recipient.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AirtelRed,
                    contentColor = Color.White
                )
            ) {
                Text("Send Message", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

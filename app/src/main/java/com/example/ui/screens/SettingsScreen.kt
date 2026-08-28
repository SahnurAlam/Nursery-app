package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.AppThemeMode
import com.example.data.local.UserPreferences
import com.example.data.model.Sale
import com.example.ui.components.NurseryLogo
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.InStockGreen
import com.example.ui.theme.InStockGreenContainer
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.ExportUtils
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NurseryViewModel,
    onNavigateBack: () -> Unit
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nurseryName by remember(preferences.nurseryName) { mutableStateOf(preferences.nurseryName) }
    var ownerName by remember(preferences.ownerName) { mutableStateOf(preferences.ownerName) }
    var contactPhone by remember(preferences.contactPhone) { mutableStateOf(preferences.contactPhone) }
    var nurseryAddress by remember(preferences.nurseryAddress) { mutableStateOf(preferences.nurseryAddress) }

    var invoiceNotes by remember(preferences.invoiceNotes) { mutableStateOf(preferences.invoiceNotes) }
    var invoiceFooter by remember(preferences.invoiceFooter) { mutableStateOf(preferences.invoiceFooter) }
    var showResetInvoiceConfirmDialog by remember { mutableStateOf(false) }
    var showPreviewInvoiceDialog by remember { mutableStateOf(false) }

    var showRestoreJsonDialog by remember { mutableStateOf(false) }
    var jsonRestoreText by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showResetLogoConfirmDialog by remember { mutableStateOf(false) }
    var tempSelectedLogoUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker launcher for custom nursery logo
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            tempSelectedLogoUri = uri
        }
    }

    // File picker for JSON backup restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        line = reader.readLine()
                    }
                    viewModel.restoreFromJson(stringBuilder.toString()) { success ->
                        if (success) {
                            viewModel.showMessage("Database restored successfully!")
                        }
                    }
                }
            } catch (e: Exception) {
                viewModel.showMessage("Error reading backup file: ${e.localizedMessage}")
            }
        }
    }

    val currencyOptions = listOf("₹", "$", "€", "£", "৳", "₨", "AED")

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = "Nursery Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline First Badge
            item {
                Surface(
                    color = InStockGreenContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline First",
                            tint = InStockGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "100% Offline & Private",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = InStockGreen
                            )
                            Text(
                                text = "All nursery data is stored locally on this device. No internet required.",
                                style = MaterialTheme.typography.bodySmall,
                                color = InStockGreen.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // SECTION 1: Theme & Display
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Theme & Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                val isSelected = preferences.themeMode == mode
                                val label = when (mode) {
                                    AppThemeMode.SYSTEM -> "System"
                                    AppThemeMode.LIGHT -> "Light"
                                    AppThemeMode.DARK -> "Dark"
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateThemeMode(mode) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))

                        // Currency Selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Currency Symbol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currencyOptions.forEach { curr ->
                                val isSelected = preferences.currencySymbol == curr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateCurrency(curr) },
                                    label = { Text(curr, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: Nursery Business Profile
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Nursery Profile & Receipt Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "This information appears on customer sales memos and printable reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = nurseryName,
                            onValueChange = { nurseryName = it },
                            label = { Text("Nursery Business Name") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_nursery_name_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Manager / Owner Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_owner_name_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Contact Phone / WhatsApp") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_phone_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = nurseryAddress,
                            onValueChange = { nurseryAddress = it },
                            label = { Text("Nursery Address") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_address_input"),
                            minLines = 2,
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                viewModel.updateNurseryProfile(
                                    name = nurseryName.trim(),
                                    owner = ownerName.trim(),
                                    phone = contactPhone.trim(),
                                    address = nurseryAddress.trim()
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_nursery_profile_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Nursery Profile")
                        }
                    }
                }
            }

            // SECTION 3: Nursery Branding & Logo Management
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Branding & Logo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Set your custom nursery brand logo. It will appear throughout the app on the dashboard, receipts, invoices, and reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Central Logo Preview Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (tempSelectedLogoUri != null) {
                                    Surface(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                                        color = Color.White,
                                        shadowElevation = 4.dp
                                    ) {
                                        AsyncImage(
                                            model = tempSelectedLogoUri,
                                            contentDescription = "New Logo Preview",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "Preview: New Logo Selected (Unsaved)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    NurseryLogo(
                                        customLogoPath = preferences.customLogoPath,
                                        size = 96.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                        backgroundColor = Color.White
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (preferences.customLogoPath != null) InStockGreenContainer else MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = if (preferences.customLogoPath != null) "Active: Custom Nursery Logo" else "Active: Default Nursery Logo",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (preferences.customLogoPath != null) InStockGreen else MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Logo Actions
                        if (tempSelectedLogoUri != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        tempSelectedLogoUri?.let { uri ->
                                            viewModel.saveCustomLogoFromUri(uri, context) { success ->
                                                if (success) {
                                                    tempSelectedLogoUri = null
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_logo_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Save Logo")
                                }

                                OutlinedButton(
                                    onClick = { tempSelectedLogoUri = null },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cancel_logo_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Cancel")
                                }
                            }

                            FilledTonalButton(
                                onClick = { logoPickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("change_selected_logo_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Choose Different Image")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { logoPickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("select_logo_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (preferences.customLogoPath != null) "Change Logo" else "Upload Logo")
                                }

                                if (preferences.customLogoPath != null) {
                                    OutlinedButton(
                                        onClick = { showResetLogoConfirmDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("reset_logo_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = ExpenseRed
                                        )
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Reset Logo")
                                    }
                                }
                            }
                        }

                        Text(
                            text = "💡 Supported formats: PNG, JPG, WEBP. Changing the in-app nursery logo does not alter your Android device app icon.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // SECTION 4: Invoice / Memo Customization
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Invoice / Memo Customization",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Customize default notes & footer printed on sales memos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        OutlinedTextField(
                            value = invoiceNotes,
                            onValueChange = { invoiceNotes = it },
                            label = { Text("Invoice Notes") },
                            placeholder = { Text("e.g. Thank you for buying from our nursery! Plant more trees.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_invoice_notes_input"),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp),
                            supportingText = {
                                Text("Appears below the total amount and payment method")
                            }
                        )

                        OutlinedTextField(
                            value = invoiceFooter,
                            onValueChange = { invoiceFooter = it },
                            label = { Text("Closing Message") },
                            placeholder = { Text("e.g. Visit Again! 🌱") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_invoice_footer_input"),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            supportingText = {
                                Text("Appears at the very bottom/footer of the sales memo")
                            }
                        )

                        // Live preview container
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LIVE MEMO PREVIEW",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "#INV-00101",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )

                                Text(
                                    text = "1. Mango Plant (Hybrid)  2 pcs x ${preferences.currencySymbol} 350.00",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "TOTAL PAID: ${preferences.currencySymbol} 650.00 (Cash / UPI)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )

                                if (invoiceNotes.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Note: $invoiceNotes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "(Notes section empty — omitted from memo)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (invoiceFooter.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = invoiceFooter,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "(Closing message empty — omitted from memo)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        FilledTonalButton(
                            onClick = { showPreviewInvoiceDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("preview_invoice_memo_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Preview Full Invoice")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateInvoiceCustomization(invoiceNotes, invoiceFooter)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_invoice_customization_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save")
                            }

                            OutlinedButton(
                                onClick = { showResetInvoiceConfirmDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_invoice_customization_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Reset to Default")
                            }
                        }
                    }
                }
            }

            // SECTION 5: Database Backup & Restore
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Backup & Restore Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Export an offline JSON backup of your plants, sales, expenses, and customers to prevent data loss when switching phones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Export Full Backup Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val backupJson = viewModel.getExportJsonString()
                                    val timestamp = System.currentTimeMillis()
                                    ExportUtils.shareFile(
                                        context = context,
                                        content = backupJson,
                                        fileName = "Sahnur_Nursery_Backup_$timestamp.json",
                                        mimeType = "application/json",
                                        title = "Export Nursery Backup JSON"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_backup_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Export Database Backup (JSON)")
                        }

                        // Export Customer Data (JSON) Button
                        FilledTonalButton(
                            onClick = {
                                coroutineScope.launch {
                                    val customerJson = viewModel.getExportCustomerDataJsonString()
                                    val timestamp = System.currentTimeMillis()
                                    ExportUtils.shareFile(
                                        context = context,
                                        content = customerJson,
                                        fileName = "Sahnur_Customers_Data_$timestamp.json",
                                        mimeType = "application/json",
                                        title = "Export Customer Data JSON"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_customer_data_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Export Customer Data (JSON)")
                        }

                        // Restore Backup Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { filePickerLauncher.launch("application/json") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Import JSON")
                            }

                            OutlinedButton(
                                onClick = { showRestoreJsonDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Paste JSON")
                            }
                        }
                    }
                }
            }

            // SECTION 4: App Information
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nursery Data Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Version 1.0.0 (Production Build)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Designed for commercial plant nurseries, horticultural farms, and seedling suppliers. Fully offline, fast, and secure.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Paste JSON Restore Dialog
    if (showRestoreJsonDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreJsonDialog = false },
            title = { Text("Paste JSON Backup") },
            text = {
                Column {
                    Text("Paste the exported JSON backup text below to restore your nursery records:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonRestoreText,
                        onValueChange = { jsonRestoreText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{\n  \"plants\": [...],\n  \"customers\": [...]\n}") },
                        minLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreFromJson(jsonRestoreText) { success ->
                            if (success) {
                                showRestoreJsonDialog = false
                                jsonRestoreText = ""
                            }
                        }
                    },
                    enabled = jsonRestoreText.isNotBlank()
                ) {
                    Text("Restore Database")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreJsonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Logo Confirmation Dialog
    if (showResetLogoConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetLogoConfirmDialog = false },
            title = { Text("Reset Nursery Logo") },
            text = {
                Text("Are you sure you want to remove your custom nursery logo and restore the default branding logo?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeCustomLogo(context)
                        showResetLogoConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Reset to Default")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetLogoConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Invoice Customization Confirmation Dialog
    if (showResetInvoiceConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetInvoiceConfirmDialog = false },
            title = { Text("Reset Invoice Customization") },
            text = {
                Text(
                    "Are you sure you want to reset invoice notes and closing message to default values?\n\n" +
                    "• Default Notes:\n\"${UserPreferences.DEFAULT_INVOICE_NOTES}\"\n\n" +
                    "• Default Closing:\n\"${UserPreferences.DEFAULT_INVOICE_FOOTER}\""
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        invoiceNotes = UserPreferences.DEFAULT_INVOICE_NOTES
                        invoiceFooter = UserPreferences.DEFAULT_INVOICE_FOOTER
                        viewModel.resetInvoiceCustomization()
                        showResetInvoiceConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Reset to Default")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetInvoiceConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full Invoice / Memo Preview Dialog
    if (showPreviewInvoiceDialog) {
        val previewSale = remember {
            Sale(
                id = 101L,
                customerId = 1L,
                customerName = "Ramesh Kumar (Sample Preview)",
                plantId = 1L,
                plantName = "Thai 7 Guava (Grafted)",
                quantity = 3,
                unitPrice = 180.0,
                discount = 40.0,
                amount = 500.0,
                paymentMethod = "Cash / UPI",
                notes = "",
                date = System.currentTimeMillis()
            )
        }
        ReceiptDialog(
            sale = previewSale,
            preferences = preferences.copy(
                invoiceNotes = invoiceNotes,
                invoiceFooter = invoiceFooter
            ),
            onDismiss = { showPreviewInvoiceDialog = false }
        )
    }
}

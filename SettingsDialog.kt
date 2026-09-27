package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BrowserSettings
import com.example.data.model.SearchEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: BrowserSettings,
    onUpdateSettings: ((BrowserSettings) -> BrowserSettings) -> Unit,
    onOpenClearData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchMenuExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "تخصيص تجربة التصفح",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إعدادات الأمان، المظهر، ومحركات البحث",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Search Engine Selection
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "محرك البحث الافتراضي",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = searchMenuExpanded,
                    onExpandedChange = { searchMenuExpanded = !searchMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = settings.searchEngine.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = searchMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = searchMenuExpanded,
                        onDismissRequest = { searchMenuExpanded = false }
                    ) {
                        SearchEngine.values().forEach { engine ->
                            DropdownMenuItem(
                                text = { Text(engine.displayName) },
                                onClick = {
                                    onUpdateSettings { it.copy(searchEngine = engine) }
                                    searchMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Privacy & Protection settings
            SettingsSwitchItem(
                icon = Icons.Default.Security,
                title = "درع حظر الإعلانات والمتعقبات",
                subtitle = "تسريع تحميل المواقع ومنع جمع بياناتك الحساسة",
                checked = settings.isAdBlockerEnabled,
                onCheckedChange = { value ->
                    onUpdateSettings { it.copy(isAdBlockerEnabled = value) }
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Security,
                title = "إرسال ترويسة عدم التعقب (Do Not Track)",
                subtitle = "طلب عدم تعقب نشاطك من خوادم الويب",
                checked = settings.isDoNotTrackEnabled,
                onCheckedChange = { value ->
                    onUpdateSettings { it.copy(isDoNotTrackEnabled = value) }
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "الوضع الليلي العام (Dark Mode)",
                subtitle = "تفعيل المظهر الداكن تلقائياً لجميع التبويبات والمواقع",
                checked = settings.isNightModeGlobal,
                onCheckedChange = { value ->
                    onUpdateSettings { it.copy(isNightModeGlobal = value) }
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Laptop,
                title = "عرض إصدار سطح المكتب افتراضياً",
                subtitle = "فتح المواقع مثل أجهزة الكمبيوتر",
                checked = settings.isDesktopModeGlobal,
                onCheckedChange = { value ->
                    onUpdateSettings { it.copy(isDesktopModeGlobal = value) }
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.CleaningServices,
                title = "مسح البيانات تلقائياً عند إغلاق التطبيق",
                subtitle = "حذف الكوكيز والسجل المؤقت لحماية خصوصيتك عند الخروج",
                checked = settings.clearOnExit,
                onCheckedChange = { value ->
                    onUpdateSettings { it.copy(clearOnExit = value) }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Clear data action
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable { onOpenClearData() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "مسح بيانات التصفح والكوكيز",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "حذف ملفات تعريف الارتباط والذاكرة المؤقتة لضمان الخصوصية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

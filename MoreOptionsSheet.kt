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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TabModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsSheet(
    tab: TabModel?,
    isBookmarked: Boolean,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSaveOffline: () -> Unit,
    onOpenReaderMode: () -> Unit,
    onToggleNightMode: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onOpenOfflinePages: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPrivacyShield: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "خيارات التصفح والمزيد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "متصفح TOOL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Quick actions
            MenuActionItem(
                icon = Icons.Default.Add,
                title = "تبويب جديد",
                subtitle = "فتح صفحة جديدة",
                onClick = { onNewTab(); onDismiss() }
            )

            MenuActionItem(
                icon = Icons.Default.VisibilityOff,
                title = "تبويب تصفح خفي جديد",
                subtitle = "تصفح بخصوصية كاملة وبدون حفظ سجلات",
                iconTint = Color(0xFFA78BFA),
                onClick = { onNewIncognitoTab(); onDismiss() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Reader & Offline Saving
            MenuActionItem(
                icon = Icons.Default.AutoStories,
                title = "وضع القراءة المريح",
                subtitle = "عرض مريح للنصوص بدون إعلانات وتشتيت",
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = { onOpenReaderMode(); onDismiss() }
            )

            MenuActionItem(
                icon = Icons.Default.SaveAlt,
                title = "حفظ الصفحة للقراءة دون إنترنت",
                subtitle = "تخزين المحتوى لقراءته في أي وقت بدون شبكة",
                iconTint = Color(0xFF10B981),
                onClick = { onSaveOffline(); onDismiss() }
            )

            MenuActionItem(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                title = if (isBookmarked) "إزالة من الإشارات المرجعية" else "إضافة إلى الإشارات المرجعية",
                subtitle = "حفظ الرابط في المفضلة",
                iconTint = if (isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface,
                onClick = { onToggleBookmark(); onDismiss() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Toggles: Night Mode and Desktop Mode
            MenuToggleItem(
                icon = Icons.Default.DarkMode,
                title = "الوضع الليلي للمواقع",
                subtitle = "تحويل ألوان المواقع لراحة العين في الظلام",
                checked = tab?.isNightMode == true,
                onCheckedChange = { onToggleNightMode() }
            )

            MenuToggleItem(
                icon = if (tab?.isDesktopMode == true) Icons.Default.Laptop else Icons.Default.Smartphone,
                title = "إصدار سطح المكتب (كمبيوتر)",
                subtitle = "طلب الموقع كجهاز كمبيوتر مكتبي",
                checked = tab?.isDesktopMode == true,
                onCheckedChange = { onToggleDesktopMode() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Managers: Downloads, Offline Pages, Bookmarks, History
            MenuActionItem(
                icon = Icons.Default.Download,
                title = "مدير التحميلات المتطور",
                subtitle = "عرض وتنزيل الملفات وتتبع السرعة والحالة",
                onClick = { onOpenDownloads(); onDismiss() }
            )

            MenuActionItem(
                icon = Icons.Default.CloudDownload,
                title = "الصفحات المحفوظة (أوفلاين)",
                subtitle = "تصفح المقالات والصفحات المخزنة محلياً",
                onClick = { onOpenOfflinePages(); onDismiss() }
            )

            MenuActionItem(
                icon = Icons.Default.Bookmark,
                title = "الإشارات المرجعية والمفضلة",
                subtitle = "المواقع المفضلة المحفوظة لديك",
                onClick = { onOpenBookmarks(); onDismiss() }
            )

            MenuActionItem(
                icon = Icons.Default.History,
                title = "سجل التصفح",
                subtitle = "المواقع والصفحات التي قمت بزيارتها",
                onClick = { onOpenHistory(); onDismiss() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Privacy & Settings
            MenuActionItem(
                icon = Icons.Default.Security,
                title = "حماية الخصوصية والأمان",
                subtitle = "حظر المتعقبات والإعلانات وشهادة SSL",
                iconTint = Color(0xFF10B981),
                onClick = { onOpenPrivacyShield(); onDismiss() }
            )

            MenuActionItem(
                icon = Icons.Default.CleaningServices,
                title = "مسح بيانات التصفح",
                subtitle = "حذف ملفات تعريف الارتباط وسجل التصفح والذاكرة المؤقتة",
                iconTint = MaterialTheme.colorScheme.error,
                onClick = { onClearData(); onDismiss() }
            )

            MenuActionItem(
                icon = Icons.Default.Settings,
                title = "الإعدادات وتخصيص المتصفح",
                subtitle = "محرك البحث، حجم الخط، إعدادات الخصوصية",
                onClick = { onOpenSettings(); onDismiss() }
            )
        }
    }
}

@Composable
private fun MenuActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
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
    }
}

@Composable
private fun MenuToggleItem(
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
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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

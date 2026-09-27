package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ClearDataDialog(
    onConfirm: (cookies: Boolean, cache: Boolean, history: Boolean, storage: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var clearCookies by remember { mutableStateOf(true) }
    var clearCache by remember { mutableStateOf(true) }
    var clearHistory by remember { mutableStateOf(true) }
    var clearStorage by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مسح بيانات التصفح والحفاظ على الخصوصية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ClearOptionRow(
                    label = "ملفات تعريف الارتباط (Cookies)",
                    checked = clearCookies,
                    onCheckedChange = { clearCookies = it }
                )
                ClearOptionRow(
                    label = "الذاكرة المؤقتة وملفات التخزين المؤقت",
                    checked = clearCache,
                    onCheckedChange = { clearCache = it }
                )
                ClearOptionRow(
                    label = "سجل المواقع والصفحات التي تمت زيارتها",
                    checked = clearHistory,
                    onCheckedChange = { clearHistory = it }
                )
                ClearOptionRow(
                    label = "بيانات المواقع المخزنة محلياً (Web Storage)",
                    checked = clearStorage,
                    onCheckedChange = { clearStorage = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(clearCookies, clearCache, clearHistory, clearStorage)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("مسح الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun ClearOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

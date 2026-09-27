package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TabModel

@Composable
fun CurvedTopBar(
    tab: TabModel?,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onOpenReader: () -> Unit,
    onOpenPrivacyShield: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                clip = false
            ),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        color = if (tab?.isIncognito == true) Color(0xFF1B1B2F) else MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Privacy Shield & SSL lock badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenPrivacyShield() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("privacy_shield_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (tab != null && tab.blockedTrackersCount > 0) {
                                Badge(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "${tab.blockedTrackersCount}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (tab?.isSecure == true) Icons.Default.Lock else Icons.Default.Shield,
                            contentDescription = "حماية الخصوصية",
                            tint = if (tab?.isIncognito == true) Color(0xFFA78BFA)
                            else if (tab?.isSecure == true) Color(0xFF10B981)
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (tab?.isIncognito == true) {
                        Text(
                            text = "تصفح خفي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA78BFA)
                        )
                    }
                }

                // Page Title & URL domain
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (tab?.url == "tool://home") "متصفح TOOL" else (tab?.title?.ifBlank { "تحميل..." } ?: "متصفح TOOL"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (tab?.isIncognito == true) Color.White else MaterialTheme.colorScheme.onSurface
                    )

                    val domain = if (tab?.url == "tool://home") "صفحة البداية الذكية"
                    else tab?.url?.replace("https://", "")?.replace("http://", "")?.take(32) ?: ""

                    if (domain.isNotEmpty()) {
                        Text(
                            text = domain,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (tab?.isIncognito == true) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action Buttons: Reader Mode & Reload/Stop
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reader Mode Quick Access Button
                    IconButton(
                        onClick = onOpenReader,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reader_mode_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (tab?.readerContent != null) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = "وضع القراءة المريح",
                                tint = if (tab?.readerContent != null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Reload or Stop
                    IconButton(
                        onClick = {
                            if (tab?.isLoading == true) onStop() else onReload()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reload_stop_button")
                    ) {
                        Icon(
                            imageVector = if (tab?.isLoading == true) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = if (tab?.isLoading == true) "إيقاف" else "تحديث",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Progress bar along the curved bottom edge
            AnimatedVisibility(
                visible = tab?.isLoading == true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { (tab?.progress ?: 0) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    color = if (tab?.isIncognito == true) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

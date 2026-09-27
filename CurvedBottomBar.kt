package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun CurvedBottomBar(
    tab: TabModel?,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onSearchClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                clip = false
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = if (tab?.isIncognito == true) Color(0xFF141426) else MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                enabled = tab?.canGoBack == true,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "للخلف",
                    tint = if (tab?.canGoBack == true) {
                        if (tab.isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            // Forward Button
            IconButton(
                onClick = onForward,
                enabled = tab?.canGoForward == true,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "للأمام",
                    tint = if (tab?.canGoForward == true) {
                        if (tab.isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            // Central Search / Address Bar Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(
                        if (tab?.isIncognito == true) Color(0xFF23233E)
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    .border(
                        width = 1.dp,
                        color = if (tab?.isIncognito == true) Color(0xFF4C1D95).copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(23.dp)
                    )
                    .clickable { onSearchClick() }
                    .padding(horizontal = 14.dp)
                    .testTag("bottom_search_bar"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (tab?.url == "tool://home") Icons.Default.Search else Icons.Default.Language,
                        contentDescription = "بحث",
                        tint = if (tab?.isIncognito == true) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val displayText = if (tab?.url == "tool://home" || tab?.url.isNullOrBlank()) {
                        "بحث أو إدخال عنوان..."
                    } else {
                        tab?.url?.replace("https://", "")?.replace("http://", "") ?: ""
                    }

                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (tab?.url == "tool://home" || tab?.url.isNullOrBlank()) {
                            if (tab?.isIncognito == true) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            if (tab?.isIncognito == true) Color.White else MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tabs Switcher Button
            IconButton(
                onClick = onTabsClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("tabs_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (tab?.isIncognito == true) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = "$tabCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tab?.isIncognito == true) Color(0xFFA78BFA) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Three Dots Menu Button ("...")
            IconButton(
                onClick = onMoreOptionsClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("more_options_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "خيارات إضافية",
                    tint = if (tab?.isIncognito == true) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

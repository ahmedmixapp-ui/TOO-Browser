package com.example.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReaderContent
import com.example.data.model.ReaderFont
import com.example.data.model.ReaderTheme
import java.util.Locale

@Composable
fun ReaderModeView(
    readerContent: ReaderContent?,
    onSaveOffline: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(ReaderTheme.WARM_SEPIA) }
    var fontSize by remember { mutableStateOf(19) }
    var selectedFont by remember { mutableStateOf(ReaderFont.SERIF) }
    var isSpeaking by remember { mutableStateOf(false) }

    // TTS engine for reading out loud
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        val listener = object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId?.endsWith("_last") == true || utteranceId == "tool_reader") {
                    isSpeaking = false
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                isSpeaking = false
            }
        }

        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setOnUtteranceProgressListener(listener)
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val backgroundColor = Color(selectedTheme.bgLightHex)
    val textColor = Color(selectedTheme.textLightHex)
    val fontFamily = if (selectedFont == ReaderFont.SERIF) FontFamily.Serif else FontFamily.SansSerif

    Surface(
        modifier = modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Reader Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        tts?.stop()
                        onClose()
                    },
                    modifier = Modifier.testTag("close_reader_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق وضع القراءة",
                        tint = textColor
                    )
                }

                Text(
                    text = "وضع القراءة المريح",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // TTS Audio Playback
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                tts?.stop()
                                isSpeaking = false
                            } else {
                                val textToRead = readerContent?.fullText ?: ""
                                if (textToRead.isNotBlank()) {
                                    val maxLen = TextToSpeech.getMaxSpeechInputLength().coerceIn(1000, 3900)
                                    val chunks = textToRead.chunked(maxLen)
                                    chunks.forEachIndexed { index, chunk ->
                                        val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                                        val utteranceId = if (index == chunks.lastIndex) "tool_reader_last" else "tool_reader_$index"
                                        tts?.speak(chunk, queueMode, null, utteranceId)
                                    }
                                    isSpeaking = true
                                }
                            }
                        },
                        modifier = Modifier.testTag("tts_reader_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = "الاستماع للمقال",
                            tint = textColor
                        )
                    }

                    // Save offline button
                    IconButton(
                        onClick = onSaveOffline,
                        modifier = Modifier.testTag("save_offline_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "حفظ أوفلاين",
                            tint = textColor
                        )
                    }
                }
            }

            // Reader Customization Bar (Theme pills, Font size stepper, Font family toggle)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                color = textColor.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Themes selector
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReaderTheme.values().forEach { theme ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(theme.bgLightHex))
                                    .border(
                                        width = if (selectedTheme == theme) 2.5.dp else 1.dp,
                                        color = if (selectedTheme == theme) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedTheme = theme }
                            )
                        }
                    }

                    // Font Size: A- / A+
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { if (fontSize > 14) fontSize -= 2 }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("A-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }

                        Text("${fontSize}sp", fontSize = 12.sp, color = textColor.copy(alpha = 0.8f))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { if (fontSize < 28) fontSize += 2 }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("A+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }

                    // Font toggle (Serif vs Sans)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(textColor.copy(alpha = 0.12f))
                            .clickable {
                                selectedFont = if (selectedFont == ReaderFont.SERIF) ReaderFont.SANS else ReaderFont.SERIF
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (selectedFont == ReaderFont.SERIF) "Serif" else "Sans",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }

            HorizontalDivider(
                color = textColor.copy(alpha = 0.15f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Article Content
            if (readerContent == null || (readerContent.paragraphs.isEmpty() && readerContent.fullText.isBlank())) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جاري استخراج محتوى المقال أو أن الصفحة لا تحتوي على نص كافٍ...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Title
                    Text(
                        text = readerContent.title,
                        fontSize = (fontSize + 6).sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = fontFamily,
                        color = textColor,
                        lineHeight = (fontSize + 12).sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meta: Author & Read time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (readerContent.author.isNotBlank()) {
                            Text(
                                text = "الكاتب: ${readerContent.author}",
                                fontSize = 13.sp,
                                color = textColor.copy(alpha = 0.7f),
                                fontFamily = fontFamily
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${readerContent.readingTimeMinutes} دقائق قراءة",
                                fontSize = 13.sp,
                                color = textColor.copy(alpha = 0.7f),
                                fontFamily = fontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Paragraphs
                    val paragraphs = if (readerContent.paragraphs.isNotEmpty()) {
                        readerContent.paragraphs
                    } else {
                        readerContent.fullText.split("\n\n")
                    }

                    paragraphs.forEach { p ->
                        Text(
                            text = p,
                            fontSize = fontSize.sp,
                            fontFamily = fontFamily,
                            lineHeight = (fontSize * 1.6f).sp,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

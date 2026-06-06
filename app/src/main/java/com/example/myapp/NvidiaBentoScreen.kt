package com.example.myapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Color Palette - Vanguard OS (Industrial Premium)
private val NvidiaBlack = Color(0xFF050505)
private val SurfaceDark = Color(0xFF0D0D0D)
private val CardUserSurface = Color(0xFF121212)
private val BorderMuted = Color(0xFF1E1E1E)
private val NvidiaGreen = Color(0xFF76B900)
private val NeonCyan = Color(0xFF00F0FF)
private val GlassBackground = Color(0xCC000000)

@Composable
fun NvidiaBentoScreen(viewModel: NvidiaViewModel = viewModel()) {
    val listState = rememberLazyListState()
    val messages = viewModel.messages
    
    // Auto-scroll logic: Seamlessly animate to bottom as tokens flow
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Secondary trigger for streaming content updates
    val lastMessageContent = messages.lastOrNull()?.content ?: ""
    LaunchedEffect(lastMessageContent) {
        if (messages.isNotEmpty() && !messages.last().isUser) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = NvidiaBlack,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                GlassmorphicCommandBar(
                    value = viewModel.promptInput,
                    onValueChange = { viewModel.promptInput = it },
                    onSend = { viewModel.onSendClicked() },
                    enabled = !viewModel.loadingState && viewModel.apiKey.isNotBlank(),
                    isLoading = viewModel.loadingState
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        WelcomeBentoLayer()
                    }
                } else {
                    items(messages, key = { it.id }) { message ->
                        if (message.isUser) {
                            UserBentoLayer(message)
                        } else {
                            AiBentoLayer(message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeBentoLayer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, NvidiaGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "SYSTEM ONLINE",
                color = NvidiaGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vanguard OS initialized. Awaiting secure command protocol. All systems nominal.",
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun UserBentoLayer(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(CardUserSurface)
                .border(1.dp, BorderMuted, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = message.content,
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AiBentoLayer(message: ChatMessage) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(SurfaceDark)
                .drawBehind {
                    // Striking Neon Left-Accent Line
                    drawLine(
                        color = NeonCyan,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 4.dp.toPx()
                    )
                }
                .border(
                    width = 1.dp,
                    color = BorderMuted,
                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .padding(start = 4.dp) // Offset for the neon line
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (message.content.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(NeonCyan)
                                    .alpha(pulseAlpha)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROCESSING...",
                                color = NeonCyan.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        SelectionContainer {
                            Text(
                                text = message.content,
                                color = Color.White.copy(alpha = 0.95f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicCommandBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 24.dp)) // Asymmetrical
            .background(GlassBackground)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 24.dp)
            )
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    "INITIALIZE CORE...", 
                    color = Color.White.copy(alpha = 0.3f), 
                    fontSize = 12.sp, 
                    fontFamily = FontFamily.Monospace 
                ) 
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp, 
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        )

        Button(
            onClick = onSend,
            enabled = enabled,
            modifier = Modifier
                .fillMaxHeight()
                .width(90.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = NvidiaGreen,
                contentColor = NvidiaBlack,
                disabledContainerColor = Color(0xFF222222),
                disabledContentColor = Color.Gray
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = NvidiaBlack,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "EXEC",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

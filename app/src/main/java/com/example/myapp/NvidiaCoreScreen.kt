package com.example.myapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NvidiaCoreScreen(viewModel: NvidiaViewModel = viewModel()) {
    var visible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    // Entrance Sequence
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = VanguardDark
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. COMMAND HEADER
            VanguardHeader(visible)

            // 2. SYSTEM_CONFIGURATION BENTO (New Config Card)
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, 200)) + slideInVertically(tween(800, 200)) { it / 2 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VanguardAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = VanguardSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "SYSTEM_CONFIGURATION // ACCESS_LAYER",
                            color = VanguardAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        OutlinedTextField(
                            value = viewModel.apiKey,
                            onValueChange = { viewModel.updateApiKey(it) },
                            label = { Text("API_KEY", color = VanguardGray, fontSize = 10.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VanguardAccent,
                                unfocusedBorderColor = VanguardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = viewModel.selectedModel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("TARGET_MODEL", color = VanguardGray, fontSize = 10.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VanguardAccent,
                                    unfocusedBorderColor = VanguardBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .background(VanguardSurface)
                                    .border(1.dp, VanguardBorder)
                                    .exposedDropdownSize()
                            ) {
                                viewModel.availableModels.value.forEach { model ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = model, 
                                                color = Color.White,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.onModelSelected(model)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. PRIMARY METRICS BENTO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "UPLINK_STABILITY",
                    value = "99.8%",
                    modifier = Modifier.weight(1.2f),
                    delay = 300,
                    visible = visible
                )
                MetricCard(
                    title = "CORE_TEMP",
                    value = "42°C",
                    modifier = Modifier.weight(0.8f),
                    delay = 450,
                    visible = visible,
                    isWarning = true
                )
            }

            // LIVE TELEMETRY METRICS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "RAM_USAGE",
                    value = viewModel.currentRam.value,
                    modifier = Modifier.weight(1f),
                    delay = 500,
                    visible = visible
                )
                MetricCard(
                    title = "SYSTEM_TPS",
                    value = viewModel.currentTps.value,
                    modifier = Modifier.weight(1f),
                    delay = 550,
                    visible = visible
                )
            }

            // 4. SYSTEM TERMINAL (Telemetry)
            SystemTerminalCard(visible, delay = 600)
        }
    }
}

@Composable
fun VanguardHeader(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(1000)) + expandVertically(tween(800))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(listOf(Color.Transparent, VanguardAccent.copy(0.05f)))
                    )
            )

            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = "OPERATOR_ID",
                    color = VanguardAccent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "VANGUARD_01",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = VanguardAccent,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.size(width = 40.dp, height = 4.dp)
                ) {}
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    delay: Int,
    visible: Boolean,
    isWarning: Boolean = false
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(800, delay)) { it / 2 } + fadeIn(tween(800, delay)),
        modifier = modifier
    ) {
        Surface(
            color = VanguardSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isWarning) Color.Red.copy(0.3f) else VanguardBorder),
            modifier = Modifier.height(100.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = VanguardGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = value,
                    color = if (isWarning) Color.Red else Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SystemTerminalCard(visible: Boolean, delay: Int) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(1000, delay)) + scaleIn(tween(1000, delay), initialScale = 0.95f)
    ) {
        Surface(
            color = VanguardSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, VanguardBorder),
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(VanguardAccent, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE_SYSTEM_TELEMETRY",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val logs = listOf(
                    "> INITIALIZING NEURAL_LINK...",
                    "> HANDSHAKE SUCCESSFUL [PORT:8080]",
                    "> DECRYPTING CORE_DATA... 100%",
                    "> SYSTEM_STATUS: OPTIMAL"
                )
                
                logs.forEachIndexed { index, log ->
                    Text(
                        text = log,
                        color = VanguardAccent.copy(alpha = 1f - (index * 0.2f)),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

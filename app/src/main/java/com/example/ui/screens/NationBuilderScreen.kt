package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VikasScheme
import com.example.ui.GameViewModel
import java.util.Locale

@Composable
fun NationBuilderScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val coins by viewModel.coins.collectAsState()
    val vikasPoints by viewModel.vikasPoints.collectAsState()
    val schemes by viewModel.schemes.collectAsState()
    val rallyFeverTime by viewModel.rallyFeverTime.collectAsState()

    val totalVPS = schemes.sumOf { it.currentVPS }
    val effectiveVPS = if (rallyFeverTime > 0) totalVPS * 2 else totalVPS

    // Calculate simulated GDP based on Vikas Points
    val calculatedGDP = 3.8 + (vikasPoints / 10000.0).coerceAtMost(26.2)
    val gdpProgress = ((calculatedGDP - 3.8) / (10.0 - 3.8)).toFloat().coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "rally_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7F2))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier.testTag("back_from_nation_builder")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0D233A)
                )
            }

            Text(
                text = "NATION BUILDER",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD84315),
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0D253F),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "🪙 $coins",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // GDP & VPS Dashboard Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D233A))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "INDIA GDP MILESTONE",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.2f Trillion", calculatedGDP),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Growth",
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress towards $10T Viksit Bharat
                        LinearProgressIndicator(
                            progress = { gdpProgress },
                            color = Color(0xFFFF9933),
                            trackColor = Color(0xFF263238),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current: $3.8T", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Text("Goal: $10T Viksit Bharat", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Ticker metrics
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF142C47))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Vikas Points", color = Color(0xFF90A4AE), fontSize = 11.sp)
                                Text("$vikasPoints VP", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFF2E4C6E)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("VPS (Output)", color = Color(0xFF90A4AE), fontSize = 11.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = if (rallyFeverTime > 0) Color(0xFFFF3D00) else Color(0xFF81C784),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+$effectiveVPS/s",
                                        color = if (rallyFeverTime > 0) Color(0xFFFF5722) else Color(0xFF81C784),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Rally Speech ("Mitron! Address the Nation!")
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (rallyFeverTime > 0) pulseScale else 1.0f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = if (rallyFeverTime > 0) 3.dp else 1.dp,
                            color = if (rallyFeverTime > 0) Color(0xFFFF9100) else Color(0xFFFFCC80),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.triggerRallySpeech() }
                        .testTag("rally_speech_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rallyFeverTime > 0) Color(0xFFFFF3E0) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (rallyFeverTime > 0) Color(0xFFFF6F00) else Color(0xFFD84315),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = "Rally",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (rallyFeverTime > 0) "2X VIKAS FEVER ACTIVE! 🔥" else "MITRON! ADDRESS RALLY",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (rallyFeverTime > 0) Color(0xFFD84315) else Color(0xFF0D233A)
                                )
                                Text(
                                    text = if (rallyFeverTime > 0)
                                        "Time remaining: ${rallyFeverTime}s (+2x Output)"
                                    else
                                        "Tap to inspire crowd & 2X VPS boost!",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                        Text(
                            text = if (rallyFeverTime > 0) "🔥" else "📢",
                            fontSize = 24.sp
                        )
                    }
                }
            }

            // Schemes Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "National Development Schemes",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF0D233A)
                    )
                    Text(
                        text = "8 Schemes",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Schemes List
            items(schemes, key = { it.id }) { scheme ->
                SchemeUpgradeCard(
                    scheme = scheme,
                    canAfford = vikasPoints >= scheme.currentCost,
                    onUpgrade = { viewModel.upgradeScheme(scheme.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SchemeUpgradeCard(
    scheme: VikasScheme,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = scheme.iconEmoji, fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = scheme.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0D233A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.padding(1.dp)
                        ) {
                            Text(
                                text = "Lvl ${scheme.level}",
                                color = Color(0xFF1B5E20),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${scheme.hindiName} • +${scheme.currentVPS} VP/s",
                        fontSize = 12.sp,
                        color = Color(0xFF1B8A2F),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = scheme.tagLine,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Upgrade Button
            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F00),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("upgrade_${scheme.id}_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "UPGRADE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (canAfford) Color.White else Color.Gray
                    )
                    Text(
                        text = "${scheme.currentCost} VP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color(0xFFFFD54F) else Color.DarkGray
                    )
                }
            }
        }
    }
}

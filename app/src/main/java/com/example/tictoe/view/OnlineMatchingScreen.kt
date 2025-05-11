package com.example.tictoe.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Define element data classes outside the composable function
private data class FloatingElementSpec(
    val size: Dp,
    val floatAnimSpec: InfiniteRepeatableSpec<Float>,
    val alphaAnimSpec: InfiniteRepeatableSpec<Float>,
    val offset: Offset,
    val index: Int
)

private data class FloatingElementAnim(
    val size: Dp,
    val floatAnim: Float,
    val alpha: Float,
    val offset: Offset
)

@Composable
fun OnlineMatchingScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onMatchFound: (String) -> Unit
) {
    // States for animations and dialogs
    var isSearching by remember { mutableStateOf(true) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    // Rotating animation for the searching icon
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "rotationAngle"
    )
    
    // Dots typing animation
    val dotCount by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 3,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotCount"
    )
    
    // Animated gradient background
    val gradientColors = listOf(
        Color(0xFF1E124D),
        Color(0xFF2D1863),
        Color(0xFF3A256A),
        Color(0xFF2D1863),
        Color(0xFF1E124D)
    )
    
    val transition = rememberInfiniteTransition(label = "backgroundTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translateAnim"
    )
    
    // Glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // Create a list of transition objects outside of remember block
    val elementTransitions = List(8) { 
        rememberInfiniteTransition(label = "floatingElement$it") 
    }
    
    // Staggered floating elements with explicit data class
    val floatingElements = remember {
        List(8) {
            val randomSize = Random.nextFloat() * 40f + 10f
            val randomDuration = Random.nextInt(4000, 8000)
            val randomStartPhase = Random.nextFloat()
            
            FloatingElementSpec(
                size = randomSize.dp,
                floatAnimSpec = infiniteRepeatable(
                    animation = tween(randomDuration, easing = FastOutSlowInEasing, 
                                     delayMillis = (randomStartPhase * randomDuration).toInt()),
                    repeatMode = RepeatMode.Reverse
                ),
                alphaAnimSpec = infiniteRepeatable(
                    animation = tween(randomDuration, easing = FastOutSlowInEasing, 
                                     delayMillis = (randomStartPhase * randomDuration).toInt()),
                    repeatMode = RepeatMode.Reverse
                ),
                offset = Offset(
                    x = Random.nextFloat() * 1f,
                    y = Random.nextFloat() * 1f
                ),
                index = it
            )
        }
    }
    
    // Create animations from specs with explicit data class
    val floatingElementAnimations = floatingElements.map { element ->
        val floatAnim by elementTransitions[element.index].animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = element.floatAnimSpec,
            label = "floatAnim${element.index}"
        )
        
        val alpha by elementTransitions[element.index].animateFloat(
            initialValue = 0.05f,
            targetValue = 0.15f,
            animationSpec = element.alphaAnimSpec,
            label = "alphaAnim${element.index}"
        )
        
        FloatingElementAnim(
            size = element.size,
            floatAnim = floatAnim,
            alpha = alpha,
            offset = element.offset
        )
    }
    
    // Background with animated gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = translateAnim,
                    endY = translateAnim + 1000f
                )
            )
    ) {
        // Floating background elements
        floatingElementAnimations.forEach { element ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (element.offset.x * 1000 - 200).dp,
                        y = (element.offset.y * 1000 - 200 + element.floatAnim * 40).dp
                    )
                    .size(element.size)
                    .alpha(element.alpha)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFF4EE6FA)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4EE6FA).copy(alpha = 0.7f),
                                Color(0xFF4EE6FA).copy(alpha = 0.0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Background decorative elements - glowing circles
        Box(
            modifier = Modifier
                .size(280.dp)
                .alpha(0.15f)
                .offset(x = (-140).dp, y = (-140).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD600).copy(alpha = 0.7f),
                            Color(0xFFFFD600).copy(alpha = 0.0f)
                        )
                    ),
                    shape = CircleShape
                )
                .align(Alignment.TopStart)
        )
        
        Box(
            modifier = Modifier
                .size(320.dp)
                .alpha(0.12f)
                .offset(x = 160.dp, y = 160.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4EE6FA).copy(alpha = 0.7f),
                            Color(0xFF4EE6FA).copy(alpha = 0.0f)
                        )
                    ),
                    shape = CircleShape
                )
                .align(Alignment.BottomEnd)
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated glow around title
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .alpha(glowAlpha)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    spotColor = Color(0xFFFFD600)
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD600).copy(alpha = 0.7f),
                                            Color(0xFFFFD600).copy(alpha = 0.0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        Text(
                            text = "T",
                            color = Color(0xFFFFD600),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFFFFD600).copy(alpha = 0.6f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                    
                    Text(
                        text = "ic tac Toe",
                        color = Color(0xFFFFD600),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFFFFD600).copy(alpha = 0.4f),
                                offset = Offset(0f, 0f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
                
                // Glass morphism effect for the online indicator
                Card(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0xFF4E3A8C).copy(alpha = 0.3f),
                    elevation = 0.dp,
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .blur(radius = 0.5.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Pulsing dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(pulseScale)
                                .background(Color(0xFF4EE6FA), CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4EE6FA).copy(alpha = 0.7f),
                                            Color(0xFF4EE6FA).copy(alpha = 0.0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "ONLINE",
                            color = Color(0xFF4EE6FA),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Main card with glass morphism effect
            Card(
                shape = RoundedCornerShape(30.dp),
                backgroundColor = Color(0xFF332058).copy(alpha = 0.65f),
                elevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(30.dp),
                        spotColor = Color(0xFFFFD600).copy(alpha = 0.5f)
                    )
                    .blur(radius = 0.5.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD600).copy(alpha = 0.7f),
                                Color(0xFFFFD600).copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    // Animated search icon with layers for depth
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale)
                    ) {
                        // Outer glow layer
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .alpha(glowAlpha * 0.3f)
                                .shadow(
                                    elevation = 30.dp,
                                    shape = CircleShape,
                                    spotColor = Color(0xFF4EE6FA)
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4EE6FA).copy(alpha = 0.3f),
                                            Color(0xFF4EE6FA).copy(alpha = 0.0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // Middle glow layer
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .alpha(glowAlpha * 0.5f)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = CircleShape,
                                    spotColor = Color(0xFF4EE6FA)
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4EE6FA).copy(alpha = 0.5f),
                                            Color(0xFF4EE6FA).copy(alpha = 0.0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // Inner circle
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    spotColor = Color(0xFF4EE6FA)
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4E3A8C),
                                            Color(0xFF332058)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF4EE6FA).copy(alpha = 0.8f),
                                            Color(0xFF4EE6FA).copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Rotating search icon
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF4EE6FA),
                                modifier = Modifier
                                    .size(50.dp)
                                    .graphicsLayer { 
                                        rotationZ = rotationAngle
                                    }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    // Searching text with animated dots
                    Text(
                        text = "Searching for opponent" + ".".repeat(dotCount),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFF4EE6FA).copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 4f
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Cancel button with glow effect
                    Button(
                        onClick = { showCancelConfirmation = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFFFD600)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        ),
                        modifier = Modifier
                            .width(160.dp)
                            .height(52.dp)
                            .shadow(
                                elevation = 15.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = Color(0xFFFFD600).copy(alpha = 0.5f)
                            )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF2D1863),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Exit button with glass effect
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF4EE6FA).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0xFF4EE6FA).copy(alpha = 0.5f)
                    )
                    .blur(radius = 0.2.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFF2D1863),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Exit",
                        color = Color(0xFF2D1863),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Matching player simulation
        LaunchedEffect(key1 = isSearching) {
            if (isSearching) {
                delay(5000) // Simulate finding a match after 5 seconds
                onMatchFound("Player123")
            }
        }
        
        // Cancel confirmation dialog with glass morphism
        if (showCancelConfirmation) {
            Dialog(onDismissRequest = { showCancelConfirmation = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Backdrop blur effect
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.15f)
                            .shadow(
                                elevation = 15.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color(0xFF4EE6FA).copy(alpha = 0.5f)
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF4EE6FA).copy(alpha = 0.7f),
                                        Color(0xFF4EE6FA).copy(alpha = 0.0f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    )
                    
                    // Glass card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0xFF332058).copy(alpha = 0.85f),
                        elevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .blur(radius = 0.3.dp)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4EE6FA).copy(alpha = 0.8f),
                                        Color(0xFF4EE6FA).copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Cancel Search?",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color(0xFF4EE6FA).copy(alpha = 0.5f),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                text = "Are you sure you want to stop searching for an opponent?",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // No button - outline glass effect
                                OutlinedButton(
                                    onClick = { showCancelConfirmation = false },
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF4EE6FA).copy(alpha = 0.8f),
                                                Color(0xFF4EE6FA).copy(alpha = 0.2f)
                                            )
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        backgroundColor = Color.Transparent,
                                        contentColor = Color(0xFF4EE6FA)
                                    ),
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(52.dp)
                                ) {
                                    Text(
                                        text = "No",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color(0xFF4EE6FA).copy(alpha = 0.5f),
                                                offset = Offset(0f, 0f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                }
                                
                                // Yes button - solid with glow
                                Button(
                                    onClick = {
                                        showCancelConfirmation = false
                                        isSearching = false
                                        onCancel()
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color(0xFF4EE6FA)
                                    ),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 10.dp
                                    ),
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(52.dp)
                                        .shadow(
                                            elevation = 10.dp,
                                            shape = RoundedCornerShape(24.dp),
                                            spotColor = Color(0xFF4EE6FA).copy(alpha = 0.5f)
                                        )
                                ) {
                                    Text(
                                        text = "Yes",
                                        color = Color(0xFF2D1863),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnlineMatchingScreenPreview() {
    OnlineMatchingScreen(
        onBack = {},
        onCancel = {},
        onMatchFound = {}
    )
} 
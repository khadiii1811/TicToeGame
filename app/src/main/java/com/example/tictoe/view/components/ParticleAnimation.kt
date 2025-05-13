package com.example.tictoe.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

// Define ParticleData class
data class ParticleData(
    val xPos: Float, 
    val yPos: Float, 
    val particleSize: Float, 
    val alphaValue: Float, 
    val xSpeed: Float, 
    val ySpeed: Float
)

@Composable
fun rememberParticles(count: Int = 16): List<MutableState<ParticleData>> {
    return remember { 
        List(count) { 
            mutableStateOf(createRandomParticle())
        }
    }
}

fun createRandomParticle(): ParticleData {
    return ParticleData(
        xPos = Random.nextFloat() * 400 - 20,
        yPos = Random.nextFloat() * 800 - 20, 
        particleSize = Random.nextFloat() * 5 + 3,
        alphaValue = Random.nextFloat() * 0.15f + 0.05f,
        xSpeed = Random.nextFloat() * 1f - 0.5f,
        ySpeed = Random.nextFloat() * 0.4f - 0.2f
    )
}

fun updateParticle(particle: ParticleData): ParticleData {
    // Move particle and check if out of bounds
    val newX = particle.xPos + particle.xSpeed
    val newY = particle.yPos + particle.ySpeed
    
    // If particle goes out of bounds, reset it
    return if (newX < -50 || newX > 500 || newY < -50 || newY > 1000) {
        createRandomParticle()
    } else {
        particle.copy(xPos = newX, yPos = newY)
    }
}

@Composable
fun ParticleEffect(particle: ParticleData) {
    Box(
        modifier = Modifier
            .size(particle.particleSize.dp)
            .alpha(particle.alphaValue)
            .offset(x = particle.xPos.dp, y = particle.yPos.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD600).copy(alpha = 0.6f),
                        Color(0xFF4EE6FA).copy(alpha = 0.2f)
                    )
                ),
                shape = CircleShape
            )
    )
} 
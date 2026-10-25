package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural low-latency arcade audio synthesizer for Modi Run.
 * Generates custom chime tones for jumps, coin grabs, power-ups, speeches, and crashes.
 */
class SoundManager(private val isSoundEnabled: () -> Boolean) {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    fun playJump() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            playToneSweep(startFreq = 320.0, endFreq = 640.0, durationMs = 120, volume = 0.5f)
        }
    }

    fun playDoubleJump() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            playToneSweep(startFreq = 480.0, endFreq = 960.0, durationMs = 150, volume = 0.55f)
        }
    }

    fun playCoin() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            playTone(frequency = 987.77, durationMs = 60, volume = 0.5f) // B5
            playTone(frequency = 1318.51, durationMs = 90, volume = 0.6f) // E6
        }
    }

    fun playBroomClean() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            playToneSweep(startFreq = 500.0, endFreq = 850.0, durationMs = 100, volume = 0.5f)
        }
    }

    fun playPowerUp() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            // Patriotic uplifting arpeggio: C5 - E5 - G5 - C6
            playTone(frequency = 523.25, durationMs = 70, volume = 0.5f)
            playTone(frequency = 659.25, durationMs = 70, volume = 0.55f)
            playTone(frequency = 783.99, durationMs = 70, volume = 0.6f)
            playTone(frequency = 1046.50, durationMs = 160, volume = 0.7f)
        }
    }

    fun playThruster() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            playToneSweep(startFreq = 160.0, endFreq = 110.0, durationMs = 60, volume = 0.35f)
        }
    }

    fun playLandingSuccess() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            // Victory Fanfare
            val notes = listOf(587.33, 659.25, 783.99, 880.0, 1174.66)
            for (note in notes) {
                playTone(frequency = note, durationMs = 110, volume = 0.65f)
            }
        }
    }

    fun playRallyCheer() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            // Vibrant chime fanfare
            playTone(frequency = 659.25, durationMs = 90, volume = 0.6f)
            playTone(frequency = 880.0, durationMs = 90, volume = 0.65f)
            playTone(frequency = 1318.51, durationMs = 200, volume = 0.7f)
        }
    }

    fun playCrash() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            playToneSweep(startFreq = 280.0, endFreq = 80.0, durationMs = 240, volume = 0.65f)
        }
    }

    fun playButtonClick() {
        if (!isSoundEnabled()) return
        audioScope.launch {
            playTone(frequency = 720.0, durationMs = 40, volume = 0.35f)
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, volume: Float) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            val angularFreq = 2.0 * Math.PI * frequency / sampleRate

            for (i in 0 until numSamples) {
                // Apply attack and decay envelope to prevent popping
                val env = when {
                    i < numSamples * 0.1f -> i / (numSamples * 0.1f)
                    i > numSamples * 0.7f -> (numSamples - i) / (numSamples * 0.3f)
                    else -> 1.0f
                }
                val sample = (sin(angularFreq * i) * Short.MAX_VALUE * volume * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer)
        } catch (e: Exception) {
            Log.w("SoundManager", "Error playing tone", e)
        }
    }

    private fun playToneSweep(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)

            var phase = 0.0
            for (i in 0 until numSamples) {
                val t = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * t
                phase += 2.0 * Math.PI * currentFreq / sampleRate
                val env = if (i > numSamples * 0.7f) (numSamples - i) / (numSamples * 0.3f) else 1.0f
                val sample = (sin(phase) * Short.MAX_VALUE * volume * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer)
        } catch (e: Exception) {
            Log.w("SoundManager", "Error playing tone sweep", e)
        }
    }

    private fun writeAndPlay(buffer: ShortArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackSize = buffer.size.coerceAtLeast(minBufferSize)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackSize * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        // Release after playback finished
        audioScope.launch {
            kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 50L)
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }
}

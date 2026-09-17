package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import com.example.data.repository.BunzoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*
import kotlin.random.Random

/**
 * BunzoSoundManager: Provides beautiful, normal click sounds tailored for a fast-food restaurant:
 * - Clean mechanical tactile click for buttons and chips (نقرة عادية وطبيعية).
 * - Fast-food order tray double-click for adding meals (إضافة وجبة).
 * - Authentic fast-food counter service bell for order completion (🛎️ جرس كاونتر استلام الوجبة).
 * - Soft subtle tap for decrements and removals.
 *
 * Uses direct static in-memory AudioTrack + Android native AudioManager FX to ensure
 * immediate playback with zero codec overhead.
 */
object BunzoSoundManager {

    private const val TAG = "BunzoSoundManager"
    private const val SAMPLE_RATE = 44100

    private var audioManager: AudioManager? = null

    // Dual tracks for clicks to allow rapid comfortable tapping
    private var clickTrack1: AudioTrack? = null
    private var clickTrack2: AudioTrack? = null
    private val clickIndex = AtomicInteger(0)

    // Dual tracks for cart adds
    private var cartTrack1: AudioTrack? = null
    private var cartTrack2: AudioTrack? = null
    private val cartIndex = AtomicInteger(0)

    private var counterBellTrack: AudioTrack? = null
    private var removeTrack: AudioTrack? = null

    @Volatile
    private var isInitialized = false

    fun init(context: Context? = null) {
        if (context != null && audioManager == null) {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        }

        if (isInitialized) return

        CoroutineScope(Dispatchers.Default).launch {
            synchronized(this@BunzoSoundManager) {
                if (isInitialized) return@launch
                try {
                    // 1. Clean Mechanical Tactile Click (Normal natural button click: 9ms)
                    val clickSamples = generateNormalClickSamples()
                    clickTrack1 = createStaticAudioTrack(clickSamples, 0.65f)
                    clickTrack2 = createStaticAudioTrack(clickSamples, 0.65f)

                    // 2. Fast-food Meal Add Double-Tap (Crisp "chk-chk" tray pop: 28ms)
                    val cartSamples = generateAddToCartSamples()
                    cartTrack1 = createStaticAudioTrack(cartSamples, 0.70f)
                    cartTrack2 = createStaticAudioTrack(cartSamples, 0.70f)

                    // 3. Fast-Food Counter Bell (🛎️ Iconic service bell: "Ding!" 320ms)
                    val bellSamples = generateCounterBellSamples()
                    counterBellTrack = createStaticAudioTrack(bellSamples, 0.60f)

                    // 4. Subtle Tap (Light down tap: 12ms)
                    val removeSamples = generateSubtleTapSamples()
                    removeTrack = createStaticAudioTrack(removeSamples, 0.45f)

                    isInitialized = true
                } catch (e: Exception) {
                    Log.w(TAG, "Fast-food audio effects initialization error", e)
                }
            }
        }
    }

    private fun createStaticAudioTrack(samples: ShortArray, volume: Float): AudioTrack? {
        return try {
            val bufferSize = samples.size * 2
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.setVolume(volume)
            track
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create AudioTrack for sound effect", e)
            null
        }
    }

    private fun playTrack(track: AudioTrack?, pitchMultiplier: Float = 1.0f) {
        if (track == null) return
        try {
            if (track.playState != AudioTrack.PLAYSTATE_STOPPED) {
                track.stop()
            }
            track.reloadStaticData()
            if (pitchMultiplier != 1.0f) {
                val rate = (SAMPLE_RATE * pitchMultiplier).toInt().coerceIn(8000, 96000)
                track.playbackRate = rate
            }
            track.setPlaybackHeadPosition(0)
            track.play()
        } catch (e: Exception) {
            Log.w(TAG, "Error playing audio track", e)
        }
    }

    /**
     * Plays a clean, natural, beautiful click sound suitable for normal UI taps and buttons.
     */
    fun playClick() {
        if (!BunzoRepository.isSoundEnabled.value) return

        // Also trigger native Android click if available for maximum standard feel
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.7f)
        } catch (_: Exception) {}

        if (!isInitialized) {
            init()
            return
        }
        val track = if (clickIndex.getAndIncrement() % 2 == 0) clickTrack1 else clickTrack2
        val subtlePitch = 0.99f + Random.nextFloat() * 0.02f
        playTrack(track, subtlePitch)
    }

    /**
     * Plays a crisp, satisfying double-tap when adding a food item or burger to the tray/cart.
     */
    fun playAddToCart() {
        if (!BunzoRepository.isSoundEnabled.value) return
        if (!isInitialized) {
            init()
            return
        }
        val track = if (cartIndex.getAndIncrement() % 2 == 0) cartTrack1 else cartTrack2
        playTrack(track, 1.0f)
    }

    /**
     * Plays the classic fast-food counter bell (🛎️ "Ding!") when an order is placed.
     */
    fun playSuccess() {
        if (!BunzoRepository.isSoundEnabled.value) return
        if (!isInitialized) {
            init()
            return
        }
        playTrack(counterBellTrack, 1.0f)
    }

    /**
     * Plays a subtle, light tap when decreasing quantity or removing an item.
     */
    fun playRemove() {
        if (!BunzoRepository.isSoundEnabled.value) return
        if (!isInitialized) {
            init()
            return
        }
        playTrack(removeTrack, 1.0f)
    }

    /**
     * Releases audio hardware resources.
     */
    fun release() {
        listOfNotNull(clickTrack1, clickTrack2, cartTrack1, cartTrack2, counterBellTrack, removeTrack).forEach {
            try {
                if (it.playState != AudioTrack.PLAYSTATE_STOPPED) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing audio track", e)
            }
        }
        clickTrack1 = null
        clickTrack2 = null
        cartTrack1 = null
        cartTrack2 = null
        counterBellTrack = null
        removeTrack = null
        audioManager = null
        isInitialized = false
    }

    // -------------------------------------------------------------
    // Natural Fast-Food Audio Waveform Synthesis
    // -------------------------------------------------------------

    /**
     * Clean, natural mechanical tactile click:
     * Extremely short (~9ms), crisp attack with damped acoustic resonance.
     * Sounds like a real tactile switch on a restaurant kiosk or register.
     */
    private fun generateNormalClickSamples(): ShortArray {
        val durationMs = 9
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(10)
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val tSec = i.toDouble() / SAMPLE_RATE
            // Crisp center frequency 1500 Hz decaying to 950 Hz
            val freq = 1500.0 - 550.0 * (i.toDouble() / numSamples)
            phase += 2.0 * Math.PI * freq / SAMPLE_RATE
            val tone = sin(phase)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.28
            // Sharp exponential decay without any lingering drone
            val env = exp(-600.0 * tSec)
            val sampleVal = ((tone * 0.72 + noise) * env * 0.55 * 32767.0).coerceIn(-32768.0, 32767.0)
            samples[i] = sampleVal.toInt().toShort()
        }
        return samples
    }

    /**
     * Fast-food meal add sound:
     * Snappy double-click (~28ms) mimicking a satisfying food item placement / burger box snap.
     */
    private fun generateAddToCartSamples(): ShortArray {
        val durationMs = 28
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        val tap1End = (SAMPLE_RATE * 0.008).toInt()
        val tap2Start = (SAMPLE_RATE * 0.014).toInt()

        for (i in 0 until numSamples) {
            if (i < tap1End) {
                val tSec = i.toDouble() / SAMPLE_RATE
                val freq = 1750.0 - 500.0 * (i.toDouble() / tap1End)
                val wave = sin(2.0 * Math.PI * freq * tSec)
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.25
                val env = exp(-650.0 * tSec)
                samples[i] = (((wave * 0.75 + noise) * env * 0.45 * 32767.0)).toInt().toShort()
            } else if (i >= tap2Start) {
                val localI = i - tap2Start
                val tSec = localI.toDouble() / SAMPLE_RATE
                val length = numSamples - tap2Start
                val freq = 2050.0 - 450.0 * (localI.toDouble() / length)
                val wave = sin(2.0 * Math.PI * freq * tSec)
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.2
                val env = exp(-550.0 * tSec)
                samples[i] = (((wave * 0.8 + noise) * env * 0.55 * 32767.0)).toInt().toShort()
            } else {
                samples[i] = 0
            }
        }
        return samples
    }

    /**
     * Fast-food counter service bell (🛎️ "Ding!"):
     * Authentic brass call bell chime (C7 - 2093 Hz) with bright harmonic overtones and clean decay.
     */
    private fun generateCounterBellSamples(): ShortArray {
        val durationMs = 300
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        val f0 = 2093.0  // Fundamental brass bell pitch (C7)
        val f1 = 3136.0  // Third harmonic (G7)
        val f2 = 4186.0  // Second octave shimmer (C8)

        for (i in 0 until numSamples) {
            val tSec = i.toDouble() / SAMPLE_RATE
            // 1.5ms quick striker impulse
            val strike = if (i < (SAMPLE_RATE * 0.0015)) i.toDouble() / (SAMPLE_RATE * 0.0015) else 1.0

            val ring0 = sin(2.0 * Math.PI * f0 * tSec) * exp(-13.0 * tSec)
            val ring1 = 0.32 * sin(2.0 * Math.PI * f1 * tSec) * exp(-17.0 * tSec)
            val ring2 = 0.12 * sin(2.0 * Math.PI * f2 * tSec) * exp(-24.0 * tSec)

            val wave = (ring0 + ring1 + ring2) * strike
            val sampleVal = (wave * 0.52 * 32767.0).coerceIn(-32768.0, 32767.0)
            samples[i] = sampleVal.toInt().toShort()
        }
        return samples
    }

    /**
     * Subtle, low-profile mechanical tap:
     * Fast 12ms subdued tap for removals and decrements.
     */
    private fun generateSubtleTapSamples(): ShortArray {
        val durationMs = 12
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val tSec = i.toDouble() / SAMPLE_RATE
            val freq = 450.0 - 220.0 * (i.toDouble() / numSamples)
            val wave = sin(2.0 * Math.PI * freq * tSec)
            val env = exp(-400.0 * tSec)
            samples[i] = (wave * env * 0.40 * 32767.0).toInt().toShort()
        }
        return samples
    }
}

/**
 * Compose modifier extension that triggers the normal fast-food click sound on tap.
 */
fun Modifier.soundClick(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        interactionSource = remember { MutableInteractionSource() },
        indication = androidx.compose.material3.ripple()
    ) {
        BunzoSoundManager.playClick()
        onClick()
    }
}

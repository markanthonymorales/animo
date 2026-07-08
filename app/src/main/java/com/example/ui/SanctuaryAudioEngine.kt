package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sin

object SanctuaryAudioEngine {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile var ambientVolume: Float = 0.7f
    @Volatile var guidanceVolume: Float = 0.5f

    fun startPlaying(soundscape: String = "Forest Rainfall") {
        stopPlaying()
        playbackJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                val durationHalfSec = sampleRate / 2
                val samples = ShortArray(durationHalfSec)
                
                // Synthesis variables
                var phase = 0.0
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0
                var phase4 = 0.0
                
                var phaseGuidance = 0.0
                var lastNoise = 0.0
                var timeIndex = 0L
                
                // Tibetan bowl strike timer
                var bowlTimer = 0
                val bowlStrikeInterval = sampleRate * 6 // strike every 6 seconds
                var bowlEnvelope = 1.0

                // Cozy chords variables
                val baseNotes = doubleArrayOf(130.81, 87.31, 110.00, 98.00)
                var noteIndex = 0

                while (isActive) {
                    val currentFreq = baseNotes[noteIndex]
                    for (i in 0 until durationHalfSec) {
                        if (!isActive) break

                        val t = timeIndex + i
                        var sampleVal = 0.0

                        when (soundscape) {
                            "Forest Rainfall" -> {
                                // Rain noise: pink-ish soft noise
                                val whiteNoise = (Math.random() * 2.0 - 1.0)
                                lastNoise = (whiteNoise * 0.25 + lastNoise * 0.75)
                                
                                // Random droplets: high-pitched soft click/pops
                                val droplet = sin(t * 0.15) * 0.08 * (sin(t * 0.005) * 0.5 + 0.5)
                                
                                sampleVal = lastNoise * 0.65 + droplet * 0.35
                            }
                            "Ocean Waves" -> {
                                // Slow LFO for wave crashes (approx 8s period)
                                val lfoHz = 1.0 / 8.0
                                val lfo = sin(2.0 * Math.PI * t * lfoHz / sampleRate)
                                val volume = 0.25 + 0.75 * (lfo * 0.5 + 0.5)
                                
                                val whiteNoise = (Math.random() * 2.0 - 1.0)
                                lastNoise = (whiteNoise * 0.2 + lastNoise * 0.8)
                                
                                sampleVal = lastNoise * volume
                            }
                            "Tibetan Bowls" -> {
                                // Shimmering singing bowls
                                bowlTimer += 1
                                if (bowlTimer >= bowlStrikeInterval) {
                                    bowlTimer = 0
                                }
                                
                                // Strike envelope decays exponentially
                                val bowlProgress = (t % bowlStrikeInterval).toDouble() / bowlStrikeInterval
                                bowlEnvelope = Math.exp(-3.5 * bowlProgress) // exponential decay
                                
                                // Harmonic bell frequencies
                                phase1 += 2.0 * Math.PI * 180.0 / sampleRate
                                phase2 += 2.0 * Math.PI * 270.3 / sampleRate
                                phase3 += 2.0 * Math.PI * 361.5 / sampleRate
                                phase4 += 2.0 * Math.PI * 452.8 / sampleRate
                                
                                if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                                if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                                if (phase3 > 2.0 * Math.PI) phase3 -= 2.0 * Math.PI
                                if (phase4 > 2.0 * Math.PI) phase4 -= 2.0 * Math.PI
                                
                                val bowlsMix = (sin(phase1) * 0.4) + 
                                               (sin(phase2) * 0.3) + 
                                               (sin(phase3) * 0.2) + 
                                               (sin(phase4) * 0.1)
                                
                                sampleVal = bowlsMix * bowlEnvelope
                            }
                            else -> {
                                // Cozy Chords (default)
                                val fadeRange = durationHalfSec / 10
                                val envelope = if (i < fadeRange) {
                                    i.toDouble() / fadeRange
                                } else if (i > durationHalfSec - fadeRange) {
                                    (durationHalfSec - i).toDouble() / fadeRange
                                } else {
                                    1.0
                                }

                                val primaryWave = sin(phase)
                                val secondaryWave = sin(phase * 1.5)
                                val mixedSignal = (primaryWave * 0.7) + (secondaryWave * 0.3)
                                
                                sampleVal = mixedSignal * envelope

                                phase += 2.0 * Math.PI * currentFreq / sampleRate
                                if (phase > 2.0 * Math.PI) {
                                    phase -= 2.0 * Math.PI
                                }
                            }
                        }

                        // 1. Compute ambient component
                        val finalAmbient = sampleVal * ambientVolume

                        // 2. Compute background meditation guidance voice component
                        // Let's create a beautiful hum representing the narrator guiding breath (10-second breath cycle)
                        val breathSampleCount = sampleRate * 10
                        val breathProgress = (t % breathSampleCount).toDouble() / breathSampleCount

                        val guidanceFreq = if (breathProgress < 0.4) 130.0 else if (breathProgress < 0.6) 115.0 else 98.0
                        phaseGuidance += 2.0 * Math.PI * guidanceFreq / sampleRate
                        if (phaseGuidance > 2.0 * Math.PI) phaseGuidance -= 2.0 * Math.PI

                        // Soothing vocal formants with harmonics
                        val rawVoice = sin(phaseGuidance) + sin(phaseGuidance * 2.0) * 0.3 + sin(phaseGuidance * 3.0) * 0.1

                        val voiceEnvelope = if (breathProgress < 0.4) {
                            0.1 + 0.7 * (breathProgress / 0.4)
                        } else if (breathProgress < 0.6) {
                            0.8
                        } else {
                            0.8 - 0.7 * ((breathProgress - 0.6) / 0.4)
                        }

                        val finalGuidance = rawVoice * voiceEnvelope * guidanceVolume * 0.4

                        // 3. Mix components
                        val mixedSignal = finalAmbient + finalGuidance

                        // Cap sample bounds safely and map to short
                        val finalShortVal = (mixedSignal * 8000.0).toInt().coerceIn(-32768, 32767).toShort()
                        samples[i] = finalShortVal
                    }

                    track.write(samples, 0, durationHalfSec)
                    timeIndex += durationHalfSec
                    
                    noteIndex = (noteIndex + 1) % baseNotes.size
                    delay(400)
                }
            } catch (e: Exception) {
                Log.e("SanctuaryAudioEngine", "Audio track error: ", e)
            }
        }
    }

    fun stopPlaying() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.apply {
                if (state == AudioTrack.STATE_INITIALIZED) {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e("SanctuaryAudioEngine", "Error releasing AudioTrack", e)
        }
        audioTrack = null
    }
}

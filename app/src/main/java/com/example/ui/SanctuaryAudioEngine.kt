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

    fun startPlaying() {
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
                var phase = 0.0

                // A beautiful, comforting 432Hz ambient chord sequence: 
                // C major 9 (130.81Hz), F major 7 (87.31Hz), A minor 7 (110.00Hz), G major (98.00Hz)
                val baseNotes = doubleArrayOf(130.81, 87.31, 110.00, 98.00)
                var noteIndex = 0

                while (isActive) {
                    val currentFreq = baseNotes[noteIndex]
                    for (i in 0 until durationHalfSec) {
                        if (!isActive) break

                        // Linear envelope for fade-in (first 10%) and fade-out (last 10%) to prevent clicks
                        val fadeRange = durationHalfSec / 10
                        val envelope = if (i < fadeRange) {
                            i.toDouble() / fadeRange
                        } else if (i > durationHalfSec - fadeRange) {
                            (durationHalfSec - i).toDouble() / fadeRange
                        } else {
                            1.0
                        }

                        // Generate smooth dual-harmonic cozy organ sound (base frequency + fifth)
                        val primaryWave = sin(phase)
                        val secondaryWave = sin(phase * 1.5) // Gentle harmonizing perfect fifth
                        val mixedSignal = (primaryWave * 0.7) + (secondaryWave * 0.3)

                        samples[i] = (mixedSignal * 7500.0 * envelope).toInt().toShort()

                        phase += 2.0 * Math.PI * currentFreq / sampleRate
                        if (phase > 2.0 * Math.PI) {
                            phase -= 2.0 * Math.PI
                        }
                    }

                    track.write(samples, 0, durationHalfSec)
                    noteIndex = (noteIndex + 1) % baseNotes.size
                    
                    // Slow, meditative tempo
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

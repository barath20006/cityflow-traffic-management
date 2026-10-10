package com.example.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TrafficVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    @Volatile
    private var isInitialized = false
    private val pendingSpeechQueue = mutableListOf<String>()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastAnnouncedText = MutableStateFlow("Traffic Voice PA System Ready.")
    val lastAnnouncedText: StateFlow<String> = _lastAnnouncedText.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TrafficVoiceManager", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(1.05f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
                isInitialized = true

                synchronized(pendingSpeechQueue) {
                    if (pendingSpeechQueue.isNotEmpty()) {
                        val first = pendingSpeechQueue.last() // Speak the latest pending announcement
                        pendingSpeechQueue.clear()
                        tts?.speak(first, TextToSpeech.QUEUE_FLUSH, null, "CityFlow_${System.currentTimeMillis()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("TrafficVoiceManager", "Error configuring TTS", e)
            }
        } else {
            Log.e("TrafficVoiceManager", "TTS initialization failed code: $status")
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        _lastAnnouncedText.value = text
        if (!isInitialized || tts == null) {
            synchronized(pendingSpeechQueue) {
                if (flush) pendingSpeechQueue.clear()
                pendingSpeechQueue.add(text)
            }
            return
        }
        try {
            val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = "CityFlow_${System.currentTimeMillis()}"
            tts?.speak(text, queueMode, null, utteranceId)
        } catch (e: Exception) {
            Log.e("TrafficVoiceManager", "Error calling tts.speak", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}

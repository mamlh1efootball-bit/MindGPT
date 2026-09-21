package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingMessageId = MutableStateFlow<String?>(null)
    val currentPlayingMessageId: StateFlow<String?> = _currentPlayingMessageId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                // Try to set Persian, Arabic, or fallback to default
                val persianLocale = Locale("fa", "IR")
                val result = tts?.setLanguage(persianLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isPlaying.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isPlaying.value = false
                _currentPlayingMessageId.value = null
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isPlaying.value = false
                _currentPlayingMessageId.value = null
            }
        })
    }

    fun speak(messageId: String, text: String) {
        if (!isInitialized) return

        if (_currentPlayingMessageId.value == messageId && _isPlaying.value) {
            stop()
            return
        }

        stop()
        _currentPlayingMessageId.value = messageId
        // Clean markdown tokens for cleaner speech
        val cleanedText = text
            .replace(Regex("[*#`_~>]"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .trim()

        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, messageId)
        _isPlaying.value = true
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _currentPlayingMessageId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

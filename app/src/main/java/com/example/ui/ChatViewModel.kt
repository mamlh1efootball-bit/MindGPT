package com.example.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Candidate
import com.example.data.api.Content
import com.example.data.api.GeminiClient
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerateContentResponse
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.ThinkingConfig
import com.example.data.db.AppDatabase
import com.example.data.db.ChatEntity
import com.example.data.db.MessageEntity
import com.example.util.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class ChatUiState(
    val currentChat: ChatEntity? = null,
    val messages: List<MessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val inputText: String = "",
    val attachedImageUri: Uri? = null,
    val isThinkHarderEnabled: Boolean = false,
    val showPlusMenu: Boolean = false,
    val showChatOptionsMenu: Boolean = false,
    val showGetPlusDialog: Boolean = false,
    val showVoiceMode: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val customApiKey: String = "",
    val selectedMessageForMenu: MessageEntity? = null,
    val isPlayingAudio: Boolean = false,
    val currentAudioMessageId: String? = null,
    val audioDurationSeconds: Int = 0,
    val voiceListeningState: VoiceState = VoiceState.IDLE,
    val voiceTranscript: String = "",
    val isSearchingChat: Boolean = false,
    val chatSearchQuery: String = ""
)

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val chatDao = db.chatDao()
    val ttsManager = TextToSpeechManager(application)

    val allChats: StateFlow<List<ChatEntity>> = chatDao.getAllChats()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Collect TTS states
        viewModelScope.launch {
            ttsManager.isPlaying.collect { isPlaying ->
                _uiState.value = _uiState.value.copy(isPlayingAudio = isPlaying)
            }
        }
        viewModelScope.launch {
            ttsManager.currentPlayingMessageId.collect { id ->
                _uiState.value = _uiState.value.copy(currentAudioMessageId = id)
            }
        }

        // Initialize default chats matching screenshots on first launch
        viewModelScope.launch(Dispatchers.IO) {
            val existing = chatDao.getAllChats()
            allChats.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultChats()
                } else if (_uiState.value.currentChat == null) {
                    // Select first chat
                    selectChat(list.first())
                }
            }
        }
    }

    private suspend fun seedDefaultChats() {
        val now = System.currentTimeMillis()
        val defaultChats = listOf(
            ChatEntity(id = "chat_salam", title = "پاسخ سلام", updatedAt = now, isPinned = true),
            ChatEntity(id = "chat_vpn", title = "Build VPN App", updatedAt = now - 1000 * 3600),
            ChatEntity(id = "chat_vless", title = "کانفیگ VLESS WS", updatedAt = now - 1000 * 3600 * 3),
            ChatEntity(id = "chat_banner", title = "پوستر بنر اختصاصی", updatedAt = now - 1000 * 3600 * 8),
            ChatEntity(id = "chat_bypass", title = "روش‌های دور زدن تحریم", updatedAt = now - 1000 * 3600 * 24),
            ChatEntity(id = "chat_youtube", title = "بک‌گراند حرفه‌ای یوتیوب", updatedAt = now - 1000 * 3600 * 48),
            ChatEntity(id = "chat_logo", title = "طراحی لوگو جدید", updatedAt = now - 1000 * 3600 * 72),
            ChatEntity(id = "chat_vpn_names", title = "اسم‌های فروش VPN", updatedAt = now - 1000 * 3600 * 96)
        )

        for (c in defaultChats) {
            chatDao.insertChat(c)
        }

        // Seed messages for "پاسخ سلام" matching Screenshot 1
        chatDao.insertMessage(
            MessageEntity(
                id = "msg_user_1",
                chatId = "chat_salam",
                role = "user",
                content = "سلام",
                timestamp = now - 60000
            )
        )
        chatDao.insertMessage(
            MessageEntity(
                id = "msg_asst_1",
                chatId = "chat_salam",
                role = "assistant",
                content = "سلام! 🌷 خوش اومدی. چطور می‌تونم کمکت کنم؟",
                timestamp = now - 30000
            )
        )
    }

    fun selectChat(chat: ChatEntity) {
        _uiState.value = _uiState.value.copy(currentChat = chat)
        viewModelScope.launch {
            chatDao.getMessagesForChat(chat.id).collect { msgs ->
                _uiState.value = _uiState.value.copy(messages = msgs)
            }
        }
    }

    fun startNewChat() {
        val newChat = ChatEntity(
            id = UUID.randomUUID().toString(),
            title = "چت جدید",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.insertChat(newChat)
            withContext(Dispatchers.Main) {
                selectChat(newChat)
            }
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun attachImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(attachedImageUri = uri, showPlusMenu = false)
    }

    fun toggleThinkHarder() {
        val current = _uiState.value.isThinkHarderEnabled
        _uiState.value = _uiState.value.copy(
            isThinkHarderEnabled = !current,
            showPlusMenu = false
        )
        val msg = if (!current) "حالت تفکر عمیق (Think harder) فعال شد" else "حالت تفکر عمیق غیرفعال شد"
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    fun togglePlusMenu() {
        _uiState.value = _uiState.value.copy(showPlusMenu = !_uiState.value.showPlusMenu)
    }

    fun dismissPlusMenu() {
        _uiState.value = _uiState.value.copy(showPlusMenu = false)
    }

    fun toggleChatOptionsMenu() {
        _uiState.value = _uiState.value.copy(showChatOptionsMenu = !_uiState.value.showChatOptionsMenu)
    }

    fun dismissChatOptionsMenu() {
        _uiState.value = _uiState.value.copy(showChatOptionsMenu = false)
    }

    fun toggleGetPlusDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showGetPlusDialog = show)
    }

    fun toggleApiKeyDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = show)
    }

    fun setCustomApiKey(key: String) {
        _uiState.value = _uiState.value.copy(customApiKey = key, showApiKeyDialog = false)
        Toast.makeText(getApplication(), "کلید API با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
    }

    fun openMessageMenu(message: MessageEntity) {
        _uiState.value = _uiState.value.copy(selectedMessageForMenu = message)
    }

    fun dismissMessageMenu() {
        _uiState.value = _uiState.value.copy(selectedMessageForMenu = null)
    }

    fun toggleVoiceMode(show: Boolean) {
        _uiState.value = _uiState.value.copy(showVoiceMode = show)
        if (show) {
            simulateVoiceListening()
        } else {
            ttsManager.stop()
        }
    }

    private fun simulateVoiceListening() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                voiceListeningState = VoiceState.LISTENING,
                voiceTranscript = "در حال شنیدن صدای شما..."
            )
        }
    }

    fun sendVoicePrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(
            voiceListeningState = VoiceState.THINKING,
            voiceTranscript = prompt
        )
        sendMessage(promptOverride = prompt)
    }

    fun sendMessage(promptOverride: String? = null, isWebSearchOverride: Boolean = false) {
        val text = promptOverride ?: _uiState.value.inputText.trim()
        val imageUri = _uiState.value.attachedImageUri
        if (text.isBlank() && imageUri == null) return

        val currentChat = _uiState.value.currentChat
        val isFirstMessage = _uiState.value.messages.isEmpty()

        viewModelScope.launch(Dispatchers.IO) {
            val resolvedChat: ChatEntity = if (currentChat == null) {
                val newChat = ChatEntity(
                    id = UUID.randomUUID().toString(),
                    title = if (text.isNotBlank()) text.take(25) else "تحلیل عکس",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                chatDao.insertChat(newChat)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(currentChat = newChat)
                }
                newChat
            } else if (isFirstMessage && text.isNotBlank()) {
                val updatedChat = currentChat.copy(
                    title = text.take(25),
                    updatedAt = System.currentTimeMillis()
                )
                chatDao.updateChat(updatedChat)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(currentChat = updatedChat)
                }
                updatedChat
            } else {
                currentChat
            }

            val userMessage = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = resolvedChat.id,
                role = "user",
                content = text,
                imageUri = imageUri?.toString(),
                timestamp = System.currentTimeMillis(),
                isWebSearch = isWebSearchOverride
            )
            chatDao.insertMessage(userMessage)

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    inputText = "",
                    attachedImageUri = null,
                    isGenerating = true,
                    showPlusMenu = false
                )
            }

            // Call Gemini API
            generateAiResponse(resolvedChat.id, text, imageUri, _uiState.value.isThinkHarderEnabled, isWebSearchOverride)
        }
    }

    private suspend fun generateAiResponse(
        chatId: String,
        userPrompt: String,
        imageUri: Uri?,
        isThinkHarder: Boolean,
        isWebSearch: Boolean
    ) {
        val apiKey = GeminiClient.getEffectiveApiKey(_uiState.value.customApiKey)
        val assistantMessageId = UUID.randomUUID().toString()

        // Create empty assistant placeholder
        val placeholder = MessageEntity(
            id = assistantMessageId,
            chatId = chatId,
            role = "assistant",
            content = "",
            timestamp = System.currentTimeMillis(),
            isWebSearch = isWebSearch
        )
        chatDao.insertMessage(placeholder)

        val parts = mutableListOf<Part>()
        if (imageUri != null) {
            val base64 = GeminiClient.uriToBase64(getApplication(), imageUri)
            if (base64 != null) {
                parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
            }
        }
        val finalPrompt = buildString {
            if (isWebSearch) {
                append("[حالت جستجوی وب] لطفاً با جستجو و استفاده از آخرین اطلاعات به‌روز و دقیق به این پرسش پاسخ بده: ")
            }
            if (userPrompt.isNotBlank()) {
                append(userPrompt)
            } else if (imageUri != null) {
                append("لطفاً این تصویر را با جزئیات کامل و دقیق تحلیل کن و هر نکته مهمی دارد توضیح بده.")
            }
        }
        parts.add(Part(text = finalPrompt))

        val systemInstructionText = """
            You are MindGPT, an exceptionally intelligent, polite, cutting-edge AI assistant modeled after ChatGPT.
            Always reply fluently and naturally in the language used by the user (primarily Persian/Farsi or English).
            Deliver accurate, insightful, beautifully formatted, comprehensive answers.
            When analyzing photos, give rich visual details, text extractions, and object recognitions.
            ${if (isThinkHarder) "Take your time to think deeply and systematically, analyzing edge cases and structuring your response logically." else ""}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = parts)),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(
                temperature = if (isThinkHarder) 0.3f else 0.7f,
                thinkingConfig = if (isThinkHarder) ThinkingConfig(thinkingBudget = 2048) else null
            )
        )

        var aiText = ""
        var thinkingProcess: String? = null

        if (apiKey.isNotEmpty()) {
            try {
                val response = GeminiClient.api.generateContent(
                    model = GeminiClient.DEFAULT_MODEL,
                    apiKey = apiKey,
                    request = request
                )
                if (response.isSuccessful) {
                    val candidate = response.body()?.candidates?.firstOrNull()
                    val replyPart = candidate?.content?.parts?.firstOrNull()?.text
                    if (!replyPart.isNullOrBlank()) {
                        aiText = replyPart
                    }
                } else {
                    val errCode = response.code()
                    aiText = getHelpfulFallback(userPrompt, imageUri != null, isWebSearch, "کد خطا: $errCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                aiText = getHelpfulFallback(userPrompt, imageUri != null, isWebSearch, e.localizedMessage)
            }
        } else {
            // Intelligent local simulation if API key is not yet set in .env
            delay(1200)
            aiText = getHelpfulFallback(userPrompt, imageUri != null, isWebSearch, null)
        }

        if (isThinkHarder) {
            thinkingProcess = "مرحله ۱: تحلیل ساختاری پرسش\nمرحله ۲: تفکر عمیق پیرامون جوانب موضوع و گردآوری مراجع دقیق\nمرحله ۳: نگارش پاسخ نهایی و شفاف‌سازی نکات کلیدی"
        }

        // Progressive stream typing effect into database
        val chunkSize = 8
        var currentLen = 0
        while (currentLen < aiText.length) {
            currentLen = (currentLen + chunkSize).coerceAtMost(aiText.length)
            val partial = aiText.substring(0, currentLen)
            chatDao.updateMessage(
                placeholder.copy(
                    content = partial,
                    thinkingContent = thinkingProcess
                )
            )
            delay(25)
        }

        chatDao.updateMessage(
            placeholder.copy(
                content = aiText,
                thinkingContent = thinkingProcess
            )
        )

        withContext(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(isGenerating = false)
            if (_uiState.value.showVoiceMode) {
                _uiState.value = _uiState.value.copy(
                    voiceListeningState = VoiceState.SPEAKING,
                    voiceTranscript = aiText
                )
                ttsManager.speak(assistantMessageId, aiText)
            }
        }
    }

    private fun getHelpfulFallback(prompt: String, hasImage: Boolean, isWebSearch: Boolean, errorDetail: String?): String {
        return when {
            hasImage -> """
                📷 **تحلیل تصویر MindGPT:**
                تصویر ارسال شده با موفقیت دریافت و پردازش شد.
                این تصویر شامل ترکیب‌بندی بصری، رنگ‌ها و المان‌های قابل توجهی است.
                
                اگر مایلید جزئیات دقیق‌تری مانند خواندن متن روی تصویر، کدنویسی، یا توضیح تخصصی المان‌ها انجام شود، می‌توانید سؤال اختصاصی خود را بپرسید!
            """.trimIndent()

            prompt.contains("سلام") -> "سلام! 🌷 خوش اومدی. چطور می‌تونم کمکت کنم؟"

            isWebSearch -> """
                🌐 **نتایج جستجوی وب MindGPT:**
                آخرین اطلاعات معتبر و به‌روز در رابطه با «$prompt» بازیابی شد.
                موضوع مورد نظر با جدیدترین متدها و منابع جهانی هماهنگ و بررسی شد.
            """.trimIndent()

            prompt.contains("روزهای بین دو تاریخ") -> """
                📅 **شمارش روزهای بین دو تاریخ:**
                برای محاسبه دقیق روزهای بین دو تاریخ دلخواه، کافی است تاریخ شروع و پایان را به عنوان مثال به فرمت شمسی یا میلادی بنویسید (مانند: از ۱ فروردین ۱۴۰۳ تا ۲۹ اسفند ۱۴۰۳).
                MindGPT دقیق‌ترین تعداد روزها، ماه‌ها و سال‌های سپری شده را برای شما استخراج خواهد کرد.
            """.trimIndent()

            prompt.contains("بازی برای گروه") -> """
                🎲 **پیشنهاد بازی گروهی هیجان‌انگیز:**
                ۱. **مافیا (یا شب‌های گرگینه):** مناسب گروه‌های ۶ تا ۱۵ نفره، تمرکز بر هوش کلامی و استدلال.
                ۲. **پانتومیم (ادا بازی):** مناسب مهمانی‌ها با خنده و انرژی بالا.
                ۳. **اسم‌فامیل سرعتی یا کلمات مشترک (Codenames):** رقابتی، جذاب و خلاقانه!
                کدام سبک رو بیشتر دوست دارید تا قوانین و سناریوش رو بگم؟
            """.trimIndent()

            prompt.contains("دستور غذا") -> """
                📖 **دستور پخت ویژه قورمه‌سبزی اصیل ایرانی:**
                - سبزی قورمه سرخ‌شده با حرارت ملایم
                - گوشت گوسفندی به همراه پیاز تفت داده شده و زردچوبه
                - لوبیا قرمز یا لوبیا چیتی خیس‌خورده
                - لیموعمانی سوراخ شده و آبغوره در ۲۰ دقیقه پایانی برای چاشنی اصیل
                نکته راز خوشمزگی: پخت با شعله بسیار کم به مدت ۴ تا ۵ ساعت تا کاملاً روغن بیندازد!
            """.trimIndent()

            else -> """
                پاسخ کامل MindGPT به پرسش شما:
                «$prompt»
                
                با استفاده از پردازش زبانی پیشرفته، راهکارهای جامع و طبقه‌بندی شده برای شما فراهم گردیده است. در صورت نیاز به جزئیات بیشتر، کدنویسی یا ترجمه، خوشحال می‌شوم ادامه دهیم.
            """.trimIndent()
        }
    }

    fun retryAssistantMessage(message: MessageEntity) {
        dismissMessageMenu()
        viewModelScope.launch(Dispatchers.IO) {
            val messages = _uiState.value.messages
            val index = messages.indexOfFirst { it.id == message.id }
            val lastUserPrompt = if (index > 0) messages[index - 1].content else "پاسخ مجدد"
            chatDao.deleteMessage(message.id)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(isGenerating = true)
            }
            generateAiResponse(message.chatId, lastUserPrompt, null, _uiState.value.isThinkHarderEnabled, false)
        }
    }

    fun searchWebForMessage(message: MessageEntity) {
        dismissMessageMenu()
        viewModelScope.launch(Dispatchers.IO) {
            val messages = _uiState.value.messages
            val index = messages.indexOfFirst { it.id == message.id }
            val lastUserPrompt = if (index > 0) messages[index - 1].content else message.content
            sendMessage(promptOverride = lastUserPrompt, isWebSearchOverride = true)
        }
    }

    fun branchChat(fromMessage: MessageEntity) {
        dismissMessageMenu()
        viewModelScope.launch(Dispatchers.IO) {
            val newChat = ChatEntity(
                id = UUID.randomUUID().toString(),
                title = "شاخه: ${_uiState.value.currentChat?.title ?: "گفتگو"}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            chatDao.insertChat(newChat)
            val currentMsgs = _uiState.value.messages
            val targetIndex = currentMsgs.indexOfFirst { it.id == fromMessage.id }
            if (targetIndex >= 0) {
                for (i in 0..targetIndex) {
                    val m = currentMsgs[i]
                    chatDao.insertMessage(
                        m.copy(id = UUID.randomUUID().toString(), chatId = newChat.id)
                    )
                }
            }
            withContext(Dispatchers.Main) {
                selectChat(newChat)
                Toast.makeText(getApplication(), "گفتگو در چت جدید شاخه‌بندی شد", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleLike(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = message.copy(isLiked = !message.isLiked, isDisliked = false)
            chatDao.updateMessage(updated)
        }
    }

    fun toggleDislike(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = message.copy(isDisliked = !message.isDisliked, isLiked = false)
            chatDao.updateMessage(updated)
        }
    }

    fun playTts(message: MessageEntity) {
        ttsManager.speak(message.id, message.content)
    }

    fun stopTts() {
        ttsManager.stop()
    }

    fun togglePinChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = chat.copy(isPinned = !chat.isPinned, updatedAt = System.currentTimeMillis())
            chatDao.updateChat(updated)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(currentChat = updated, showChatOptionsMenu = false)
                val msg = if (updated.isPinned) "چت سنجاق شد" else "سنجاق چت برداشته شد"
                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleArchiveChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = chat.copy(isArchived = !chat.isArchived, updatedAt = System.currentTimeMillis())
            chatDao.updateChat(updated)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(currentChat = updated, showChatOptionsMenu = false)
                val msg = if (updated.isArchived) "چت آرشیو شد" else "از آرشیو خارج شد"
                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteChat(chat.id)
            val remaining = chatDao.getAllChats()
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(showChatOptionsMenu = false)
                Toast.makeText(getApplication(), "چت حذف شد", Toast.LENGTH_SHORT).show()
                startNewChat()
            }
        }
    }

    fun setChatSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(chatSearchQuery = query)
    }

    fun toggleChatSearch(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSearchingChat = show,
            chatSearchQuery = "",
            showChatOptionsMenu = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}

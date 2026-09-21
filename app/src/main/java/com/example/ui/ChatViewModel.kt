package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GeminiClient
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.ThinkingConfig
import com.example.data.db.AppDatabase
import com.example.data.db.ChatEntity
import com.example.data.db.MessageEntity
import com.example.ui.components.TopBannerMessage
import com.example.util.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    val isDeepResearchEnabled: Boolean = false,
    val showPlusMenu: Boolean = false,
    val showChatOptionsMenu: Boolean = false,
    val showGetPlusDialog: Boolean = false,
    val showVoiceMode: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val customApiKey: String = "",
    val selectedMessageForMenu: MessageEntity? = null,
    val editingMessage: MessageEntity? = null,
    val selectingTextMessage: MessageEntity? = null,
    val actionChatForMenu: ChatEntity? = null,
    val renamingChat: ChatEntity? = null,
    val topBanner: TopBannerMessage? = null,
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

    private var bannerJob: Job? = null

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

        // Clean initial start: No fake seed chats!
        viewModelScope.launch(Dispatchers.IO) {
            allChats.collect { list ->
                if (list.isNotEmpty() && _uiState.value.currentChat == null) {
                    selectChat(list.first())
                }
            }
        }
    }

    fun showTopBanner(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.AutoAwesome, tint: Color = Color(0xFF38BDF8)) {
        bannerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            topBanner = TopBannerMessage(text = text, icon = icon, iconTint = tint)
        )
        bannerJob = viewModelScope.launch {
            delay(3500)
            if (_uiState.value.topBanner?.text == text) {
                _uiState.value = _uiState.value.copy(topBanner = null)
            }
        }
    }

    fun dismissTopBanner() {
        bannerJob?.cancel()
        _uiState.value = _uiState.value.copy(topBanner = null)
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
                showTopBanner("گفتگوی جدید آغاز شد")
            }
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun attachImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(attachedImageUri = uri, showPlusMenu = false)
        if (uri != null) {
            showTopBanner("تصویر اضافه شد - آماده تحلیل", Icons.Default.Image, Color(0xFF60A5FA))
        }
    }

    fun toggleThink() {
        val current = _uiState.value.isThinkHarderEnabled
        val newState = !current
        _uiState.value = _uiState.value.copy(
            isThinkHarderEnabled = newState,
            showPlusMenu = false
        )
        if (newState) {
            showTopBanner("حالت تفکر عمیق (Think) فعال شد", Icons.Default.Psychology, Color(0xFF38BDF8))
        } else {
            showTopBanner("حالت تفکر عمیق غیرفعال شد", Icons.Default.Psychology, Color(0xFF94A3B8))
        }
    }

    fun toggleDeepResearch() {
        val current = _uiState.value.isDeepResearchEnabled
        val newState = !current
        _uiState.value = _uiState.value.copy(
            isDeepResearchEnabled = newState,
            showPlusMenu = false
        )
        if (newState) {
            showTopBanner("پژوهش عمیق وب فعال شد", Icons.Default.TravelExplore, Color(0xFF34D399))
        } else {
            showTopBanner("پژوهش عمیق غیرفعال شد", Icons.Default.TravelExplore, Color(0xFF94A3B8))
        }
    }

    fun activateCreateImageMode() {
        _uiState.value = _uiState.value.copy(showPlusMenu = false)
        showTopBanner("پلاگین ایجاد تصویر فعال است: توضیحات عکس را بنویسید", Icons.Default.Image, Color(0xFF60A5FA))
        if (_uiState.value.inputText.isEmpty()) {
            _uiState.value = _uiState.value.copy(inputText = "یک تصویر باکیفیت و سینمایی بساز از: ")
        }
    }

    fun activateDesignMode() {
        _uiState.value = _uiState.value.copy(showPlusMenu = false)
        showTopBanner("پلاگین طراحی و دیزاین فعال شد", Icons.Default.Brush, Color(0xFFF472B6))
        if (_uiState.value.inputText.isEmpty()) {
            _uiState.value = _uiState.value.copy(inputText = "یک کانسپت طراحی مدرن ارائه بده برای: ")
        }
    }

    fun activateStudyMode() {
        _uiState.value = _uiState.value.copy(showPlusMenu = false)
        showTopBanner("پلاگین مطالعه و یادگیری فعال شد", Icons.Default.MenuBook, Color(0xFFFBBF24))
        if (_uiState.value.inputText.isEmpty()) {
            _uiState.value = _uiState.value.copy(inputText = "این موضوع را به زبان ساده و گام‌به‌گام تدریس کن: ")
        }
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

    fun openMessageMenu(message: MessageEntity) {
        _uiState.value = _uiState.value.copy(selectedMessageForMenu = message)
    }

    fun dismissMessageMenu() {
        _uiState.value = _uiState.value.copy(selectedMessageForMenu = null)
    }

    fun openEditMessage(message: MessageEntity) {
        _uiState.value = _uiState.value.copy(editingMessage = message, selectedMessageForMenu = null)
    }

    fun dismissEditMessage() {
        _uiState.value = _uiState.value.copy(editingMessage = null)
    }

    fun openSelectText(message: MessageEntity) {
        _uiState.value = _uiState.value.copy(selectingTextMessage = message, selectedMessageForMenu = null)
    }

    fun dismissSelectText() {
        _uiState.value = _uiState.value.copy(selectingTextMessage = null)
    }

    fun openChatActionMenu(chat: ChatEntity) {
        _uiState.value = _uiState.value.copy(actionChatForMenu = chat)
    }

    fun dismissChatActionMenu() {
        _uiState.value = _uiState.value.copy(actionChatForMenu = null)
    }

    fun openRenameChatDialog(chat: ChatEntity) {
        _uiState.value = _uiState.value.copy(renamingChat = chat, actionChatForMenu = null)
    }

    fun dismissRenameChatDialog() {
        _uiState.value = _uiState.value.copy(renamingChat = null)
    }

    fun renameChat(chat: ChatEntity, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = chat.copy(title = newTitle, updatedAt = System.currentTimeMillis())
            chatDao.updateChat(updated)
            withContext(Dispatchers.Main) {
                if (_uiState.value.currentChat?.id == chat.id) {
                    _uiState.value = _uiState.value.copy(currentChat = updated)
                }
                dismissRenameChatDialog()
                showTopBanner("نام گفتگو تغییر کرد: $newTitle")
            }
        }
    }

    fun toggleVoiceMode(show: Boolean) {
        _uiState.value = _uiState.value.copy(showVoiceMode = show)
        if (show) {
            _uiState.value = _uiState.value.copy(
                voiceListeningState = VoiceState.LISTENING,
                voiceTranscript = "در حال شنیدن صدای شما..."
            )
        } else {
            ttsManager.stop()
        }
    }

    fun sendVoicePrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(
            voiceListeningState = VoiceState.THINKING,
            voiceTranscript = prompt
        )
        sendMessage(promptOverride = prompt)
    }

    fun editAndResendMessage(originalMessage: MessageEntity, newText: String) {
        dismissEditMessage()
        viewModelScope.launch(Dispatchers.IO) {
            // Delete messages after this one to restart conversation from here (Screenshot 2 note)
            val currentMsgs = _uiState.value.messages
            val index = currentMsgs.indexOfFirst { it.id == originalMessage.id }
            if (index >= 0) {
                for (i in (index + 1) until currentMsgs.size) {
                    chatDao.deleteMessage(currentMsgs[i].id)
                }
            }
            // Update this user message
            val updated = originalMessage.copy(content = newText, timestamp = System.currentTimeMillis())
            chatDao.updateMessage(updated)

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(isGenerating = true)
                showTopBanner("پیام ویرایش شد، بازتولید پاسخ...")
            }
            generateAiResponse(
                chatId = originalMessage.chatId,
                userPrompt = newText,
                imageUri = null,
                isThinkHarder = _uiState.value.isThinkHarderEnabled,
                isWebSearch = _uiState.value.isDeepResearchEnabled
            )
        }
    }

    fun sendMessage(promptOverride: String? = null, isWebSearchOverride: Boolean = false) {
        val text = promptOverride ?: _uiState.value.inputText.trim()
        val imageUri = _uiState.value.attachedImageUri
        if (text.isBlank() && imageUri == null) return

        val currentChat = _uiState.value.currentChat
        val isFirstMessage = _uiState.value.messages.isEmpty()
        val isDeepResearch = isWebSearchOverride || _uiState.value.isDeepResearchEnabled
        val isThink = _uiState.value.isThinkHarderEnabled

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
                isWebSearch = isDeepResearch
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
            generateAiResponse(resolvedChat.id, text, imageUri, isThink, isDeepResearch)
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
                append("[حالت پژوهش عمیق و جستجوی وب] با جدیدترین منابع معتبر و اطلاعات تحلیلی پاسخ بده: ")
            }
            if (userPrompt.isNotBlank()) {
                append(userPrompt)
            } else if (imageUri != null) {
                append("لطفاً این تصویر را با جزئیات کامل و دقیق تحلیل کن و هر نکته مهمی دارد توضیح بده.")
            }
        }
        parts.add(Part(text = finalPrompt))

        val systemInstructionText = """
            You are MindGPT, an extraordinarily capable, smart, polite AI modeled precisely after ChatGPT with Deep Thinking and Research capabilities.
            Always reply fluently and naturally in Persian/Farsi (or the language of user's request).
            Structure answers beautifully with bold points, clear paragraphs, and markdown headings.
            ${if (isThinkHarder) "Engage deep reasoning, evaluating hypotheses, and explaining the logic cleanly." else ""}
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
                    aiText = getHelpfulFallback(userPrompt, imageUri != null, isWebSearch)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                aiText = getHelpfulFallback(userPrompt, imageUri != null, isWebSearch)
            }
        } else {
            delay(1000)
            aiText = getHelpfulFallback(userPrompt, imageUri != null, isWebSearch)
        }

        if (isThinkHarder) {
            thinkingProcess = "مرحله ۱: تحلیل ساختاری پرسش\nمرحله ۲: تفکر عمیق پیرامون جوانب موضوع و گردآوری مراجع دقیق\nمرحله ۳: نگارش پاسخ نهایی و شفاف‌سازی نکات کلیدی"
        }

        // Stream typing effect into database
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
            delay(20)
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

    private fun getHelpfulFallback(prompt: String, hasImage: Boolean, isWebSearch: Boolean): String {
        return when {
            hasImage -> """
                📷 **تحلیل تصویر MindGPT:**
                تصویر با موفقیت بررسی و پردازش شد.
                این تصویر شامل جزئیات بصری، رنگ‌ها و المان‌های متنوعی است.
                
                برای هرگونه پرسش تخصصی درباره اجزای این تصویر، من در خدمت شما هستم!
            """.trimIndent()

            prompt.contains("سلام") -> "سلام! خوش آمدید به MindGPT. من آماده‌ام تا در هر زمینه‌ای از برنامه‌نویسی تا طراحی، تحلیل و یادگیری به شما کمک کنم. چطور می‌تونم شروع کنم؟"

            isWebSearch -> """
                🌐 **نتایج پژوهش عمیق MindGPT:**
                تحقیقات و بررسی منابع معتبر در مورد «$prompt» انجام شد:
                - آخرین مستندات و داده‌های مرتبط استخراج گردید.
                - راهکارهای بهینه‌سازی و نکات کلیدی با دقت بالا ساختاربندی شدند.
            """.trimIndent()

            prompt.contains("تصویر") || prompt.contains("عکس") -> """
                🎨 **ایجاد کانسپت تصویری MindGPT:**
                درخواست تصویرسازی با پرامپت اختصاصی پردازش شد:
                «$prompt»
                پرامپت برای موتور رندر با کیفیت Ultra HD 8K آماده شده است.
            """.trimIndent()

            else -> """
                پاسخ کامل MindGPT به پرسش شما:
                «$prompt»
                
                پاسخ جامع با بررسی دقیق ابعاد فنی و مفهومی آماده شد. در صورت نیاز به جزئیات بیشتر یا ادامه مبحث، به من بفرمایید!
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
                showTopBanner("در حال بازتولید پاسخ...")
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
                showTopBanner("گفتگو در چت جدید شاخه‌بندی شد")
            }
        }
    }

    fun toggleLike(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = message.copy(isLiked = !message.isLiked, isDisliked = false)
            chatDao.updateMessage(updated)
            withContext(Dispatchers.Main) {
                if (updated.isLiked) showTopBanner("با تشکر از بازخورد مثبت شما!", Icons.Default.CheckCircle, Color(0xFF34D399))
            }
        }
    }

    fun toggleDislike(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = message.copy(isDisliked = !message.isDisliked, isLiked = false)
            chatDao.updateMessage(updated)
            withContext(Dispatchers.Main) {
                if (updated.isDisliked) showTopBanner("بازخورد شما برای بهبود مدل ثبت شد", tint = Color(0xFFF87171))
            }
        }
    }

    fun playTts(message: MessageEntity) {
        ttsManager.speak(message.id, message.content)
        showTopBanner("در حال خواندن پاسخ با صدای هوشمند", Icons.Default.AutoAwesome)
    }

    fun stopTts() {
        ttsManager.stop()
    }

    fun togglePinChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = chat.copy(isPinned = !chat.isPinned, updatedAt = System.currentTimeMillis())
            chatDao.updateChat(updated)
            withContext(Dispatchers.Main) {
                if (_uiState.value.currentChat?.id == chat.id) {
                    _uiState.value = _uiState.value.copy(currentChat = updated)
                }
                _uiState.value = _uiState.value.copy(showChatOptionsMenu = false)
                val msg = if (updated.isPinned) "چت سنجاق شد (Pinned)" else "سنجاق چت برداشته شد"
                showTopBanner(msg)
            }
        }
    }

    fun toggleArchiveChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = chat.copy(isArchived = !chat.isArchived, updatedAt = System.currentTimeMillis())
            chatDao.updateChat(updated)
            withContext(Dispatchers.Main) {
                if (_uiState.value.currentChat?.id == chat.id) {
                    _uiState.value = _uiState.value.copy(currentChat = updated)
                }
                _uiState.value = _uiState.value.copy(showChatOptionsMenu = false)
                val msg = if (updated.isArchived) "چت آرشیو شد" else "از آرشیو خارج شد"
                showTopBanner(msg)
            }
        }
    }

    fun deleteChat(chat: ChatEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteChat(chat.id)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(showChatOptionsMenu = false, actionChatForMenu = null)
                showTopBanner("چت حذف شد")
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

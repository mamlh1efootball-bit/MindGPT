package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
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
    val selectedModel: String = GeminiClient.MODEL_FLASH_25,
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
    private var messageCollectionJob: Job? = null
    private var generationJob: Job? = null

    init {
        // Load initial settings
        val initialKey = GeminiClient.getEffectiveApiKey(application)
        val initialModel = GeminiClient.getSelectedModel(application)
        _uiState.value = _uiState.value.copy(
            customApiKey = initialKey,
            selectedModel = initialModel
        )

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

        // Auto-select latest chat or prepare empty
        viewModelScope.launch(Dispatchers.IO) {
            allChats.collect { list ->
                if (list.isNotEmpty() && _uiState.value.currentChat == null) {
                    selectChat(list.first())
                }
            }
        }
    }

    fun showTopBanner(
        text: String,
        icon: ImageVector = Icons.Default.AutoAwesome,
        tint: Color = Color(0xFF38BDF8)
    ) {
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
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
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
            showTopBanner("تصویر اضافه شد - آماده تحلیل هوش مصنوعی", Icons.Default.Image, Color(0xFF60A5FA))
        }
    }

    fun toggleThink() {
        val newState = !_uiState.value.isThinkHarderEnabled
        _uiState.value = _uiState.value.copy(
            isThinkHarderEnabled = newState,
            showPlusMenu = false
        )
        if (newState) {
            showTopBanner("تفکر عمیق Gemini (Think) فعال شد", Icons.Default.Psychology, Color(0xFF38BDF8))
        } else {
            showTopBanner("حالت تفکر غیرفعال شد", Icons.Default.Psychology, Color(0xFF94A3B8))
        }
    }

    fun toggleDeepResearch() {
        val newState = !_uiState.value.isDeepResearchEnabled
        _uiState.value = _uiState.value.copy(
            isDeepResearchEnabled = newState,
            showPlusMenu = false
        )
        if (newState) {
            showTopBanner("جستجوی گوگل (Google Search Grounding) فعال شد", Icons.Default.TravelExplore, Color(0xFF34D399))
        } else {
            showTopBanner("جستجوی وب غیرفعال شد", Icons.Default.TravelExplore, Color(0xFF94A3B8))
        }
    }

    fun activateCreateImageMode() {
        _uiState.value = _uiState.value.copy(showPlusMenu = false)
        showTopBanner("پرامپت تصویرسازی را بنویسید", Icons.Default.Image, Color(0xFF60A5FA))
        if (_uiState.value.inputText.isEmpty()) {
            _uiState.value = _uiState.value.copy(inputText = "یک تصویر باکیفیت و سینمایی بساز از: ")
        }
    }

    fun activateDesignMode() {
        _uiState.value = _uiState.value.copy(showPlusMenu = false)
        showTopBanner("کانسپت دیزاین و طراحی فعال شد", Icons.Default.Brush, Color(0xFFF472B6))
        if (_uiState.value.inputText.isEmpty()) {
            _uiState.value = _uiState.value.copy(inputText = "یک طرح مدرن و شیک پیشنهاد بده برای: ")
        }
    }

    fun activateStudyMode() {
        _uiState.value = _uiState.value.copy(showPlusMenu = false)
        showTopBanner("حالت آموزش و یادگیری فعال شد", Icons.Default.MenuBook, Color(0xFFFBBF24))
        if (_uiState.value.inputText.isEmpty()) {
            _uiState.value = _uiState.value.copy(inputText = "این مبحث را به زبان ساده و گام‌به‌گام توضیح بده: ")
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

    fun openApiKeyDialog() {
        _uiState.value = _uiState.value.copy(
            showApiKeyDialog = true,
            showChatOptionsMenu = false
        )
    }

    fun dismissApiKeyDialog() {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = false)
    }

    fun saveApiKeyAndModel(key: String, model: String) {
        GeminiClient.saveCustomApiKey(getApplication(), key)
        GeminiClient.saveSelectedModel(getApplication(), model)
        _uiState.value = _uiState.value.copy(
            customApiKey = key.trim(),
            selectedModel = model,
            showApiKeyDialog = false
        )
        showTopBanner("تنظیمات Google AI Studio ذخیره و فعال شد", Icons.Default.CheckCircle, Color(0xFF34D399))
    }

    fun clearApiKey() {
        GeminiClient.saveCustomApiKey(getApplication(), "")
        _uiState.value = _uiState.value.copy(
            customApiKey = "",
            showApiKeyDialog = false
        )
        showTopBanner("کلید اختصاصی حذف شد")
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
                showTopBanner("نام گفتگو به $newTitle تغییر یافت")
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
            val currentMsgs = _uiState.value.messages
            val index = currentMsgs.indexOfFirst { it.id == originalMessage.id }
            if (index >= 0) {
                for (i in (index + 1) until currentMsgs.size) {
                    chatDao.deleteMessage(currentMsgs[i].id)
                }
            }
            val updated = originalMessage.copy(content = newText, timestamp = System.currentTimeMillis())
            chatDao.updateMessage(updated)

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(isGenerating = true)
                showTopBanner("پیام ویرایش شد، در حال دریافت پاسخ...")
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

            // Call Gemini API smoothly
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
        generationJob?.cancel()
        val assistantMessageId = UUID.randomUUID().toString()

        // Insert initial placeholder message for instant UI feedback
        val placeholder = MessageEntity(
            id = assistantMessageId,
            chatId = chatId,
            role = "assistant",
            content = "",
            timestamp = System.currentTimeMillis(),
            isWebSearch = isWebSearch
        )
        chatDao.insertMessage(placeholder)

        val result = GeminiClient.executeGenerateContent(
            context = getApplication(),
            prompt = userPrompt,
            imageUri = imageUri,
            isThink = isThinkHarder,
            isWebSearch = isWebSearch,
            inMemoryKey = _uiState.value.customApiKey
        )

        var finalReplyText: String
        var finalThinking: String? = null

        if (result.isSuccess) {
            val genResult = result.getOrNull()!!
            finalReplyText = genResult.text
            finalThinking = genResult.thinkingProcess
        } else {
            val exception = result.exceptionOrNull()
            val errorMsg = exception?.message ?: "خطای ناشناخته در اتصال به Google AI Studio"

            if (errorMsg.contains("کلید Google AI Studio API تنظیم نشده است", ignoreCase = true) ||
                errorMsg.contains("معتبر نیست", ignoreCase = true)
            ) {
                finalReplyText = """
                    ⚠️ **توجه: کلید Google AI Studio API تنظیم نشده است**
                    
                    برای دریافت پاسخ‌های زنده و واقعی، لطفاً کلید API رایگان خود را از [Google AI Studio](https://aistudio.google.com/apikey) دریافت کرده و در بخش تنظیمات وارد فرمایید.
                    
                    🔹 **نحوه فعال‌سازی:**
                    ۱. منوی کشویی سمت چپ را باز کنید
                    ۲. روی آیکون ⚙️ تنظیمات در پایین کلیک کنید
                    ۳. کلید API خود را وارد کرده و دکمه ذخیره را بزنید.
                """.trimIndent()
                withContext(Dispatchers.Main) {
                    showTopBanner("کلید Google AI Studio تنظیم نشده است", Icons.Default.Key, Color(0xFFF59E0B))
                }
            } else {
                finalReplyText = "❌ **خطا در ارتباط با سرور:**\n$errorMsg\n\nلطفاً اتصال اینترنت خود را بررسی نموده و مجدداً تلاش فرمایید."
            }
        }

        // Smooth simulated typing WITHOUT hammering SQLite:
        // Update database in 3-4 progressive batches instead of 100 times,
        // so UI stays 100% fluid and no scroll teleporting occurs.
        val totalLen = finalReplyText.length
        if (totalLen > 60) {
            val steps = 5
            val stepSize = totalLen / steps
            for (step in 1..steps) {
                delay(60)
                val currentChunk = finalReplyText.substring(0, (step * stepSize).coerceAtMost(totalLen))
                chatDao.updateMessage(
                    placeholder.copy(
                        content = currentChunk,
                        thinkingContent = finalThinking
                    )
                )
            }
        }

        // Final complete update
        chatDao.updateMessage(
            placeholder.copy(
                content = finalReplyText,
                thinkingContent = finalThinking
            )
        )

        withContext(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(isGenerating = false)
            if (_uiState.value.showVoiceMode) {
                _uiState.value = _uiState.value.copy(
                    voiceListeningState = VoiceState.SPEAKING,
                    voiceTranscript = finalReplyText
                )
                ttsManager.speak(assistantMessageId, finalReplyText)
            }
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
                showTopBanner("در حال بازتولید پاسخ با Gemini...")
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
        showTopBanner("در حال خواندن پاسخ هوشمند", Icons.Default.AutoAwesome)
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
        messageCollectionJob?.cancel()
        generationJob?.cancel()
        bannerJob?.cancel()
        ttsManager.shutdown()
    }
}

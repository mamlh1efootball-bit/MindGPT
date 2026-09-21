package com.example.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatOptionsMenu
import com.example.ui.components.ChatSideDrawer
import com.example.ui.components.ChatTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GetPlusDialog
import com.example.ui.components.ImagesGalleryDialog
import com.example.ui.components.MessageItem
import com.example.ui.components.MessageOptionsPopup
import com.example.ui.components.VoiceModeOverlay
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allChats by viewModel.allChats.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showImagesStudio by remember { mutableStateOf(false) }

    // Android Photo Picker (zero-permission standard)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.attachImage(uri)
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // Save temporary bitmap to cache and attach
            try {
                val tempFile = java.io.File(context.cacheDir, "camera_snap_${System.currentTimeMillis()}.jpg")
                val out = java.io.FileOutputStream(tempFile)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                viewModel.attachImage(Uri.fromFile(tempFile))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Speech to text dictation launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onInputTextChange(spokenText)
            }
        }
    }

    // Auto-scroll when messages change or new generation finishes
    LaunchedEffect(uiState.messages.size, uiState.isGenerating) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ChatSideDrawer(
                chats = allChats,
                activeChatId = uiState.currentChat?.id,
                onSelectChat = { chat ->
                    viewModel.selectChat(chat)
                    coroutineScope.launch { drawerState.close() }
                },
                onNewChatClick = {
                    viewModel.startNewChat()
                    coroutineScope.launch { drawerState.close() }
                },
                onImagesClick = {
                    showImagesStudio = true
                    coroutineScope.launch { drawerState.close() }
                },
                onSearchClick = {
                    viewModel.toggleChatSearch(true)
                    coroutineScope.launch { drawerState.close() }
                },
                onSettingsClick = {
                    viewModel.toggleApiKeyDialog(true)
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    ChatTopBar(
                        hasActiveChat = uiState.messages.isNotEmpty(),
                        isPlayingAudio = uiState.isPlayingAudio,
                        onMenuClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        onNewChatClick = { viewModel.startNewChat() },
                        onOptionsClick = { viewModel.toggleChatOptionsMenu() },
                        onGetPlusClick = { viewModel.toggleGetPlusDialog(true) },
                        onStopAudioClick = { viewModel.stopTts() }
                    )

                    // In-chat search bar
                    if (uiState.isSearchingChat) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ChatSurfaceElevated)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search in chat",
                                tint = TextSecondaryGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = uiState.chatSearchQuery,
                                onValueChange = { viewModel.setChatSearchQuery(it) },
                                placeholder = { Text("جستجو در گفتگو...", color = TextMuted, fontSize = 14.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextPrimaryWhite,
                                    unfocusedTextColor = TextPrimaryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.toggleChatSearch(false) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close search",
                                    tint = TextSecondaryGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                ChatInputBar(
                    inputText = uiState.inputText,
                    attachedImageUri = uiState.attachedImageUri,
                    isGenerating = uiState.isGenerating,
                    isThinkHarderEnabled = uiState.isThinkHarderEnabled,
                    showPlusMenu = uiState.showPlusMenu,
                    placeholderText = if (uiState.messages.isEmpty()) "Ask MindGPT" else "Reply to MindGPT",
                    onInputChange = { viewModel.onInputTextChange(it) },
                    onSend = { viewModel.sendMessage() },
                    onVoiceClick = { viewModel.toggleVoiceMode(true) },
                    onMicClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "صحبت کنید...")
                            }
                            speechLauncher.launch(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "سرویس تشخیص صدا در دسترس نیست", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onPlusClick = { viewModel.togglePlusMenu() },
                    onRemoveImage = { viewModel.attachImage(null) },
                    onCameraClick = {
                        viewModel.dismissPlusMenu()
                        try {
                            cameraLauncher.launch(null)
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در باز کردن دوربین", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onPhotosClick = {
                        viewModel.dismissPlusMenu()
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onFilesClick = {
                        viewModel.dismissPlusMenu()
                        Toast.makeText(context, "فایل‌ها: انتخاب سند متنی", Toast.LENGTH_SHORT).show()
                    },
                    onPluginsClick = {
                        viewModel.dismissPlusMenu()
                        Toast.makeText(context, "پلاگین‌ها فعال هستند", Toast.LENGTH_SHORT).show()
                    },
                    onToggleThinkHarder = { viewModel.toggleThinkHarder() },
                    modifier = Modifier.imePadding()
                )
            },
            containerColor = ChatOledBlack,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (uiState.showPlusMenu) {
                            viewModel.dismissPlusMenu()
                        }
                    }
            ) {
                val filteredMessages = if (uiState.chatSearchQuery.isNotBlank()) {
                    uiState.messages.filter { it.content.contains(uiState.chatSearchQuery, ignoreCase = true) }
                } else {
                    uiState.messages
                }

                if (filteredMessages.isEmpty() && !uiState.isGenerating) {
                    // Empty State with suggestions matching Screenshot 11
                    EmptyStateView(
                        onSuggestionClick = { prompt ->
                            viewModel.onInputTextChange(prompt)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                } else {
                    // Message Stream
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(filteredMessages, key = { it.id }) { message ->
                            MessageItem(
                                message = message,
                                onMenuClick = { viewModel.openMessageMenu(message) },
                                onPlayTts = { viewModel.playTts(message) },
                                onToggleLike = { viewModel.toggleLike(message) },
                                onToggleDislike = { viewModel.toggleDislike(message) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Modals
    if (uiState.showChatOptionsMenu && uiState.currentChat != null) {
        val chat = uiState.currentChat!!
        ChatOptionsMenu(
            chat = chat,
            onDismiss = { viewModel.dismissChatOptionsMenu() },
            onShare = {
                viewModel.dismissChatOptionsMenu()
                val shareText = uiState.messages.joinToString("\n\n") { "${it.role}: ${it.content}" }
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(intent, "اشتراک گفتگو"))
            },
            onPin = { viewModel.togglePinChat(chat) },
            onAddToProject = {
                viewModel.dismissChatOptionsMenu()
                Toast.makeText(context, "به پروژه افزوده شد", Toast.LENGTH_SHORT).show()
            },
            onUploadedFiles = {
                viewModel.dismissChatOptionsMenu()
                val imageCount = uiState.messages.count { !it.imageUri.isNullOrEmpty() }
                Toast.makeText(context, "تعداد فایل‌های تصویر: $imageCount", Toast.LENGTH_SHORT).show()
            },
            onFindInChat = { viewModel.toggleChatSearch(true) },
            onAddToHome = {
                viewModel.dismissChatOptionsMenu()
                Toast.makeText(context, "میانبر به صفحه اصلی اضافه شد", Toast.LENGTH_SHORT).show()
            },
            onArchive = { viewModel.toggleArchiveChat(chat) },
            onDelete = { viewModel.deleteChat(chat) }
        )
    }

    if (uiState.selectedMessageForMenu != null) {
        val message = uiState.selectedMessageForMenu!!
        MessageOptionsPopup(
            message = message,
            onDismiss = { viewModel.dismissMessageMenu() },
            onBranch = { viewModel.branchChat(message) },
            onRetry = { viewModel.retryAssistantMessage(message) },
            onSearchWeb = { viewModel.searchWebForMessage(message) }
        )
    }

    if (uiState.showGetPlusDialog) {
        GetPlusDialog(
            onDismiss = { viewModel.toggleGetPlusDialog(false) },
            onConfigureApiKey = { viewModel.toggleApiKeyDialog(true) }
        )
    }

    if (uiState.showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = uiState.customApiKey,
            onDismiss = { viewModel.toggleApiKeyDialog(false) },
            onSave = { key -> viewModel.setCustomApiKey(key) }
        )
    }

    if (showImagesStudio) {
        ImagesGalleryDialog(
            onDismiss = { showImagesStudio = false },
            onSelectPrompt = { prompt ->
                viewModel.sendMessage(promptOverride = prompt)
            }
        )
    }

    if (uiState.showVoiceMode) {
        VoiceModeOverlay(
            voiceState = uiState.voiceListeningState,
            transcript = uiState.voiceTranscript,
            onClose = { viewModel.toggleVoiceMode(false) },
            onSendVoicePrompt = { prompt -> viewModel.sendVoicePrompt(prompt) }
        )
    }
}

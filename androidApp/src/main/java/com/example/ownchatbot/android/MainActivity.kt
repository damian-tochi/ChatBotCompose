package com.example.ownchatbot.android

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.runtime.State
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.ownchatbot.android.data.PreferencesManager
import com.example.ownchatbot.android.ui.DarkESTMaritineBlue
import com.example.ownchatbot.android.ui.DarkMaritineBlue
import com.example.ownchatbot.android.ui.GeminiChatBotTheme
import com.example.ownchatbot.android.ui.LightNavy
import com.example.ownchatbot.android.ui.LightNavyTF
import com.example.ownchatbot.android.ui.MaritineBlue
import com.example.ownchatbot.android.ui.NormalMaritineBlue
import com.example.ownchatbot.android.ui.Purple40
import com.example.ownchatbot.android.view.ChatEvent
import com.example.ownchatbot.android.view.ChatViewModel
import com.example.ownchatbot.android.view.TextToSpeechState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")

    private val imagePicker =
        registerForActivityResult<PickVisualMediaRequest, Uri>(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                uriState.update { uri.toString() }
            }
        }

    private var textToSpeech: TextToSpeech? = null
    private val _state = mutableStateOf(TextToSpeechState())
    private val state: State<TextToSpeechState> = _state


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiChatBotTheme {
                val chaViewModel = viewModel<ChatViewModel>()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkESTMaritineBlue
                ) {
                    Scaffold(modifier = Modifier.background(DarkESTMaritineBlue), topBar = {
                        AppBar(chaViewModel)
                    }) {
                        ChatScreen(paddingValues = it, chaViewModel)
                    }

                }
            }
        }
    }


    @Composable
    fun ChatScreen(paddingValues: PaddingValues, chatViewModel: ChatViewModel) {

        val transition = rememberInfiniteTransition(label = "")
        val animatedProgress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(20500, easing = EaseInCirc),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val brush = Brush.linearGradient(
            colors = listOf(DarkMaritineBlue, DarkESTMaritineBlue),
            start = Offset(0f, Float.POSITIVE_INFINITY),
            end = Offset(Float.POSITIVE_INFINITY, 500f * animatedProgress)
        )

        val chatState = chatViewModel.chatState.collectAsState().value
        val bitmap: Bitmap? = getBitmap()

        Column(modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(brush),
            verticalArrangement = Arrangement.Bottom) {

            if (chatState.chatList.size > 0) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    reverseLayout = true) {
                    itemsIndexed(chatState.chatList) { _, chat ->
                        val saveDate = chat.time
                        val compareDay = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                        val today = compareDay.format(Date())
                        val savedTime = compareDay.format(saveDate)
                        val msgDateAndTime = if (TextUtils.equals(today, savedTime)) {
                            val sdf = SimpleDateFormat("hh:mma", Locale.ENGLISH)
                            sdf.format(saveDate)
                        } else {
                            val sdf = SimpleDateFormat("MMMM dd, yyyy 'hh:mma", Locale.ENGLISH)
                            sdf.format(saveDate)
                        }

                        if (chat.isFromUser) {
                            UserChatItem(
                                prompt = chat.prompt,
                                bitmap = chat.bitmap,
                                time = msgDateAndTime
                            )
                        } else {
                            ModelChatItem(
                                response = chat.prompt,
                                time = msgDateAndTime
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(R.mipmap.android_avatar),
                        modifier = Modifier
                            .size(115.dp)
                            .padding(2.dp)
                            .background(MaritineBlue, CircleShape),
                        contentDescription = "picked image",
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 0.dp, 6.dp),
                        textAlign = TextAlign.Center,
                        text = "Powered by Gemini AI",
                        fontSize = 10.sp,
                        color = LightNavy
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp, 10.dp, 10.dp, 0.dp),
                        textAlign = TextAlign.Center,
                        text = "How may i be of assistance?",
                        fontSize = 15.sp,
                        color = Color.White
                    )

                }
            }

            bitmap?.let {
                Box(modifier = Modifier.padding(10.dp)) {
                    Image(
                        modifier = Modifier
                            .size(70.dp)
                            .padding(bottom = 2.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentDescription = "picked image",
                        contentScale = ContentScale.Crop,
                        bitmap = it.asImageBitmap()
                    )
                }

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            imagePicker.launch(
                                PickVisualMediaRequest
                                    .Builder()
                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    .build()
                            )
                        },
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = "Add Photo",
                    tint = LightNavy
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightNavyTF,
                        unfocusedContainerColor = LightNavyTF,
                        disabledContainerColor = LightNavyTF,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                    ),
                    value = chatState.prompt,
                    onValueChange = {
                        chatViewModel.onEvent(ChatEvent.UpdatePrompt(it))
                    },
                    placeholder = {
                        Text(
                            text = "Type a prompt",
                            color = Color.White,
                            fontStyle = FontStyle.Italic
                        )
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            chatViewModel.onEvent(ChatEvent.SendPrompt(chatState.prompt, bitmap))
                            uriState.update { "" }
                        },
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send prompt",
                    tint = LightNavy
                )

            }

        }

    }

    @Composable
    fun UserChatItem(prompt: String, bitmap: Bitmap?, time: String?) {
        Column(modifier = Modifier.padding(start = 100.dp, bottom = 16.dp)) {

            bitmap?.let {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    bitmap = it.asImageBitmap()
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(elevation = 10.dp, RoundedCornerShape(8.dp))
                    .background(NormalMaritineBlue)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        text = prompt,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    time?.let {
                        Text(
                            modifier = Modifier
                                .height(18.dp)
                                .wrapContentWidth()
                                .align(Alignment.End),
                            text = it,
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ModelChatItem(response: String, time: String?) {
        val mContext = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        Column(
            modifier = Modifier.padding(end = 100.dp, bottom = 16.dp)
        ) {

            Icon(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.Transparent)
                    .padding(0.dp, 0.dp, 0.dp, 10.dp)
                    .clickable {
                        if (_state.value.isButtonEnabled) {
                            _state.value = state.value.copy(
                                isButtonEnabled = false,
                                text = response
                            )
                            textToSpeech(mContext)
                        }
                    },
                imageVector = if (_state.value.isButtonEnabled) {
                    Icons.AutoMirrored.Rounded.VolumeDown
                } else {
                    Icons.AutoMirrored.Rounded.VolumeUp
                },
                contentDescription = "Speak Text",
                tint = if (_state.value.isButtonEnabled) {
                    Color.White
                } else {
                    MaritineBlue
                }
            )

            Row(modifier = Modifier.align(Alignment.Start)) {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(elevation = 10.dp, RoundedCornerShape(8.dp))
                        .background(MaritineBlue)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Column(modifier = Modifier.fillMaxSize()) {

                        Row(modifier = Modifier.align(Alignment.Start)) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                text = response,
                                fontSize = 17.sp,
                                color = Color.White
                            )

                            Icon(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(response))
                                        Toast
                                            .makeText(mContext, "Text copied", Toast.LENGTH_LONG)
                                            .show()
                                    },
                                imageVector = Icons.AutoMirrored.Rounded.TextSnippet,
                                contentDescription = "Copy Text",
                                tint = LightNavy


                            )
                        }

                        Row(modifier = Modifier.align(Alignment.End)) {
                            Icon(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(response))
                                        Toast
                                            .makeText(mContext, "Text copied", Toast.LENGTH_LONG)
                                            .show()
                                    },
                                imageVector = Icons.AutoMirrored.Rounded.TextSnippet,
                                contentDescription = "Copy Text",
                                tint = LightNavy


                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                modifier = Modifier
                                    .height(18.dp)
                                    .wrapContentWidth()
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(response))
                                        Toast
                                            .makeText(mContext, "Text copied", Toast.LENGTH_LONG)
                                            .show()
                                    },
                                text = "Copy",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            time?.let {
                                Text(
                                    modifier = Modifier
                                        .height(18.dp)
                                        .wrapContentWidth(),
                                    text = it,
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                    }
                }

            }
        }
    }

    @Composable
    private fun getBitmap(): Bitmap? {
        val uri = uriState.collectAsState().value

        val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()
        ).state

        if (imageState is AsyncImagePainter.State.Success) {
            return imageState.result.drawable.toBitmap()
        }

        return null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppBar(chatViewModel: ChatViewModel) {
        val showDropDownMenu = remember { mutableStateOf(false) }
        rememberSystemUiController().setSystemBarsColor(color = DarkESTMaritineBlue)

        TopAppBar(
            { Text(text = "") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Purple40)
                .height(35.dp), actions = {
                IconButton(
                    onClick = { showDropDownMenu.value = true }) {
                    Icon(Icons.Filled.MoreVert, null, tint = LightNavy)

                }
                DropdownMenu(showDropDownMenu.value, { showDropDownMenu.value = false }) {
                    DropdownMenuItem(text = { Text(text = "Clear Chat") }, leadingIcon = {
                        Icon(Icons.Filled.DeleteForever, null, tint = LightNavy)
                    }, onClick = {
                        showDropDownMenu.value = false
                        chatViewModel.clear()
                    })
                }
            }, colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = DarkESTMaritineBlue)

        )

    }

    @Composable
    private fun UpdatePrefs() {
        val prefChatList = PreferencesManager(LocalContext.current).getChatStateFlow()
    }

    private fun textToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech?.let { txtToSpeech ->
                    txtToSpeech.language = Locale.getDefault()
                    txtToSpeech.setSpeechRate(1.0f)
                    txtToSpeech.speak(
                        _state.value.text,
                        TextToSpeech.QUEUE_ADD,
                        null,
                        99.toString()
                    )
                    txtToSpeech.setOnUtteranceProgressListener(object :
                        UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            if (TextUtils.equals(utteranceId, "99")) {
                                _state.value = state.value.copy(
                                    isButtonEnabled = false
                                )
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            if (TextUtils.equals(utteranceId, "99")) {
                                _state.value = state.value.copy(
                                    isButtonEnabled = true
                                )
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _state.value = state.value.copy(
                                isButtonEnabled = true
                            )
                        }
                    })

                }
            }

        }
    }

}

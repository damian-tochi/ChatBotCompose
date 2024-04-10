package com.example.ownchatbot.android

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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
import com.example.ownchatbot.android.ui.Darknavy
import com.example.ownchatbot.android.ui.GeminiChatBotTheme
import com.example.ownchatbot.android.ui.LightNavy
import com.example.ownchatbot.android.ui.MaritineBlue
import com.example.ownchatbot.android.ui.NormalMaritineBlue
import com.example.ownchatbot.android.ui.Purple40
import com.example.ownchatbot.android.view.ChatEvent
import com.example.ownchatbot.android.view.ChatViewModel
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
    private var isListCleared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiChatBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkESTMaritineBlue) {
                    Scaffold(modifier = Modifier.background(DarkESTMaritineBlue), topBar = {
                            AppBar()
                        }) {
                        ChatScreen(paddingValues = it)
                    }

                }
            }
        }
    }

    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chaViewModel = viewModel<ChatViewModel>()
        var chatState = chaViewModel.chatState.collectAsState().value

        val prefChatList = PreferencesManager(LocalContext.current).getChatStateFlow()


        val bitmap = getBitmap()

        if (isListCleared) {
           // chatState.chatList.clear()
            chaViewModel.chatState.collectAsState().value.chatList.clear()
            chatState = chaViewModel.chatState.collectAsState().value
            isListCleared = false
        }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .background(DarkESTMaritineBlue),
                verticalArrangement = Arrangement.Bottom) {

              if (chatState.chatList.size > 0) {
                  LazyColumn(
                      modifier = Modifier
                          .weight(1f)
                          .fillMaxWidth()
                          .padding(horizontal = 8.dp),
                      reverseLayout = true) {
                      itemsIndexed(chatState.chatList) { index, chat ->
                          //val sdf = SimpleDateFormat("'Date\n'dd-MM-yyyy '\n\nand\n\nTime\n'HH:mm:ss z", Locale.ENGLISH)
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
                                  bitmap = chat.bitmap,
                                  time = msgDateAndTime
                              )
                          }
                      }
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
                        .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
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
                        value = chatState.prompt,
                        onValueChange = {
                            chaViewModel.onEvent(ChatEvent.UpdatePrompt(it))
                        },
                        placeholder = {
                            Text(text = "Type a prompt")
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                chaViewModel.onEvent(ChatEvent.SendPrompt(chatState.prompt, bitmap))
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

            Box(modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .shadow(elevation = 10.dp, RoundedCornerShape(8.dp))
                .background(NormalMaritineBlue)
                .padding(10.dp),
                contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                        text = prompt,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    time?.let {
                        Text(modifier = Modifier
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
    fun ModelChatItem(response: String, bitmap: Bitmap?, time: String?) {
        val mContext = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        Column(
            modifier = Modifier.padding(end = 100.dp, bottom = 16.dp)) {

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

            Box(modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .shadow(elevation = 10.dp, RoundedCornerShape(8.dp))
                .background(MaritineBlue)
                .padding(10.dp),
                contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                        text = response,
                        fontSize = 17.sp,
                        color = Color.White
                    )

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
                        Text(modifier = Modifier
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
                            Text(modifier = Modifier
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
    fun AppBar() {
        val showDropDownMenu = remember { mutableStateOf(false) }
        rememberSystemUiController().setSystemBarsColor(color = DarkESTMaritineBlue)

        TopAppBar({ Text(text = "") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Purple40)
                .height(35.dp), actions = {
                IconButton(
                    onClick = { showDropDownMenu.value = true }) {
                    Icon(Icons.Filled.MoreVert, null, tint = LightNavy)

                }
                DropdownMenu(showDropDownMenu.value, { showDropDownMenu.value = false }) {
                    DropdownMenuItem( text = { Text(text = "Clear Chat") }, leadingIcon = {
                        Icon(Icons.Filled.DeleteForever, null, tint = LightNavy) }, onClick = {
                        showDropDownMenu.value = false
                        isListCleared = true

                    })
                }
            }, colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = DarkESTMaritineBlue)

        )

    }

}

package com.pranavkd.bustracker

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.pranavkd.bustracker.ChatLogic.ChatViewModel
import com.pranavkd.bustracker.ChatLogic.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScren(
    viewModel: ChatViewModel = viewModel(),
    sharedPreferences: SharedPreferences,
    navController: NavHostController
) {
    var bookingId by remember { mutableStateOf(sharedPreferences.getString("bookingId", "")!!) }
    if(bookingId == ""){
        navController.navigate("TrackingScreen")
    }
    viewModel.receiveMessage(bookingId = bookingId)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Chat With Conductor") })
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    viewModel.receiveMessage(bookingId = bookingId)
                },
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                    contentDescription = "Add"
                )
            }
        }
    ) { paddingValues ->
        val listState = rememberLazyListState()

        LaunchedEffect(viewModel.messageList.size) {
            listState.animateScrollToItem(viewModel.messageList.size )
        }
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                //reverseLayout = true
                state = listState
            ) {
                items(viewModel.messageList) { message ->
                    ChatMessageItem(message,bookingId)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            BottomSendView(
                onSendMessage = { message ->
                    viewModel.sendMessage(message,bookingId)
                }
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: Message, bookingId: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.direction == "send")
            Alignment.End else Alignment.Start
    ) {
        if (message.direction != "send") {
            Text(
                text = "Conductor",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }
        else{
            Text(
                text = "You: $bookingId",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, end = 16.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.direction == "send") 16.dp else 0.dp,
                bottomEnd = if (message.direction == "send") 0.dp else 16.dp
            ),
            color = if (message.direction == "send")
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.messageText,
                    color = if (message.direction == "send")
                        MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.direction == "send")
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun BottomSendView(modifier: Modifier = Modifier, onSendMessage: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Type a message...") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (message.isNotBlank()) {
                            onSendMessage(message)
                            message = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (message.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}
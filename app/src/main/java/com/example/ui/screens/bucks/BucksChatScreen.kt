package com.example.ui.screens.bucks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.FinancialSummary
import com.example.domain.service.BucksAssistantEngine
import com.example.domain.service.BucksChatMessage
import com.example.ui.theme.FintechBackground
import com.example.ui.theme.FintechCard
import com.example.ui.theme.FintechCardBorder
import com.example.ui.theme.FintechNeonLime
import com.example.ui.theme.FintechNeonLimeDark
import com.example.ui.theme.FintechSurface
import com.example.ui.theme.FintechSurfaceVariant
import com.example.ui.theme.FintechTextPrimary
import com.example.ui.theme.FintechTextSecondary
import com.example.ui.theme.FintechViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BucksChatScreen(
    summary: FinancialSummary,
    transactions: List<TransactionEntity>,
    bills: List<BillEntity>,
    goals: List<SavingsGoalEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages = remember {
        mutableStateListOf<BucksChatMessage>().apply {
            add(BucksAssistantEngine.INITIAL_GREETING)
        }
    }

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun sendMessage(text: String) {
        if (text.isBlank() || isTyping) return
        val userMsg = BucksChatMessage(
            isUser = true,
            text = text.trim()
        )
        messages.add(userMsg)
        inputText = ""
        isTyping = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            // Typing delay for natural conversational feel
            delay(650)
            val reply = BucksAssistantEngine.generateResponse(
                userQuery = text,
                summary = summary,
                transactions = transactions,
                bills = bills,
                goals = goals
            )
            messages.add(reply)
            isTyping = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .testTag("bucks_chat_screen")
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FintechSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FintechCard)
                    .testTag("bucks_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FintechTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bucks Green Dollar Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(FintechNeonLime)
                    .border(2.dp, FintechNeonLimeDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    color = FintechNeonLimeDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bucks",
                        style = MaterialTheme.typography.titleMedium,
                        color = FintechTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FintechNeonLime.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI WINGMAN",
                            color = FintechNeonLime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = if (isTyping) "cooking up advice..." else "always on point • no cap",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isTyping) FintechNeonLime else FintechTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(
                    message = msg,
                    onSuggestionClick = { prompt -> sendMessage(prompt) }
                )
            }

            if (isTyping) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = FintechNeonLime,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bucks is thinking...",
                            color = FintechTextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Quick Suggestions Horizontal Row
        val latestSuggestions = messages.lastOrNull { !it.isUser }?.quickSuggestions
        if (!latestSuggestions.isNullOrEmpty() && !isTyping) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(latestSuggestions) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(FintechCard)
                            .border(1.dp, FintechViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { sendMessage(prompt) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = prompt,
                            color = FintechViolet,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FintechSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask Bucks anything about your money...", color = FintechTextSecondary, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("bucks_chat_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FintechNeonLime,
                    unfocusedBorderColor = FintechCardBorder
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { sendMessage(inputText) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) FintechNeonLime else FintechCard)
                    .testTag("bucks_send_button"),
                enabled = inputText.isNotBlank() && !isTyping
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) FintechNeonLimeDark else FintechTextSecondary
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: BucksChatMessage,
    onSuggestionClick: (String) -> Unit
) {
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = timeFormatter.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(FintechNeonLime),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$",
                        color = FintechNeonLimeDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (message.isUser) 18.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 18.dp
                        )
                    )
                    .background(if (message.isUser) FintechViolet else FintechCard)
                    .border(
                        1.dp,
                        if (message.isUser) FintechViolet else FintechCardBorder,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.White else FintechTextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeStr,
                        color = if (message.isUser) Color.White.copy(alpha = 0.6f) else FintechTextSecondary.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

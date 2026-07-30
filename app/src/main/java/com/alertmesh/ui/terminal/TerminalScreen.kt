package com.alertmesh.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alertmesh.ui.theme.TerminalBg
import com.alertmesh.ui.theme.TerminalBgLight
import com.alertmesh.ui.theme.TerminalGreen
import com.alertmesh.ui.theme.TerminalTypography

@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel,
    onNavigateToEmergency: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.lines.size) {
        if (uiState.lines.isNotEmpty()) {
            listState.animateScrollToItem(uiState.lines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(uiState.lines) { line ->
                Text(
                    text = line.text,
                    style = TerminalTypography.bodyMedium,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        KeyRow(
            onKeyPress = { key ->
                if (key == "Tab") {
                    // Handle tab completion
                } else if (key == "Esc" || key == "Ctrl") {
                    // Handle modifiers
                } else {
                    viewModel.onInputChanged(uiState.currentInput + key)
                }
            },
            onHistoryUp = { /* implementation */ },
            onHistoryDown = { /* implementation */ }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBgLight)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> ",
                color = TerminalGreen,
                style = TerminalTypography.bodyMedium
            )
            BasicTextField(
                value = uiState.currentInput,
                onValueChange = { viewModel.onInputChanged(it) },
                textStyle = TerminalTypography.bodyMedium.copy(color = TerminalGreen),
                cursorBrush = SolidColor(TerminalGreen),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        val input = uiState.currentInput
                        if (input == "/sos") {
                            viewModel.onInputChanged("")
                            onNavigateToEmergency()
                        } else {
                            viewModel.onCommand(input)
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                singleLine = true
            )
        }
    }
}

package com.alertmesh.ui.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.alertmesh.protocol.model.Condition
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.ui.theme.SosRed
import com.alertmesh.ui.theme.TerminalBgLight
import com.alertmesh.ui.theme.TerminalTypography
import com.alertmesh.ui.theme.TerminalWhite

@Composable
fun TriageForm(
    onSubmit: (SosPayload) -> Unit,
    modifier: Modifier = Modifier
) {
    var peopleCount by remember { mutableStateOf("1") }
    var injuredCount by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(Condition.SAFE) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalBgLight)
            .padding(16.dp)
    ) {
        Text("People Count:", color = TerminalWhite, style = TerminalTypography.bodyMedium)
        BasicTextField(
            value = peopleCount,
            onValueChange = { peopleCount = it },
            textStyle = TerminalTypography.bodyMedium.copy(color = TerminalWhite),
            cursorBrush = SolidColor(TerminalWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(TerminalBgLight)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("Injured Count:", color = TerminalWhite, style = TerminalTypography.bodyMedium)
        BasicTextField(
            value = injuredCount,
            onValueChange = { injuredCount = it },
            textStyle = TerminalTypography.bodyMedium.copy(color = TerminalWhite),
            cursorBrush = SolidColor(TerminalWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(TerminalBgLight)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("Note (Max 100):", color = TerminalWhite, style = TerminalTypography.bodyMedium)
        BasicTextField(
            value = note,
            onValueChange = { if (it.length <= 100) note = it },
            textStyle = TerminalTypography.bodyMedium.copy(color = TerminalWhite),
            cursorBrush = SolidColor(TerminalWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(TerminalBgLight)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSubmit(
                    SosPayload(
                        peopleCount = peopleCount.toIntOrNull() ?: 1,
                        injuredCount = injuredCount.toIntOrNull() ?: 0,
                        condition = condition,
                        note = note,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SosRed)
        ) {
            Text("SEND SOS", color = TerminalWhite, style = TerminalTypography.bodyLarge)
        }
    }
}

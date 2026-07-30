package com.alertmesh.ui.emergency

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.protocol.model.Condition
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.ui.theme.AppleCardBg
import com.alertmesh.ui.theme.AppleCyan
import com.alertmesh.ui.theme.AppleGreen
import com.alertmesh.ui.theme.AppleInputBg
import com.alertmesh.ui.theme.AppleRed
import com.alertmesh.ui.theme.AppleRedGlow
import com.alertmesh.ui.theme.AppleWindowBg
import com.alertmesh.ui.theme.AppleYellow
import com.alertmesh.ui.theme.TerminalFontFamily
import com.alertmesh.ui.theme.TerminalTypography
import com.alertmesh.ui.theme.TextPrimary
import com.alertmesh.ui.theme.TextSecondary

@Composable
fun TriageForm(
    onSubmit: (SosPayload) -> Unit,
    modifier: Modifier = Modifier
) {
    var peopleCount by remember { mutableIntStateOf(1) }
    var injuredCount by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf(Condition.TRAPPED) }

    val conditions = Condition.entries.toTypedArray()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppleCardBg)
            .border(BorderStroke(1.dp, AppleRedGlow), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = " EMERGENCY TRIAGE REPORT",
            fontFamily = TerminalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = AppleRed
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Condition Chips
        Text(
            text = "CONDITION:",
            fontFamily = TerminalFontFamily,
            fontSize = 10.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (c in conditions) {
                val isSelected = c == selectedCondition
                val chipColor = when (c) {
                    Condition.TRAPPED, Condition.INJURED -> AppleRed
                    Condition.NEED_MEDICAL, Condition.NEED_WATER -> AppleYellow
                    Condition.SAFE -> AppleGreen
                }

                Surface(
                    modifier = Modifier.clickable { selectedCondition = c },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) chipColor.copy(alpha = 0.2f) else AppleInputBg,
                    border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) chipColor else TextSecondary.copy(alpha = 0.3f))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = c.name,
                            fontFamily = TerminalFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (isSelected) chipColor else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // People & Injured Count Spinners
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CounterPicker(
                label = "TOTAL PEOPLE",
                count = peopleCount,
                onIncrement = { peopleCount++ },
                onDecrement = { if (peopleCount > 1) peopleCount-- }
            )

            CounterPicker(
                label = "INJURED COUNT",
                count = injuredCount,
                onIncrement = { injuredCount++ },
                onDecrement = { if (injuredCount > 0) injuredCount-- }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Note Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("NOTE:", fontFamily = TerminalFontFamily, fontSize = 10.sp, color = TextSecondary)
            Text("${note.length}/100", fontFamily = TerminalFontFamily, fontSize = 10.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AppleInputBg)
                .border(BorderStroke(0.5.dp, AppleCyan.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            BasicTextField(
                value = note,
                onValueChange = { if (it.length <= 100) note = it },
                textStyle = TerminalTypography.bodyMedium.copy(color = TextPrimary, fontSize = 12.sp),
                cursorBrush = SolidColor(AppleCyan),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                onSubmit(
                    SosPayload(
                        peopleCount = peopleCount,
                        injuredCount = injuredCount,
                        condition = selectedCondition,
                        note = note,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
        ) {
            Text(
                text = "BROADCAST SOS EMERGENCY MESH ALERT",
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CounterPicker(
    label: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column {
        Text(text = label, fontFamily = TerminalFontFamily, fontSize = 10.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AppleInputBg)
                .border(BorderStroke(0.5.dp, TextSecondary.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppleWindowBg)
                    .clickable { onDecrement() },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = TextPrimary, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "$count", color = TextPrimary, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppleWindowBg)
                    .clickable { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = TextPrimary, fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold)
            }
        }
    }
}

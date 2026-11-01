package com.alertmesh.ui.emergency

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.protocol.model.Condition
import com.alertmesh.protocol.model.SosPayload
import com.alertmesh.ui.theme.AccentCyan
import com.alertmesh.ui.theme.AccentGreen
import com.alertmesh.ui.theme.AccentRed
import com.alertmesh.ui.theme.AccentAmber
import com.alertmesh.ui.theme.BgElevated
import com.alertmesh.ui.theme.BgSurface
import com.alertmesh.ui.theme.BgPrimary
import com.alertmesh.ui.theme.AppFontFamily
import com.alertmesh.ui.theme.TerminalTypography
import com.alertmesh.ui.theme.TextPrimary
import com.alertmesh.ui.theme.TextSecondary
import com.alertmesh.ui.theme.BorderSubtle
import com.alertmesh.ui.theme.BorderDefault

@Composable
fun TriageForm(
    onSubmit: (SosPayload) -> Unit,
    modifier: Modifier = Modifier
) {
    var peopleCount by remember { mutableIntStateOf(1) }
    var injuredCount by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf<Condition?>(null) }

    val conditions = Condition.entries.toTypedArray()
    val motionEnabled = Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgElevated)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "Most urgent need",
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Text("Choose one", fontFamily = AppFontFamily, fontSize = 13.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(BgSurface)
        ) {
            for ((index, c) in conditions.withIndex()) {
                val isSelected = c == selectedCondition
                val chipColor = when (c) {
                    Condition.TRAPPED, Condition.INJURED -> AccentRed
                    Condition.NEED_MEDICAL, Condition.NEED_WATER -> AccentAmber
                    Condition.SAFE -> AccentGreen
                }
                val rowColor by animateColorAsState(
                    targetValue = if (isSelected) chipColor.copy(alpha = 0.10f) else BgSurface,
                    animationSpec = if (motionEnabled) tween(180) else snap(),
                    label = "Condition selection"
                )

                Surface(
                    modifier = Modifier.fillMaxWidth().selectable(
                        selected = isSelected, role = Role.RadioButton,
                        onClick = { selectedCondition = c }
                    ),
                    shape = RectangleShape,
                    color = rowColor
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp)) {
                        Text(text = c.name.replace('_', ' '), fontFamily = AppFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp, color = if (isSelected) chipColor else TextPrimary,
                            modifier = Modifier.weight(1f))
                        if (motionEnabled) {
                            AnimatedVisibility(isSelected, enter = fadeIn(tween(150)), exit = fadeOut(tween(80))) {
                                Text("✓", color = chipColor, fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            }
                        } else if (isSelected) {
                            Text("✓", color = chipColor, fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    }
                }
                if (index < conditions.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(BorderDefault))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // People & Injured Count Spinners
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CounterPicker(
                label = "People",
                count = peopleCount,
                onIncrement = { peopleCount++ },
                onDecrement = { if (peopleCount > 1) { peopleCount--; injuredCount = injuredCount.coerceAtMost(peopleCount) } }
            )

            CounterPicker(
                label = "Injured",
                count = injuredCount,
                onIncrement = { if (injuredCount < peopleCount) injuredCount++ },
                onDecrement = { if (injuredCount > 0) injuredCount-- }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Details (optional)", fontFamily = AppFontFamily, fontSize = 12.sp, color = TextSecondary)
            Text("${note.length}/100", fontFamily = AppFontFamily, fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BgSurface)
                .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            BasicTextField(
                value = note,
                onValueChange = { if (it.length <= 100) note = it },
                textStyle = TerminalTypography.bodyMedium.copy(color = TextPrimary, fontSize = 15.sp),
                cursorBrush = SolidColor(AccentCyan),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner -> Box {
                    if (note.isEmpty()) Text("Landmark, injuries, supplies needed…", color = TextSecondary,
                        fontFamily = AppFontFamily, fontSize = 14.sp)
                    inner()
                } }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "The alert is submitted to the local mesh. Delivery requires connected peers.",
            fontFamily = AppFontFamily,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            enabled = selectedCondition != null,
            onClick = {
                val condition = selectedCondition ?: return@Button
                onSubmit(
                    SosPayload(
                        peopleCount = peopleCount,
                        injuredCount = injuredCount,
                        condition = condition,
                        note = note,
                        latitude = null,
                        longitude = null,
                        accuracy = null,
                        fixAgeSeconds = null
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
        ) {
            Text(
                text = "Send SOS",
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BgPrimary
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
        Text(text = label, fontFamily = AppFontFamily, fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(BgSurface)
                .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BgPrimary)
                    .clickable { onDecrement() }
                    .semantics { contentDescription = "Decrease $label" },
                contentAlignment = Alignment.Center
            ) {
                Text("−", color = TextPrimary, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "$count", color = TextPrimary, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BgPrimary)
                    .clickable { onIncrement() }
                    .semantics { contentDescription = "Increase $label" },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = TextPrimary, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
            }
        }
    }
}

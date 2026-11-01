package com.alertmesh.ui.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertmesh.ui.theme.*

@Composable
fun EmergencyScreen(viewModel: EmergencyViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().background(BgPrimary).windowInsetsPadding(WindowInsets.systemBars)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("‹  Back", color = AccentCyan, fontFamily = AppFontFamily, fontSize = 16.sp,
                modifier = Modifier.clickable(onClick = onNavigateBack).padding(end = 18.dp))
            Text("Emergency", color = TextPrimary, fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)) {
            if (state.isAlarmActive) {
                Text(if (state.senderFingerprint == "SELF") "SOS submitted" else "Incoming SOS",
                    color = AccentRed, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(state.deliveryMessage ?: "Delivery status unavailable", color = AccentAmber,
                    fontFamily = AppFontFamily, fontSize = 15.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(22.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgElevated)
                    .border(1.dp, BorderDefault, RoundedCornerShape(18.dp)).padding(20.dp)) {
                    Text("REPORT DETAILS", color = TextDim, fontFamily = AppFontFamily,
                        fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(16.dp))
                    state.incomingSos?.let { sos ->
                        ReportRow("Source", state.senderFingerprint)
                        ReportRow("Condition", sos.condition.name.replace('_', ' '))
                        ReportRow("People", "${sos.peopleCount} total · ${sos.injuredCount} injured")
                        ReportRow("Location", if (sos.latitude != null && sos.longitude != null)
                            "${sos.latitude}, ${sos.longitude}" else "No location fix")
                        if (sos.note.isNotBlank()) ReportRow("Note", sos.note)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Button(onClick = viewModel::dismissAlarm, modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) {
                    Text("Dismiss report", color = BgPrimary, fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text("Return to activity", color = AccentCyan, fontFamily = AppFontFamily,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateBack)
                        .padding(vertical = 14.dp), fontSize = 15.sp)
            } else {
                Text("Request help", color = TextPrimary, fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text("Share essential details with nearby AlertMesh devices. Review your report before sending.",
                    color = TextSecondary, fontFamily = AppFontFamily, fontSize = 15.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(20.dp))
                state.error?.let { error ->
                    Text(error, color = AccentRed, fontFamily = AppFontFamily, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                }
                TriageForm(onSubmit = viewModel::triggerSos)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Text(label.uppercase(), color = TextDim, fontFamily = AppFontFamily, fontSize = 11.sp,
        letterSpacing = 1.sp)
    Spacer(Modifier.height(4.dp))
    Text(value, color = TextPrimary, fontFamily = AppFontFamily, fontSize = 16.sp)
    Spacer(Modifier.height(16.dp))
}

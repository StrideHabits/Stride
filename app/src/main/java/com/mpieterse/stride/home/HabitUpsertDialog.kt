package com.mpieterse.stride.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpieterse.stride.R

@Composable
fun HabitUpsertDialog(onDismiss: () -> Unit, onConfirm: (String, Int, String?, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Health & Fitness") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    Surface(color = Color(0x80000000)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.upsert_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.upsert_name_placeholder)) })

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = frequency, onValueChange = { frequency = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.upsert_frequency_placeholder)) })

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = tag, onValueChange = { tag = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.upsert_tag_placeholder)) }, readOnly = true)

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = stringResource(R.string.upsert_attach_image))
                        TextButton(onClick = { /* TODO(media): open gallery picker and set imageUri */ }) { Text(stringResource(R.string.upsert_browse)) }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.upsert_cancel)) }
                        Button(onClick = { onConfirm(name, frequency.toIntOrNull() ?: 0, tag, imageUri) }, enabled = name.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                            Text(text = stringResource(R.string.upsert_add))
                        }
                    }
                }
            }
        }
    }
}



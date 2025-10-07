package com.mpieterse.stride.ui.layout.shared.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpieterse.stride.R
import java.io.InputStream

@Composable
fun ImagePicker(
    selectedImage: Bitmap?,
    onImageSelected: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tempSelectedImage by remember { mutableStateOf<Bitmap?>(selectedImage) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(selectedUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                tempSelectedImage = bitmap
                onImageSelected(bitmap)
            } catch (e: Exception) {
                // Handle error - image could not be loaded
                tempSelectedImage = null
                onImageSelected(null)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = Color.Gray.copy(alpha = 0.03f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                imagePickerLauncher.launch("image/*")
            }
    ) {
        if (tempSelectedImage != null) {
            Image(
                bitmap = tempSelectedImage!!.asImageBitmap(),
                contentDescription = "Selected habit image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.xic_uic_outline_plus),
                    contentDescription = "Add image",
                    tint = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Attach an image",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

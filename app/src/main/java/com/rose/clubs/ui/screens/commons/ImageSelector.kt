package com.rose.clubs.ui.screens.commons

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rose.clubs.R

private const val TAG = "ImageSelector"

@Composable
fun ImageSelector(modifier: Modifier, imageUri: String, onImageSelected: (selectedUri: String) -> Unit) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        Log.i(TAG, "onImageSelected: $it")
        onImageSelected(it.toString())
    }

    Row(modifier = modifier) {
        AsyncImage(
            model = imageUri,
            contentDescription = "",
            placeholder = painterResource(id = R.drawable.profile_picture),
            error = painterResource(id = R.drawable.profile_picture),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(88.dp)
                .padding(4.dp)
                .clip(CircleShape)
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(16.dp)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.width(100.dp),
                    text = imageUri.ifEmpty { "No image" },
                    style = MaterialTheme.typography.subtitle2,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select image",
                style = MaterialTheme.typography.subtitle2.copy(
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colors.secondary,
                ),
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable {
                        galleryLauncher.launch("image/*")
                    }
            )
        }
    }
}

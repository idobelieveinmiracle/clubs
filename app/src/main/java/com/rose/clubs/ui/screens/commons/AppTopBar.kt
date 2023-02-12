package com.rose.clubs.ui.screens.commons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(
    text: String,
    icon: ImageVector,
    iconDescription: String = icon.name,
    secondaryIcon: ImageVector? = null,
    onSecondaryIconClick: () -> Unit = {},
    onIconClick: () -> Unit
) {
    Row(Modifier.padding(5.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            modifier = Modifier
                .padding(10.dp)
                .clickable { onIconClick() }
                .padding(4.dp)
                .align(Alignment.CenterVertically)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        if (secondaryIcon != null) {
            Icon(
                imageVector = secondaryIcon,
                contentDescription = secondaryIcon.name,
                modifier = Modifier
                    .padding(10.dp)
                    .clickable { onSecondaryIconClick() }
                    .padding(4.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Preview
@Composable
fun AppTopBarPreview() {
    AppTopBar(
        text = "Top bar",
        icon = Icons.Filled.ArrowBack,
        "Icon",
        secondaryIcon = Icons.Filled.Email
    ) {
    }
}
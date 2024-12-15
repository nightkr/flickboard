package se.nullable.flickboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.R

@Composable
fun MenuPageLink(
    onClick: () -> Unit,
    icon: Painter?,
    label: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    Box(modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, iconModifier)
            }
            Text(
                label,
                Modifier
                    .weight(1F)
                    .padding(horizontal = 8.dp),
            )
            Icon(Icons.AutoMirrored.Default.ArrowForward, null)
        }
    }
}

@Composable
@Preview
fun MenuPageLinkPreview() {
    Surface {
        Column {
            MenuPageLink(
                onClick = {},
                icon = painterResource(R.drawable.baseline_image_search_24),
                label = "Section",
            )
            MenuPageLink(
                onClick = {},
                icon = null,
                label = "Section",
            )
        }
    }
}
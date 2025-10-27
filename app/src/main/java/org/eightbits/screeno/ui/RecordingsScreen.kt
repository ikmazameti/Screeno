package org.eightbits.screeno.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.eightbits.screeno.ui.theme.DarkBackground
import org.eightbits.screeno.ui.theme.ScreenoBlue
import org.eightbits.screeno.ui.theme.TextLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    recordings: List<String>,
    onPlay: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(recordings) { fileName ->
                ListItem(
                    headlineContent = { Text(fileName, color = TextLight) },
                    leadingContent = {
                        Icon(Icons.Default.VideoFile, contentDescription = null, tint = ScreenoBlue)
                    },
                    trailingContent = {
                        IconButton(onClick = { onPlay(fileName) }) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White
                            )
                        }
                    }
                )
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = Color.Gray.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordingsPreview() {
    RecordingsScreen(
        recordings = listOf("recording1.mp4", "recording2.mp4"),
        onPlay = {},
        onBack = {})
}

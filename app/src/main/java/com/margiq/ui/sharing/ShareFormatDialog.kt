package com.margiq.ui.sharing

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.margiq.domain.sharing.HtmlEmailFormatter
import com.margiq.domain.sharing.RichTextShareFormatter
import com.margiq.domain.sharing.ShareReport
import com.margiq.ui.sharing.image.ShareImageAppearance
import com.margiq.ui.sharing.image.ShareImageRenderer
import kotlinx.coroutines.launch

enum class ShareFormatType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    RICH_TEXT(
        title = "Rich Text",
        description = "Clean, readable text for WhatsApp, Telegram, SMS & messaging",
        icon = Icons.Default.Notes
    ),
    IMAGE(
        title = "Summary Card Image",
        description = "High-resolution branded financial card for professional presentation",
        icon = Icons.Default.Image
    ),
    EMAIL(
        title = "Email (HTML)",
        description = "Formatted email table layout with plain-text fallback",
        icon = Icons.Default.Email
    )
}

@Composable
fun ShareFormatDialog(
    report: ShareReport,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(ShareFormatType.RICH_TEXT) }
    var imageAppearance by remember { mutableStateOf(ShareImageAppearance.FOLLOW_APP) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismissRequest() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Column {
                    Text(
                        text = "Share Report",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = report.title.take(35),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Format Selection Options
                ShareFormatType.entries.forEach { format ->
                    val isSelected = selectedFormat == format
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isProcessing) { selectedFormat = format },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = format.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = format.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = format.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (!isProcessing) selectedFormat = format }
                            )
                        }
                    }
                }

                // Sub-options when Image is selected
                if (selectedFormat == ShareFormatType.IMAGE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Card Appearance",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShareImageAppearance.entries.forEach { appearance ->
                            FilterChip(
                                selected = imageAppearance == appearance,
                                onClick = { imageAppearance = appearance },
                                label = { Text(appearance.label, fontSize = 11.sp) },
                                leadingIcon = if (imageAppearance == appearance) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (selectedFormat) {
                        ShareFormatType.RICH_TEXT -> {
                            OutlinedButton(
                                onClick = {
                                    val text = RichTextShareFormatter.format(report)
                                    ShareCoordinator.copyToClipboard(context, text)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    onDismissRequest()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                Text("Copy", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    val text = RichTextShareFormatter.format(report)
                                    ShareCoordinator.shareText(context, text, "Share ${report.title}")
                                    onDismissRequest()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                Text("Share", fontSize = 13.sp)
                            }
                        }

                        ShareFormatType.IMAGE -> {
                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    scope.launch {
                                        try {
                                            val bitmap = ShareImageRenderer.render(context, report, imageAppearance)
                                            val result = ImageSaveManager.saveImageToPictures(context, bitmap, report.title.filter { it.isLetterOrDigit() }.take(15))
                                            result.onSuccess { path ->
                                                Toast.makeText(context, path, Toast.LENGTH_LONG).show()
                                            }.onFailure { err ->
                                                Toast.makeText(context, "Save failed: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                        } finally {
                                            isProcessing = false
                                            onDismissRequest()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                Text("Save", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    isProcessing = true
                                    scope.launch {
                                        try {
                                            val bitmap = ShareImageRenderer.render(context, report, imageAppearance)
                                            ShareCoordinator.shareImage(context, bitmap, report.title)
                                        } finally {
                                            isProcessing = false
                                            onDismissRequest()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                Text("Share", fontSize = 13.sp)
                            }
                        }

                        ShareFormatType.EMAIL -> {
                            Button(
                                onClick = {
                                    val emailContent = HtmlEmailFormatter.format(report)
                                    ShareCoordinator.shareHtmlEmail(context, emailContent)
                                    onDismissRequest()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                Text("Draft Email", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        },
        modifier = modifier
    )
}


package com.example.markdownlint.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownToolbar(
    onInsertText: (prefix: String, suffix: String, defaultPlaceholder: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                icon = Icons.Default.Title,
                tag = "btn_heading",
                label = "H1",
                onClick = { onInsertText("# ", "", "Heading") }
            )
            ToolbarButton(
                icon = Icons.Default.Title,
                tag = "btn_heading2",
                label = "H2",
                onClick = { onInsertText("## ", "", "Heading 2") }
            )
            ToolbarButton(
                icon = Icons.Default.FormatBold,
                tag = "btn_bold",
                label = "Bold",
                onClick = { onInsertText("**", "**", "bold text") }
            )
            ToolbarButton(
                icon = Icons.Default.FormatItalic,
                tag = "btn_italic",
                label = "Italic",
                onClick = { onInsertText("*", "*", "italic text") }
            )
            ToolbarButton(
                icon = Icons.Default.Code,
                tag = "btn_code_span",
                label = "Code",
                onClick = { onInsertText("`", "`", "code") }
            )
            ToolbarButton(
                icon = Icons.Default.Code,
                tag = "btn_code_block",
                label = "Fence",
                onClick = { onInsertText("```kotlin\n", "\n```\n", "// Code block") }
            )
            ToolbarButton(
                icon = Icons.Default.FormatListBulleted,
                tag = "btn_bullet_list",
                label = "Bullet",
                onClick = { onInsertText("- ", "", "List item") }
            )
            ToolbarButton(
                icon = Icons.Default.FormatListNumbered,
                tag = "btn_numbered_list",
                label = "Numbered",
                onClick = { onInsertText("1. ", "", "List item") }
            )
            ToolbarButton(
                icon = Icons.Default.CheckCircle,
                tag = "btn_task_list",
                label = "Task",
                onClick = { onInsertText("- [ ] ", "", "Task item") }
            )
            ToolbarButton(
                icon = Icons.Default.FormatQuote,
                tag = "btn_quote",
                label = "Quote",
                onClick = { onInsertText("> ", "", "Quote text") }
            )
            ToolbarButton(
                icon = Icons.Default.Link,
                tag = "btn_link",
                label = "Link",
                onClick = { onInsertText("[", "](https://example.com)", "Link Title") }
            )
            ToolbarButton(
                icon = Icons.Default.Image,
                tag = "btn_image",
                label = "Image",
                onClick = { onInsertText("![", "](https://example.com/image.png)", "Alt Text") }
            )
            ToolbarButton(
                icon = Icons.Default.TableChart,
                tag = "btn_table",
                label = "Table",
                onClick = {
                    onInsertText(
                        "| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |\n",
                        "",
                        ""
                    )
                }
            )
            ToolbarButton(
                icon = Icons.Default.HorizontalRule,
                tag = "btn_hr",
                label = "Divider",
                onClick = { onInsertText("\n---\n", "", "") }
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    tag: String,
    label: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .testTag(tag),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        if (label == "H1" || label == "H2" || label == "Fence" || label == "Divider") {
            Text(text = label, fontSize = 11.sp, style = MaterialTheme.typography.labelMedium)
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

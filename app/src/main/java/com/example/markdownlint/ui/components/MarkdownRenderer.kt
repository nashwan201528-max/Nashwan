package com.example.markdownlint.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.markdownlint.ui.theme.CodeBackgroundDark
import com.example.markdownlint.ui.theme.CodeBackgroundLight

@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            // 1. Code fence block
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                val fenceMarker = if (trimmed.startsWith("```")) "```" else "~~~"
                val lang = trimmed.removePrefix(fenceMarker).trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith(fenceMarker)) {
                    codeLines.add(lines[i])
                    i++
                }
                val codeBlockContent = codeLines.joinToString("\n")

                CodeBlockView(
                    language = if (lang.isEmpty()) "text" else lang,
                    code = codeBlockContent,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("code", codeBlockContent))
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
                i++
                continue
            }

            // 2. Table
            if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size && lines[i + 1].trim().contains("---")) {
                val tableRows = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                    tableRows.add(lines[i].trim())
                    i++
                }
                TableView(tableRows = tableRows)
                continue
            }

            // 3. Headings
            if (trimmed.startsWith("#")) {
                val atxMatch = Regex("""^(#{1,6})\s+(.*)$""").find(trimmed)
                if (atxMatch != null) {
                    val level = atxMatch.groupValues[1].length
                    val title = atxMatch.groupValues[2].trimEnd('#').trim()
                    HeadingView(level = level, text = title)
                    i++
                    continue
                }
            }

            // 4. Horizontal Rule
            if (trimmed.matches(Regex("""^(-{3,}|\*{3,}|_{3,})$"""))) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    thickness = 1.dp
                )
                i++
                continue
            }

            // 5. Blockquote
            if (trimmed.startsWith(">")) {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    quoteLines.add(lines[i].trim().removePrefix(">").trim())
                    i++
                }
                BlockquoteView(text = quoteLines.joinToString("\n"))
                continue
            }

            // 6. Task item
            if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ")) {
                val checked = trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ")
                val itemText = trimmed.substring(6).trim()
                TaskItemView(checked = checked, text = itemText)
                i++
                continue
            }

            // 7. Unordered list item
            if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) {
                val itemText = trimmed.substring(2).trim()
                val indentLevel = (line.length - line.trimStart().length) / 2
                ListItemView(text = itemText, indentLevel = indentLevel, isOrdered = false, number = 0)
                i++
                continue
            }

            // 8. Ordered list item
            val olMatch = Regex("""^(\s*)(\d+)\.\s+(.*)$""").find(line)
            if (olMatch != null) {
                val num = olMatch.groupValues[2].toIntOrNull() ?: 1
                val itemText = olMatch.groupValues[3].trim()
                val indentLevel = olMatch.groupValues[1].length / 2
                ListItemView(text = itemText, indentLevel = indentLevel, isOrdered = true, number = num)
                i++
                continue
            }

            // 9. Standard Paragraph
            if (trimmed.isNotEmpty()) {
                ParagraphView(text = trimmed)
            }

            i++
        }
    }
}

@Composable
private fun HeadingView(level: Int, text: String) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        2 -> MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        3 -> MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        4 -> MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        Text(text = parseInlineMarkdown(text), style = style)
        if (level <= 2) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = if (level == 1) 2.dp else 1.dp
            )
        }
    }
}

@Composable
private fun ParagraphView(text: String) {
    Text(
        text = parseInlineMarkdown(text),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun BlockquoteView(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ListItemView(text: String, indentLevel: Int, isOrdered: Boolean, number: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (indentLevel * 16).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = if (isOrdered) "$number. " else "• ",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(if (isOrdered) 24.dp else 14.dp)
        )
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TaskItemView(checked: Boolean, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = if (checked) "Completed" else "Incomplete",
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = parseInlineMarkdown(text),
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CodeBlockView(language: String, code: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CodeBackgroundDark
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.uppercase(),
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun TableView(tableRows: List<String>) {
    if (tableRows.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            tableRows.forEachIndexed { index, row ->
                // Skip separator row (|---|---|)
                if (row.contains("---")) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    return@forEachIndexed
                }

                val cells = row.split("|").filter { it.isNotEmpty() }
                val isHeader = index == 0

                Row(
                    modifier = Modifier
                        .background(
                            if (isHeader) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    cells.forEach { cell ->
                        Text(
                            text = parseInlineMarkdown(cell.trim()),
                            style = if (isHeader) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            else MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length
        while (i < len) {
            // Bold **text** or __text__
            if (i + 1 < len && (text.startsWith("**", i) || text.startsWith("__", i))) {
                val marker = text.substring(i, i + 2)
                val endIdx = text.indexOf(marker, i + 2)
                if (endIdx != -1) {
                    val inner = text.substring(i + 2, endIdx)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(inner)
                    pop()
                    i = endIdx + 2
                    continue
                }
            }
            // Inline code `code`
            if (text[i] == '`') {
                val endIdx = text.indexOf('`', i + 1)
                if (endIdx != -1) {
                    val inner = text.substring(i + 1, endIdx)
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x33888888)))
                    append(" $inner ")
                    pop()
                    i = endIdx + 1
                    continue
                }
            }
            // Italic *text* or _text_
            if (text[i] == '*' || text[i] == '_') {
                val marker = text[i]
                val endIdx = text.indexOf(marker, i + 1)
                if (endIdx != -1) {
                    val inner = text.substring(i + 1, endIdx)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(inner)
                    pop()
                    i = endIdx + 1
                    continue
                }
            }
            // Markdown link [label](url)
            if (text[i] == '[') {
                val labelEnd = text.indexOf(']', i + 1)
                if (labelEnd != -1 && labelEnd + 1 < len && text[labelEnd + 1] == '(') {
                    val urlEnd = text.indexOf(')', labelEnd + 2)
                    if (urlEnd != -1) {
                        val label = text.substring(i + 1, labelEnd)
                        pushStyle(SpanStyle(color = Color(0xFF38BDF8), textDecoration = TextDecoration.Underline))
                        append(label)
                        pop()
                        i = urlEnd + 1
                        continue
                    }
                }
            }

            append(text[i])
            i++
        }
    }
}

package com.example.markdownlint.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.markdownlint.model.IssueSeverity
import com.example.markdownlint.model.LintIssue
import com.example.markdownlint.ui.components.MarkdownToolbar
import com.example.markdownlint.ui.theme.AccentCyan
import com.example.markdownlint.ui.theme.AccentGreen
import com.example.markdownlint.ui.theme.CodeBackgroundDark
import com.example.markdownlint.ui.theme.SeverityError
import com.example.markdownlint.ui.theme.SeverityWarning
import com.example.markdownlint.viewmodel.LintUiState

@Composable
fun EditorScreen(
    state: LintUiState,
    onContentChange: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFixAll: () -> Unit,
    onSaveDoc: () -> Unit,
    onOpenStats: () -> Unit,
    onInsertSnippet: (String, String, String) -> Unit,
    onNavigateToIssues: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fixableIssuesCount = state.issues.count { it.isFixable }
    val errorCount = state.issues.count { it.severity == IssueSeverity.ERROR }
    val warningCount = state.issues.count { it.severity == IssueSeverity.WARNING }
    val lines = state.content.lines()

    // Map of line numbers (1-based) to worst severity on that line
    val lineIssuesMap = remember(state.issues) {
        state.issues.groupBy { it.lineNumber }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Document Top Header / Status Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.currentDocument.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${lines.size} lines · ${state.content.length} chars",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onUndo,
                            enabled = state.canUndo,
                            modifier = Modifier.size(36.dp).testTag("btn_undo")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = if (state.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onRedo,
                            enabled = state.canRedo,
                            modifier = Modifier.size(36.dp).testTag("btn_redo")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Redo,
                                contentDescription = "Redo",
                                tint = if (state.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("markdown", state.content))
                                Toast.makeText(context, "Markdown copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp).testTag("btn_copy_markdown")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Markdown",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onSaveDoc,
                            modifier = Modifier.size(36.dp).testTag("btn_save_doc")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Document",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenStats,
                            modifier = Modifier.size(36.dp).testTag("btn_open_stats")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Stats",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Lint Status Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onNavigateToIssues,
                        color = if (state.issues.isEmpty()) AccentGreen.copy(alpha = 0.15f)
                        else if (errorCount > 0) SeverityError.copy(alpha = 0.15f)
                        else SeverityWarning.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("status_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (state.issues.isEmpty()) Icons.Default.CheckCircle
                                else if (errorCount > 0) Icons.Default.BugReport
                                else Icons.Default.Warning,
                                contentDescription = "Lint Status",
                                tint = if (state.issues.isEmpty()) AccentGreen
                                else if (errorCount > 0) SeverityError
                                else SeverityWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.issues.isEmpty()) "All Markdown rules passed"
                                else "${state.issues.size} issues (${errorCount} errors, ${warningCount} warnings)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.issues.isEmpty()) AccentGreen
                                else if (errorCount > 0) SeverityError
                                else SeverityWarning
                            )
                        }
                    }

                    if (fixableIssuesCount > 0) {
                        ElevatedButton(
                            onClick = onFixAll,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_fix_all_editor"),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Fix All",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Fix All ($fixableIssuesCount)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Code Editor Canvas with Line Numbers Gutter
        val editorScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CodeBackgroundDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(editorScrollState)
            ) {
                // Line numbers gutter
                Column(
                    modifier = Modifier
                        .background(Color(0xFF070C16))
                        .padding(start = 10.dp, end = 6.dp, top = 12.dp, bottom = 24.dp)
                        .width(44.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.forEachIndexed { index, _ ->
                        val lineNum = index + 1
                        val hasIssue = lineIssuesMap.containsKey(lineNum)
                        val worstSeverity = lineIssuesMap[lineNum]?.minByOrNull { it.severity }?.severity

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .height(22.dp)
                                .fillMaxWidth()
                        ) {
                            if (hasIssue) {
                                val dotColor = when (worstSeverity) {
                                    IssueSeverity.ERROR -> SeverityError
                                    IssueSeverity.WARNING -> SeverityWarning
                                    else -> AccentCyan
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "$lineNum",
                                color = if (hasIssue) Color.White else Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (hasIssue) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Markdown text field input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = state.content,
                        onValueChange = onContentChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("markdown_editor_input"),
                        textStyle = TextStyle(
                            color = Color(0xFFF8FAFC),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(AccentCyan),
                        decorationBox = { innerTextField ->
                            if (state.content.isEmpty()) {
                                Text(
                                    text = "Start writing Markdown or select a template...",
                                    color = Color(0xFF475569),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }

        // Markdown Formatting Toolbar at bottom
        MarkdownToolbar(
            onInsertText = onInsertSnippet,
            modifier = Modifier.testTag("markdown_toolbar")
        )
    }
}

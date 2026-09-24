package com.example.markdownlint.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.markdownlint.model.IssueSeverity
import com.example.markdownlint.model.LintIssue
import com.example.markdownlint.ui.theme.AccentCyan
import com.example.markdownlint.ui.theme.AccentGreen
import com.example.markdownlint.ui.theme.SeverityError
import com.example.markdownlint.ui.theme.SeverityInfo
import com.example.markdownlint.ui.theme.SeverityWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileStatsBottomSheet(
    title: String,
    content: String,
    issues: List<LintIssue>,
    onDismiss: () -> Unit
) {
    val lines = content.lines()
    val words = if (content.trim().isEmpty()) 0 else content.trim().split(Regex("""\s+""")).size
    val chars = content.length
    val charactersNoSpaces = content.replace(" ", "").replace("\n", "").length

    val errorsCount = issues.count { it.severity == IssueSeverity.ERROR }
    val warningsCount = issues.count { it.severity == IssueSeverity.WARNING }
    val infosCount = issues.count { it.severity == IssueSeverity.INFO }
    val fixableCount = issues.count { it.isFixable }

    val h1Count = lines.count { it.trim().startsWith("# ") }
    val h2Count = lines.count { it.trim().startsWith("## ") }
    val h3Count = lines.count { it.trim().startsWith("### ") }
    val codeFencesCount = lines.count { it.trim().startsWith("```") } / 2
    val tablesCount = lines.count { it.trim().contains("---") && it.trim().startsWith("|") }
    val linksCount = Regex("""\[[^\]]+\]\([^)]+\)""").findAll(content).count()
    val imagesCount = Regex("""!\[[^\]]*\]\([^)]+\)""").findAll(content).count()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Document Statistics",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Document Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_stats")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lint Summary Cards
            Text(
                text = "LINT HEALTH",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Issues",
                    value = issues.size.toString(),
                    color = if (issues.isEmpty()) AccentGreen else SeverityWarning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Auto-Fixable",
                    value = fixableCount.toString(),
                    color = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Errors",
                    value = errorsCount.toString(),
                    color = if (errorsCount == 0) AccentGreen else SeverityError,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Document Metrics
            Text(
                text = "DOCUMENT METRICS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricRow("Lines", "${lines.size}")
                    MetricRow("Words", "$words")
                    MetricRow("Characters", "$chars")
                    MetricRow("Characters (no spaces)", "$charactersNoSpaces")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Structure Breakdown
            Text(
                text = "STRUCTURE & ELEMENTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricRow("H1 Top Headings", "$h1Count")
                    MetricRow("H2 Subheadings", "$h2Count")
                    MetricRow("H3 Sub-sections", "$h3Count")
                    MetricRow("Code Blocks", "$codeFencesCount")
                    MetricRow("Tables", "$tablesCount")
                    MetricRow("Links", "$linksCount")
                    MetricRow("Images", "$imagesCount")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

package com.example.markdownlint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.markdownlint.model.IssueSeverity
import com.example.markdownlint.model.LintIssue
import com.example.markdownlint.ui.components.LintIssueCard
import com.example.markdownlint.ui.theme.AccentCyan
import com.example.markdownlint.ui.theme.AccentGreen
import com.example.markdownlint.ui.theme.SeverityError
import com.example.markdownlint.ui.theme.SeverityWarning

@Composable
fun IssuesScreen(
    issues: List<LintIssue>,
    onIssueClick: (LintIssue) -> Unit,
    onQuickFix: (LintIssue) -> Unit,
    onFixAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var severityFilter by remember { mutableStateOf<IssueSeverity?>(null) }
    var fixableOnly by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Headings", "Lists", "Whitespace", "Code", "Links", "Tables")

    val filteredIssues = remember(issues, severityFilter, fixableOnly, selectedCategory) {
        issues.filter { issue ->
            val matchSeverity = severityFilter == null || issue.severity == severityFilter
            val matchFixable = !fixableOnly || issue.isFixable
            val matchCategory = selectedCategory == "All" || when (selectedCategory) {
                "Headings" -> issue.ruleId in listOf("MD001", "MD003", "MD018", "MD019", "MD020", "MD021", "MD022", "MD023", "MD024", "MD025", "MD026", "MD036", "MD041")
                "Lists" -> issue.ruleId in listOf("MD004", "MD005", "MD007", "MD029", "MD030", "MD032")
                "Whitespace" -> issue.ruleId in listOf("MD009", "MD010", "MD012", "MD013", "MD027", "MD028", "MD035", "MD047")
                "Code" -> issue.ruleId in listOf("MD014", "MD031", "MD038", "MD040", "MD046", "MD048")
                "Links" -> issue.ruleId in listOf("MD011", "MD034", "MD039", "MD042", "MD045", "MD051", "MD060")
                "Tables" -> issue.ruleId in listOf("MD055", "MD056", "MD058")
                else -> true
            }
            matchSeverity && matchFixable && matchCategory
        }
    }

    val fixableCount = issues.count { it.isFixable }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        // Hero Fix All Action Bar
        if (issues.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${issues.size} Linter Issues",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$fixableCount can be resolved automatically",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (fixableCount > 0) {
                        ElevatedButton(
                            onClick = onFixAll,
                            modifier = Modifier.testTag("btn_fix_all_issues_screen"),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Fix All",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Fix All ($fixableCount)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Severity & Fixable Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = severityFilter == null && !fixableOnly,
                    onClick = {
                        severityFilter = null
                        fixableOnly = false
                    },
                    label = { Text("All (${issues.size})") },
                    colors = FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = severityFilter == IssueSeverity.ERROR,
                    onClick = {
                        severityFilter = if (severityFilter == IssueSeverity.ERROR) null else IssueSeverity.ERROR
                    },
                    label = { Text("Errors (${issues.count { it.severity == IssueSeverity.ERROR }})") }
                )
                FilterChip(
                    selected = severityFilter == IssueSeverity.WARNING,
                    onClick = {
                        severityFilter = if (severityFilter == IssueSeverity.WARNING) null else IssueSeverity.WARNING
                    },
                    label = { Text("Warnings (${issues.count { it.severity == IssueSeverity.WARNING }})") }
                )
                FilterChip(
                    selected = fixableOnly,
                    onClick = { fixableOnly = !fixableOnly },
                    label = { Text("Fixable ($fixableCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Fixable",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Issues List or Clean State
        if (issues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        color = AccentGreen.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Perfect Markdown",
                                tint = AccentGreen,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    Text(
                        text = "Clean Markdown!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "No lint violations detected against your active rule preset.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (filteredIssues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No issues match the selected filters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredIssues, key = { it.id }) { issue ->
                    LintIssueCard(
                        issue = issue,
                        onIssueClick = onIssueClick,
                        onQuickFix = onQuickFix
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

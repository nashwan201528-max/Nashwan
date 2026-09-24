package com.example.markdownlint.engine

import com.example.markdownlint.model.FixInfo
import com.example.markdownlint.model.LintIssue

object MarkdownFixer {

    fun fixSingleIssue(content: String, issue: LintIssue): String {
        val fixInfo = issue.fixInfo ?: return content
        val lines = content.lines().toMutableList()
        val lineIdx = fixInfo.lineNumber - 1

        if (lineIdx in lines.indices) {
            if (issue.ruleId == "MD012") {
                // Remove consecutive blank line
                lines.removeAt(lineIdx)
                return lines.joinToString("\n")
            } else {
                lines[lineIdx] = fixInfo.replacementText
                return lines.joinToString("\n")
            }
        }
        return content
    }

    fun fixAllIssues(content: String, issues: List<LintIssue>): Pair<String, Int> {
        val fixable = issues.filter { it.isFixable }
        if (fixable.isEmpty()) return Pair(content, 0)

        var fixedCount = 0
        val lines = content.lines().toMutableList()

        // 1. First apply line edits in descending order of line number so indices don't shift prematurely
        val lineFixes = fixable
            .filter { it.ruleId != "MD012" && it.fixInfo != null }
            .sortedByDescending { it.lineNumber }

        for (issue in lineFixes) {
            val fix = issue.fixInfo ?: continue
            val idx = fix.lineNumber - 1
            if (idx in lines.indices) {
                lines[idx] = fix.replacementText
                fixedCount++
            }
        }

        // 2. Remove consecutive blank lines (MD012)
        val cleanedLines = mutableListOf<String>()
        var consecutiveBlanks = 0
        for (line in lines) {
            if (line.trim().isEmpty()) {
                consecutiveBlanks++
                if (consecutiveBlanks <= 1) {
                    cleanedLines.add(line)
                } else {
                    fixedCount++
                }
            } else {
                consecutiveBlanks = 0
                cleanedLines.add(line)
            }
        }

        var result = cleanedLines.joinToString("\n")
        if (!result.endsWith("\n")) {
            result += "\n"
        }

        return Pair(result, fixedCount)
    }
}

package com.example.markdownlint.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class IssueSeverity {
    ERROR,
    WARNING,
    INFO
}

data class FixInfo(
    val lineNumber: Int, // 1-based
    val originalText: String,
    val replacementText: String,
    val description: String
)

data class LintIssue(
    val id: String = java.util.UUID.randomUUID().toString(),
    val ruleId: String,
    val ruleName: String,
    val description: String,
    val lineNumber: Int, // 1-based
    val column: Int = 1,
    val severity: IssueSeverity = IssueSeverity.WARNING,
    val detail: String? = null,
    val fixInfo: FixInfo? = null
) {
    val isFixable: Boolean
        get() = fixInfo != null
}

data class RuleConfigParam(
    val key: String,
    val label: String,
    val type: ParamType,
    val defaultValue: Any,
    val options: List<String> = emptyList()
)

enum class ParamType {
    BOOLEAN,
    INTEGER,
    STRING,
    ENUM
}

data class LintRule(
    val id: String, // e.g. MD001
    val name: String, // e.g. heading-increment
    val description: String,
    val category: String, // "headings", "lists", "whitespace", "code", "html", "links", "emphasis", "tables"
    val isFixable: Boolean = false,
    val defaultEnabled: Boolean = true,
    val sampleGood: String = "",
    val sampleBad: String = "",
    val explanation: String = "",
    val params: List<RuleConfigParam> = emptyList()
)

@Serializable
data class LintConfiguration(
    val enabledRules: Map<String, Boolean> = emptyMap(),
    val lineLength: Int = 80,
    val headingStyle: String = "consistent", // consistent, atx, setext
    val ulStyle: String = "consistent", // consistent, asterisk, dash, plus
    val codeBlockStyle: String = "consistent", // consistent, fenced, indented
    val properNames: List<String> = listOf("JavaScript", "GitHub", "Android", "Kotlin", "Markdown", "Node.js")
) {
    fun isRuleEnabled(ruleId: String, defaultVal: Boolean = true): Boolean {
        return enabledRules[ruleId] ?: defaultVal
    }
}

@Entity(tableName = "documents")
@Serializable
data class MarkdownDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
)

@Entity(tableName = "presets")
@Serializable
data class ConfigPreset(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val isBuiltIn: Boolean = false,
    val configJson: String
)

package com.example.markdownlint.engine

import com.example.markdownlint.model.ConfigPreset
import com.example.markdownlint.model.LintConfiguration
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PresetCatalog {

    val defaultConfiguration = LintConfiguration(
        enabledRules = mapOf(
            "MD001" to true,
            "MD003" to true,
            "MD004" to true,
            "MD005" to true,
            "MD007" to true,
            "MD009" to true,
            "MD010" to true,
            "MD011" to true,
            "MD012" to true,
            "MD013" to false,
            "MD014" to true,
            "MD018" to true,
            "MD019" to true,
            "MD020" to true,
            "MD021" to true,
            "MD022" to true,
            "MD023" to true,
            "MD024" to true,
            "MD025" to true,
            "MD026" to true,
            "MD027" to true,
            "MD028" to true,
            "MD029" to true,
            "MD030" to true,
            "MD031" to true,
            "MD032" to true,
            "MD033" to false,
            "MD034" to true,
            "MD035" to true,
            "MD036" to true,
            "MD037" to true,
            "MD038" to true,
            "MD039" to true,
            "MD040" to true,
            "MD041" to true,
            "MD042" to true,
            "MD044" to true,
            "MD045" to true,
            "MD046" to true,
            "MD047" to true,
            "MD048" to true,
            "MD051" to true,
            "MD055" to true,
            "MD056" to true,
            "MD058" to true,
            "MD060" to true
        )
    )

    private val json = Json { prettyPrint = true }

    val builtInPresets: List<ConfigPreset> = listOf(
        ConfigPreset(
            id = "default",
            name = "Default (Recommended)",
            description = "Standard MarkdownLint rules balance readability and consistency.",
            isBuiltIn = true,
            configJson = json.encodeToString(defaultConfiguration)
        ),
        ConfigPreset(
            id = "strict",
            name = "Strict (All Rules)",
            description = "Enforces every single rule including strict 80-char line length and no inline HTML.",
            isBuiltIn = true,
            configJson = json.encodeToString(
                defaultConfiguration.copy(
                    enabledRules = RuleCatalog.allRules.associate { it.id to true },
                    lineLength = 80
                )
            )
        ),
        ConfigPreset(
            id = "prettier",
            name = "Prettier-Compatible",
            description = "Disables whitespace and styling rules that clash with the Prettier code formatter.",
            isBuiltIn = true,
            configJson = json.encodeToString(
                defaultConfiguration.copy(
                    enabledRules = defaultConfiguration.enabledRules.toMutableMap().apply {
                        put("MD004", false)
                        put("MD007", false)
                        put("MD009", false)
                        put("MD010", false)
                        put("MD012", false)
                        put("MD013", false)
                        put("MD030", false)
                        put("MD035", false)
                    }
                )
            )
        ),
        ConfigPreset(
            id = "relaxed",
            name = "Relaxed / Lenient",
            description = "Only flags critical structural mistakes (reversed links, broken headings, empty links).",
            isBuiltIn = true,
            configJson = json.encodeToString(
                LintConfiguration(
                    enabledRules = mapOf(
                        "MD001" to true,
                        "MD011" to true,
                        "MD018" to true,
                        "MD023" to true,
                        "MD041" to false,
                        "MD042" to true,
                        "MD045" to true
                    )
                )
            )
        ),
        ConfigPreset(
            id = "github_readme",
            name = "GitHub README",
            description = "Optimized for GitHub repository READMEs with shields, code blocks, and anchor links.",
            isBuiltIn = true,
            configJson = json.encodeToString(
                defaultConfiguration.copy(
                    enabledRules = defaultConfiguration.enabledRules.toMutableMap().apply {
                        put("MD033", false) // allow HTML badges and center tags
                        put("MD013", false) // allow long table/badge lines
                        put("MD041", true)
                        put("MD044", true)
                    }
                )
            )
        )
    )
}

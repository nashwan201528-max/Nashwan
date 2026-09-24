package com.example.markdownlint.engine

import com.example.markdownlint.model.FixInfo
import com.example.markdownlint.model.IssueSeverity
import com.example.markdownlint.model.LintConfiguration
import com.example.markdownlint.model.LintIssue

class MarkdownLintEngine(
    private val config: LintConfiguration = LintConfiguration()
) {

    fun lint(content: String): List<LintIssue> {
        if (content.isEmpty()) return emptyList()

        val issues = mutableListOf<LintIssue>()
        val lines = content.lines()
        val lineCount = lines.size

        // Parse line context (code blocks, comments, front matter)
        val lineTypes = analyzeLineTypes(lines)

        var lastHeadingLevel = 0
        var documentFirstHeadingFound = false
        val headingTitles = mutableListOf<String>()
        val definedAnchors = mutableSetOf<String>()

        // Pre-scan headings for anchor links check (MD051)
        for (i in lines.indices) {
            val line = lines[i]
            if (lineTypes[i] == LineType.NORMAL || lineTypes[i] == LineType.HEADING) {
                val atxMatch = Regex("""^(#{1,6})\s+(.*?)(?:\s+#+)?$""").find(line)
                if (atxMatch != null) {
                    val title = atxMatch.groupValues[2].trim()
                    val slug = slugify(title)
                    definedAnchors.add(slug)
                }
            }
        }

        // Rule: MD041 First line in file should be a top-level heading
        if (config.isRuleEnabled("MD041", defaultVal = true)) {
            var firstMeaningfulLineIndex = -1
            for (i in lines.indices) {
                val trimmed = lines[i].trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("<!--") && lineTypes[i] != LineType.FRONT_MATTER) {
                    firstMeaningfulLineIndex = i
                    break
                }
            }
            if (firstMeaningfulLineIndex >= 0) {
                val firstLine = lines[firstMeaningfulLineIndex].trim()
                val isH1 = firstLine.startsWith("# ") ||
                        (firstMeaningfulLineIndex + 1 < lineCount && lines[firstMeaningfulLineIndex + 1].trim().matches(Regex("""^={2,}$""")))
                if (!isH1) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD041",
                            ruleName = "first-line-heading",
                            description = "First line in a file should be a top-level heading",
                            lineNumber = firstMeaningfulLineIndex + 1,
                            severity = IssueSeverity.WARNING,
                            detail = "Expected '# Heading 1' at the start of document"
                        )
                    )
                }
            }
        }

        // Line-by-line checks
        var consecutiveBlanks = 0
        var firstFenceMarker: String? = null
        var firstHrStyle: String? = null
        var firstBulletStyle: Char? = null

        for (i in lines.indices) {
            val line = lines[i]
            val lineNum = i + 1
            val type = lineTypes[i]
            val isBlank = line.trim().isEmpty()

            // MD012: Multiple consecutive blank lines
            if (config.isRuleEnabled("MD012")) {
                if (isBlank && type != LineType.CODE_FENCE_CONTENT) {
                    consecutiveBlanks++
                    if (consecutiveBlanks > 1) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD012",
                                ruleName = "no-multiple-blanks",
                                description = "Multiple consecutive blank lines",
                                lineNumber = lineNum,
                                severity = IssueSeverity.WARNING,
                                detail = "Expected 1 blank line, found $consecutiveBlanks",
                                fixInfo = FixInfo(
                                    lineNumber = lineNum,
                                    originalText = line,
                                    replacementText = "", // delete line
                                    description = "Remove extra blank line"
                                )
                            )
                        )
                    }
                } else {
                    consecutiveBlanks = 0
                }
            }

            // Skip code block content for general formatting rules
            if (type == LineType.CODE_FENCE_CONTENT) {
                // MD010 inside code blocks if enabled
                if (config.isRuleEnabled("MD010") && line.contains("\t")) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD010",
                            ruleName = "no-hard-tabs",
                            description = "Hard tab character in code block",
                            lineNumber = lineNum,
                            column = line.indexOf("\t") + 1,
                            severity = IssueSeverity.INFO,
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = line.replace("\t", "  "),
                                description = "Convert tabs to spaces"
                            )
                        )
                    )
                }
                continue
            }

            // MD009: Trailing whitespace
            if (config.isRuleEnabled("MD009")) {
                if (line.endsWith(" ") || line.endsWith("\t")) {
                    val trimmedEnd = line.trimEnd()
                    issues.add(
                        LintIssue(
                            ruleId = "MD009",
                            ruleName = "no-trailing-spaces",
                            description = "Trailing spaces at end of line",
                            lineNumber = lineNum,
                            column = trimmedEnd.length + 1,
                            severity = IssueSeverity.INFO,
                            detail = "Trailing spaces found (${line.length - trimmedEnd.length} spaces)",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = trimmedEnd,
                                description = "Remove trailing whitespace"
                            )
                        )
                    )
                }
            }

            // MD010: Hard tabs
            if (config.isRuleEnabled("MD010")) {
                if (line.contains("\t")) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD010",
                            ruleName = "no-hard-tabs",
                            description = "Hard tab character should be replaced with spaces",
                            lineNumber = lineNum,
                            column = line.indexOf("\t") + 1,
                            severity = IssueSeverity.WARNING,
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = line.replace("\t", "  "),
                                description = "Replace tabs with 2 spaces"
                            )
                        )
                    )
                }
            }

            // MD013: Line length check (disabled by default)
            if (config.isRuleEnabled("MD013", defaultVal = false)) {
                if (line.length > config.lineLength && !line.startsWith("http") && !line.contains("](") && type != LineType.TABLE_ROW) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD013",
                            ruleName = "line-length",
                            description = "Line length exceeds maximum (${line.length} > ${config.lineLength})",
                            lineNumber = lineNum,
                            column = config.lineLength + 1,
                            severity = IssueSeverity.INFO
                        )
                    )
                }
            }

            // MD018 & MD019: Missing space or multiple spaces after hash in ATX heading
            val rawAtxMatch = Regex("""^(#{1,6})(\s*)(.*)$""").find(line.trimStart())
            val hasLeadingSpace = line.startsWith(" ") && rawAtxMatch != null && line.trimStart().startsWith("#")

            // MD023: Heading must start at the beginning of the line
            if (config.isRuleEnabled("MD023") && hasLeadingSpace && rawAtxMatch != null) {
                issues.add(
                    LintIssue(
                        ruleId = "MD023",
                        ruleName = "heading-start-left",
                        description = "Heading must start at the beginning of the line",
                        lineNumber = lineNum,
                        column = 1,
                        severity = IssueSeverity.WARNING,
                        fixInfo = FixInfo(
                            lineNumber = lineNum,
                            originalText = line,
                            replacementText = line.trimStart(),
                            description = "Align heading to left margin"
                        )
                    )
                )
            }

            if (rawAtxMatch != null && line.trimStart().startsWith("#")) {
                val hashes = rawAtxMatch.groupValues[1]
                val spaces = rawAtxMatch.groupValues[2]
                val headingText = rawAtxMatch.groupValues[3]
                val level = hashes.length

                if (spaces.isEmpty() && headingText.isNotEmpty()) {
                    // MD018: No space after hash
                    if (config.isRuleEnabled("MD018")) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD018",
                                ruleName = "no-missing-space-atx",
                                description = "No space after hash on atx style heading",
                                lineNumber = lineNum,
                                column = hashes.length + 1,
                                severity = IssueSeverity.ERROR,
                                detail = "Expected '$hashes $headingText'",
                                fixInfo = FixInfo(
                                    lineNumber = lineNum,
                                    originalText = line,
                                    replacementText = "$hashes $headingText",
                                    description = "Add space after heading hash"
                                )
                            )
                        )
                    }
                } else if (spaces.length > 1) {
                    // MD019: Multiple spaces after hash
                    if (config.isRuleEnabled("MD019")) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD019",
                                ruleName = "no-multiple-space-atx",
                                description = "Multiple spaces after hash on atx style heading",
                                lineNumber = lineNum,
                                column = hashes.length + 1,
                                severity = IssueSeverity.WARNING,
                                detail = "Found ${spaces.length} spaces, expected 1",
                                fixInfo = FixInfo(
                                    lineNumber = lineNum,
                                    originalText = line,
                                    replacementText = "$hashes $headingText",
                                    description = "Use single space after hash"
                                )
                            )
                        )
                    }
                }

                // Check closed ATX heading (MD020, MD021)
                if (headingText.endsWith("#")) {
                    val cleanText = headingText.trimEnd('#')
                    val trailingHashes = headingText.substring(cleanText.length)
                    if (cleanText.endsWith("  ")) {
                        if (config.isRuleEnabled("MD021")) {
                            issues.add(
                                LintIssue(
                                    ruleId = "MD021",
                                    ruleName = "no-multiple-space-closed-atx",
                                    description = "Multiple spaces inside hashes on closed atx heading",
                                    lineNumber = lineNum,
                                    severity = IssueSeverity.WARNING,
                                    fixInfo = FixInfo(
                                        lineNumber = lineNum,
                                        originalText = line,
                                        replacementText = "$hashes ${cleanText.trim()} $trailingHashes",
                                        description = "Single space before closing hash"
                                    )
                                )
                            )
                        }
                    } else if (!cleanText.endsWith(" ")) {
                        if (config.isRuleEnabled("MD020")) {
                            issues.add(
                                LintIssue(
                                    ruleId = "MD020",
                                    ruleName = "no-missing-space-closed-atx",
                                    description = "No space inside hashes on closed atx heading",
                                    lineNumber = lineNum,
                                    severity = IssueSeverity.WARNING,
                                    fixInfo = FixInfo(
                                        lineNumber = lineNum,
                                        originalText = line,
                                        replacementText = "$hashes ${cleanText.trim()} $trailingHashes",
                                        description = "Add space before closing hash"
                                    )
                                )
                            )
                        }
                    }
                }

                // MD001: Heading increment check
                if (config.isRuleEnabled("MD001")) {
                    if (lastHeadingLevel > 0 && level > lastHeadingLevel + 1) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD001",
                                ruleName = "heading-increment",
                                description = "Heading levels should only increment by one level at a time",
                                lineNumber = lineNum,
                                severity = IssueSeverity.WARNING,
                                detail = "Jumped from H$lastHeadingLevel to H$level (expected H${lastHeadingLevel + 1})",
                                fixInfo = FixInfo(
                                    lineNumber = lineNum,
                                    originalText = line,
                                    replacementText = "${"#".repeat(lastHeadingLevel + 1)} ${headingText.trimEnd('#').trim()}",
                                    description = "Change to H${lastHeadingLevel + 1}"
                                )
                            )
                        )
                    }
                    lastHeadingLevel = level
                }

                // MD025: Single top-level heading (H1)
                if (config.isRuleEnabled("MD025") && level == 1) {
                    if (documentFirstHeadingFound) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD025",
                                ruleName = "single-title",
                                description = "Multiple top-level headings in the same document",
                                lineNumber = lineNum,
                                severity = IssueSeverity.WARNING,
                                detail = "Only one H1 heading recommended per document"
                            )
                        )
                    }
                    documentFirstHeadingFound = true
                }

                // MD024: Duplicate heading content
                if (config.isRuleEnabled("MD024")) {
                    val cleanTitle = headingText.trim().trimEnd('#').trim()
                    if (cleanTitle.isNotEmpty()) {
                        if (headingTitles.contains(cleanTitle.lowercase())) {
                            issues.add(
                                LintIssue(
                                    ruleId = "MD024",
                                    ruleName = "no-duplicate-heading",
                                    description = "Multiple headings should not have identical content",
                                    lineNumber = lineNum,
                                    severity = IssueSeverity.WARNING,
                                    detail = "Duplicate heading title: '$cleanTitle'"
                                )
                            )
                        }
                        headingTitles.add(cleanTitle.lowercase())
                    }
                }

                // MD026: Trailing punctuation in heading
                if (config.isRuleEnabled("MD026")) {
                    val cleanTitle = headingText.trim().trimEnd('#').trim()
                    if (cleanTitle.endsWith(".") || cleanTitle.endsWith(":") || cleanTitle.endsWith(";")) {
                        val withoutPunctuation = cleanTitle.substring(0, cleanTitle.length - 1)
                        issues.add(
                            LintIssue(
                                ruleId = "MD026",
                                ruleName = "no-trailing-punctuation",
                                description = "Trailing punctuation in heading",
                                lineNumber = lineNum,
                                severity = IssueSeverity.INFO,
                                detail = "Heading ends with '${cleanTitle.last()}'",
                                fixInfo = FixInfo(
                                    lineNumber = lineNum,
                                    originalText = line,
                                    replacementText = "$hashes $withoutPunctuation",
                                    description = "Remove trailing punctuation"
                                )
                            )
                        )
                    }
                }

                // MD022: Headings surrounded by blank lines
                if (config.isRuleEnabled("MD022")) {
                    val prevLineBlank = i == 0 || lines[i - 1].trim().isEmpty() || lineTypes[i - 1] == LineType.FRONT_MATTER
                    val nextLineBlank = i == lineCount - 1 || lines[i + 1].trim().isEmpty()
                    if (!prevLineBlank || !nextLineBlank) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD022",
                                ruleName = "blanks-around-headings",
                                description = "Headings should be surrounded by blank lines",
                                lineNumber = lineNum,
                                severity = IssueSeverity.WARNING,
                                detail = "Expected blank line before and after heading"
                            )
                        )
                    }
                }
            }

            // MD004 & MD030 & MD005: Unordered list checks
            val ulMatch = Regex("""^(\s*)([*+-])(\s+)(.*)$""").find(line)
            if (ulMatch != null && type != LineType.CODE_FENCE_CONTENT) {
                val indent = ulMatch.groupValues[1]
                val marker = ulMatch.groupValues[2][0]
                val spaces = ulMatch.groupValues[3]
                val itemText = ulMatch.groupValues[4]

                if (firstBulletStyle == null) {
                    firstBulletStyle = marker
                } else if (config.isRuleEnabled("MD004") && marker != firstBulletStyle) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD004",
                            ruleName = "ul-style",
                            description = "Unordered list bullet style should be consistent",
                            lineNumber = lineNum,
                            severity = IssueSeverity.WARNING,
                            detail = "Found '$marker', expected '$firstBulletStyle'",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = "$indent$firstBulletStyle$spaces$itemText",
                                description = "Change bullet marker to '$firstBulletStyle'"
                            )
                        )
                    )
                }

                // MD030: Spaces after list markers
                if (config.isRuleEnabled("MD030") && spaces.length > 1) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD030",
                            ruleName = "list-marker-space",
                            description = "Spaces after list markers should be 1 space",
                            lineNumber = lineNum,
                            severity = IssueSeverity.INFO,
                            detail = "Found ${spaces.length} spaces after bullet",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = "$indent$marker $itemText",
                                description = "Use single space after list marker"
                            )
                        )
                    )
                }

                // MD007: Unordered list indentation
                if (config.isRuleEnabled("MD007") && indent.isNotEmpty() && indent.length % 2 != 0) {
                    val fixedIndent = " ".repeat(Math.max(2, (indent.length / 2) * 2))
                    issues.add(
                        LintIssue(
                            ruleId = "MD007",
                            ruleName = "ul-indent",
                            description = "Unordered list indentation should use standard 2 or 4 spaces",
                            lineNumber = lineNum,
                            severity = IssueSeverity.WARNING,
                            detail = "Odd indentation: ${indent.length} spaces",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = "$fixedIndent$marker$spaces$itemText",
                                description = "Fix list indentation to standard spacing"
                            )
                        )
                    )
                }
            }

            // MD027: Blockquote spacing
            val bqMatch = Regex("""^(\s*>+)(\s*)(.*)$""").find(line)
            if (bqMatch != null && type != LineType.CODE_FENCE_CONTENT) {
                val marker = bqMatch.groupValues[1]
                val spaces = bqMatch.groupValues[2]
                val bqContent = bqMatch.groupValues[3]

                if (config.isRuleEnabled("MD027") && spaces.length > 1) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD027",
                            ruleName = "no-multiple-space-blockquote",
                            description = "Multiple spaces after blockquote symbol",
                            lineNumber = lineNum,
                            severity = IssueSeverity.WARNING,
                            detail = "Found ${spaces.length} spaces after '>'",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = "$marker $bqContent",
                                description = "Use single space after blockquote marker"
                            )
                        )
                    )
                }
            }

            // MD011: Reversed links `(text)[url]`
            if (config.isRuleEnabled("MD011") && type != LineType.CODE_FENCE_CONTENT) {
                val reversedLinkMatch = Regex("""\(([^)]+)\)\[(https?://[^\]]+|/[^\]]+)\]""").find(line)
                if (reversedLinkMatch != null) {
                    val text = reversedLinkMatch.groupValues[1]
                    val url = reversedLinkMatch.groupValues[2]
                    val fullMatch = reversedLinkMatch.value
                    issues.add(
                        LintIssue(
                            ruleId = "MD011",
                            ruleName = "no-reversed-links",
                            description = "Reversed link syntax detected: `($text)[$url]`",
                            lineNumber = lineNum,
                            severity = IssueSeverity.ERROR,
                            detail = "Expected `[$text]($url)`",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = line.replace(fullMatch, "[$text]($url)"),
                                description = "Fix reversed link syntax"
                            )
                        )
                    )
                }
            }

            // MD034: Bare URLs
            if (config.isRuleEnabled("MD034") && type != LineType.CODE_FENCE_CONTENT) {
                val bareUrlMatch = Regex("""(?<!\[|\(|<)(https?://[^\s<>)\]]+)(?!\]|\)|>)""").find(line)
                if (bareUrlMatch != null && !line.contains("](") && !line.contains("<http")) {
                    val url = bareUrlMatch.groupValues[1]
                    issues.add(
                        LintIssue(
                            ruleId = "MD034",
                            ruleName = "no-bare-urls",
                            description = "Bare URL used without angle brackets or markdown link",
                            lineNumber = lineNum,
                            severity = IssueSeverity.INFO,
                            detail = "Bare URL: $url",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = line.replace(url, "<$url>"),
                                description = "Enclose URL in angle brackets"
                            )
                        )
                    )
                }
            }

            // MD037: Spaces inside emphasis markers `* bold *`
            if (config.isRuleEnabled("MD037") && type != LineType.CODE_FENCE_CONTENT) {
                val badEmphasisMatch = Regex("""(\*\*|__|\*|_)\s+([^\s*_][^*_]*?)\s+(\1)""").find(line)
                if (badEmphasisMatch != null) {
                    val marker = badEmphasisMatch.groupValues[1]
                    val inner = badEmphasisMatch.groupValues[2]
                    val full = badEmphasisMatch.value
                    issues.add(
                        LintIssue(
                            ruleId = "MD037",
                            ruleName = "no-space-in-emphasis",
                            description = "Spaces inside emphasis markers",
                            lineNumber = lineNum,
                            severity = IssueSeverity.WARNING,
                            detail = "Found spaces inside '$marker'",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = line.replace(full, "$marker$inner$marker"),
                                description = "Remove spaces inside emphasis"
                            )
                        )
                    )
                }
            }

            // MD038: Spaces inside code spans `` ` code ` ``
            if (config.isRuleEnabled("MD038") && type != LineType.CODE_FENCE_CONTENT) {
                val badCodeSpanMatch = Regex("""(?<!`)`\s+([^`]+?)\s+`(?!`)""").find(line)
                if (badCodeSpanMatch != null) {
                    val inner = badCodeSpanMatch.groupValues[1]
                    val full = badCodeSpanMatch.value
                    issues.add(
                        LintIssue(
                            ruleId = "MD038",
                            ruleName = "no-space-in-code",
                            description = "Spaces inside code span elements",
                            lineNumber = lineNum,
                            severity = IssueSeverity.INFO,
                            detail = "Found spaces inside backticks: `$full`",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = line.replace(full, "`$inner`"),
                                description = "Remove spaces inside code span"
                            )
                        )
                    )
                }
            }

            // MD039: Spaces inside link text `[ text ](url)`
            if (config.isRuleEnabled("MD039") && type != LineType.CODE_FENCE_CONTENT) {
                val badLinkSpacingMatch = Regex("""\[\s+([^\]]+?)\s+\]\(([^)]+)\)""").find(line)
                if (badLinkSpacingMatch != null) {
                    val innerText = badLinkSpacingMatch.groupValues[1]
                    val url = badLinkSpacingMatch.groupValues[2]
                    val full = badLinkSpacingMatch.value
                    issues.add(
                        LintIssue(
                            ruleId = "MD039",
                            ruleName = "no-space-in-links",
                            description = "Spaces inside link text brackets",
                            lineNumber = lineNum,
                            severity = IssueSeverity.INFO,
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = line.replace(full, "[$innerText]($url)"),
                                description = "Remove spaces inside link brackets"
                            )
                        )
                    )
                }
            }

            // MD040 & MD048: Code fence checks
            val fenceMatch = Regex("""^(\s*)(```|~~~)(.*)$""").find(line)
            if (fenceMatch != null) {
                val marker = fenceMatch.groupValues[2]
                val lang = fenceMatch.groupValues[3].trim()

                if (firstFenceMarker == null) {
                    firstFenceMarker = marker
                } else if (config.isRuleEnabled("MD048") && marker != firstFenceMarker) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD048",
                            ruleName = "code-fence-style",
                            description = "Code fence style should be consistent",
                            lineNumber = lineNum,
                            severity = IssueSeverity.WARNING,
                            detail = "Found '$marker', expected '$firstFenceMarker'"
                        )
                    )
                }

                // If opening fence and no language
                if (config.isRuleEnabled("MD040") && lang.isEmpty() && type == LineType.CODE_FENCE_START) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD040",
                            ruleName = "fenced-code-language",
                            description = "Fenced code blocks should have a language specified",
                            lineNumber = lineNum,
                            severity = IssueSeverity.INFO,
                            detail = "Missing language identifier (e.g. ```kotlin, ```json, ```bash)"
                        )
                    )
                }

                // MD014: Commands show output ($ prefix)
                if (config.isRuleEnabled("MD014") && lang in listOf("bash", "sh", "zsh", "shell", "terminal", "console")) {
                    // check lines inside this fence
                    var j = i + 1
                    var allCommandsHaveDollar = true
                    var count = 0
                    while (j < lines.size && !lines[j].trim().startsWith("```") && !lines[j].trim().startsWith("~~~")) {
                        val innerLine = lines[j].trim()
                        if (innerLine.isNotEmpty()) {
                            count++
                            if (!innerLine.startsWith("$")) {
                                allCommandsHaveDollar = false
                            }
                        }
                        j++
                    }
                    if (count > 0 && allCommandsHaveDollar) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD014",
                                ruleName = "commands-show-output",
                                description = "Dollar signs before commands without showing output",
                                lineNumber = lineNum,
                                severity = IssueSeverity.INFO,
                                detail = "Prefixing bash commands with '$' makes copy-paste annoying"
                            )
                        )
                    }
                }
            }

            // MD035: Horizontal rule style
            val hrMatch = Regex("""^(\s*)(-{3,}|\*{3,}|_{3,})\s*$""").find(line)
            if (hrMatch != null && type != LineType.CODE_FENCE_CONTENT && type != LineType.FRONT_MATTER) {
                val hrSymbol = hrMatch.groupValues[2].trim()
                if (firstHrStyle == null) {
                    firstHrStyle = hrSymbol
                } else if (config.isRuleEnabled("MD035") && hrSymbol != firstHrStyle) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD035",
                            ruleName = "hr-style",
                            description = "Horizontal rule style should be consistent",
                            lineNumber = lineNum,
                            severity = IssueSeverity.WARNING,
                            detail = "Found '$hrSymbol', expected '$firstHrStyle'",
                            fixInfo = FixInfo(
                                lineNumber = lineNum,
                                originalText = line,
                                replacementText = firstHrStyle,
                                description = "Standardize HR to '$firstHrStyle'"
                            )
                        )
                    )
                }
            }

            // MD044: Proper names casing
            if (config.isRuleEnabled("MD044") && type != LineType.CODE_FENCE_CONTENT) {
                for (name in config.properNames) {
                    val regex = Regex("""(?i)\b${Regex.escape(name)}\b""")
                    val match = regex.find(line)
                    if (match != null && match.value != name) {
                        val wrongName = match.value
                        issues.add(
                            LintIssue(
                                ruleId = "MD044",
                                ruleName = "proper-names",
                                description = "Proper names should have correct casing",
                                lineNumber = lineNum,
                                severity = IssueSeverity.WARNING,
                                detail = "Found '$wrongName', expected '$name'",
                                fixInfo = FixInfo(
                                    lineNumber = lineNum,
                                    originalText = line,
                                    replacementText = line.replaceFirst(wrongName, name),
                                    description = "Correct casing to '$name'"
                                )
                            )
                        )
                    }
                }
            }

            // MD045: Images should have alt text
            if (config.isRuleEnabled("MD045") && type != LineType.CODE_FENCE_CONTENT) {
                if (Regex("""!\[\s*\]\([^)]+\)""").containsMatchIn(line)) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD045",
                            ruleName = "no-alt-text",
                            description = "Images should have alternate text",
                            lineNumber = lineNum,
                            severity = IssueSeverity.WARNING,
                            detail = "Alt text improves accessibility for screen reader users"
                        )
                    )
                }
            }

            // MD042: Empty links
            if (config.isRuleEnabled("MD042") && type != LineType.CODE_FENCE_CONTENT) {
                if (Regex("""(?<!!)\[\s*\]\([^)]+\)""").containsMatchIn(line) || Regex("""(?<!!)\[[^\]]+\]\(\s*\)""").containsMatchIn(line)) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD042",
                            ruleName = "no-empty-links",
                            description = "No empty links (empty link text or URL)",
                            lineNumber = lineNum,
                            severity = IssueSeverity.ERROR,
                            detail = "Link requires both non-empty text and non-empty URL destination"
                        )
                    )
                }
            }

            // MD060: Descriptive link text
            if (config.isRuleEnabled("MD060") && type != LineType.CODE_FENCE_CONTENT) {
                val nonDescriptiveRegex = Regex("""(?i)\[(click here|here|link|more|read more|this link|this)\]\([^)]+\)""")
                val ndMatch = nonDescriptiveRegex.find(line)
                if (ndMatch != null) {
                    issues.add(
                        LintIssue(
                            ruleId = "MD060",
                            ruleName = "descriptive-link-text",
                            description = "Link text should be descriptive (avoid '${ndMatch.groupValues[1]}')",
                            lineNumber = lineNum,
                            severity = IssueSeverity.INFO,
                            detail = "Use meaningful phrases explaining destination rather than generic callouts"
                        )
                    )
                }
            }

            // MD051: Anchor link fragments check
            if (config.isRuleEnabled("MD051") && type != LineType.CODE_FENCE_CONTENT) {
                val fragmentRegex = Regex("""\[[^\]]+\]\(#([^)]+)\)""")
                for (match in fragmentRegex.findAll(line)) {
                    val anchor = match.groupValues[1].lowercase().trim()
                    if (anchor.isNotEmpty() && !definedAnchors.contains(anchor)) {
                        issues.add(
                            LintIssue(
                                ruleId = "MD051",
                                ruleName = "link-fragments",
                                description = "Link fragment '#$anchor' does not match any heading in document",
                                lineNumber = lineNum,
                                severity = IssueSeverity.WARNING,
                                detail = "Target heading '#$anchor' not found"
                            )
                        )
                    }
                }
            }
        }

        // MD047: File should end with single newline
        if (config.isRuleEnabled("MD047")) {
            if (!content.endsWith("\n") || content.endsWith("\n\n")) {
                issues.add(
                    LintIssue(
                        ruleId = "MD047",
                        ruleName = "single-trailing-newline",
                        description = "Files should end with a single newline character",
                        lineNumber = lineCount,
                        severity = IssueSeverity.INFO,
                        detail = if (!content.endsWith("\n")) "File missing trailing newline" else "File has multiple trailing newlines",
                        fixInfo = FixInfo(
                            lineNumber = lineCount,
                            originalText = lines.last(),
                            replacementText = lines.last().trimEnd() + "\n",
                            description = "Ensure single trailing newline"
                        )
                    )
                )
            }
        }

        return issues.sortedBy { it.lineNumber }
    }

    private fun slugify(text: String): String {
        return text.lowercase()
            .replace(Regex("""[^\w\s-]"""), "")
            .trim()
            .replace(Regex("""\s+"""), "-")
    }

    private enum class LineType {
        NORMAL,
        FRONT_MATTER,
        CODE_FENCE_START,
        CODE_FENCE_CONTENT,
        HEADING,
        TABLE_ROW
    }

    private fun analyzeLineTypes(lines: List<String>): List<LineType> {
        val types = ArrayList<LineType>(lines.size)
        var inCodeFence = false
        var inFrontMatter = false

        for (i in lines.indices) {
            val line = lines[i].trim()

            if (i == 0 && line == "---") {
                inFrontMatter = true
                types.add(LineType.FRONT_MATTER)
                continue
            }
            if (inFrontMatter) {
                types.add(LineType.FRONT_MATTER)
                if (line == "---" || line == "...") {
                    inFrontMatter = false
                }
                continue
            }

            if (line.startsWith("```") || line.startsWith("~~~")) {
                if (inCodeFence) {
                    inCodeFence = false
                    types.add(LineType.CODE_FENCE_START)
                } else {
                    inCodeFence = true
                    types.add(LineType.CODE_FENCE_START)
                }
                continue
            }

            if (inCodeFence) {
                types.add(LineType.CODE_FENCE_CONTENT)
            } else if (line.startsWith("#")) {
                types.add(LineType.HEADING)
            } else if (line.startsWith("|") && line.endsWith("|")) {
                types.add(LineType.TABLE_ROW)
            } else {
                types.add(LineType.NORMAL)
            }
        }
        return types
    }
}

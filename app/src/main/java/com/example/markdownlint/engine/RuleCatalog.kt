package com.example.markdownlint.engine

import com.example.markdownlint.model.LintRule
import com.example.markdownlint.model.ParamType
import com.example.markdownlint.model.RuleConfigParam

object RuleCatalog {

    val allRules: List<LintRule> = listOf(
        LintRule(
            id = "MD001",
            name = "heading-increment",
            description = "Heading levels should only increment by one level at a time",
            category = "headings",
            isFixable = true,
            sampleBad = "# Title\n### Subheading",
            sampleGood = "# Title\n## Subheading\n### Section",
            explanation = "Skipping heading levels (e.g. from H1 directly to H3) confuses document outlines and screen readers."
        ),
        LintRule(
            id = "MD003",
            name = "heading-style",
            description = "Heading style should be consistent throughout the document",
            category = "headings",
            isFixable = true,
            sampleBad = "# Header 1\n\nHeader 2\n--------",
            sampleGood = "# Header 1\n\n## Header 2",
            explanation = "Do not mix ATX (`# ...`) and Setext (`===` or `---`) heading styles within the same document.",
            params = listOf(
                RuleConfigParam("style", "Style", ParamType.ENUM, "consistent", listOf("consistent", "atx", "atx_closed", "setext"))
            )
        ),
        LintRule(
            id = "MD004",
            name = "ul-style",
            description = "Unordered list bullet style should be consistent",
            category = "lists",
            isFixable = true,
            sampleBad = "* Item 1\n- Item 2\n+ Item 3",
            sampleGood = "- Item 1\n- Item 2\n- Item 3",
            explanation = "Using the same bullet marker (*, -, or +) throughout the document produces cleaner markdown.",
            params = listOf(
                RuleConfigParam("style", "Bullet Marker", ParamType.ENUM, "consistent", listOf("consistent", "dash", "asterisk", "plus"))
            )
        ),
        LintRule(
            id = "MD005",
            name = "list-indent",
            description = "Inconsistent indentation for list items at the same level",
            category = "lists",
            isFixable = true,
            sampleBad = "- First item\n  - Nested item\n   - Incorrect indentation",
            sampleGood = "- First item\n  - Nested item 1\n  - Nested item 2",
            explanation = "Items at the same nesting level should use the same number of spaces for indentation."
        ),
        LintRule(
            id = "MD007",
            name = "ul-indent",
            description = "Unordered list indentation should use standard 2 or 4 spaces",
            category = "lists",
            isFixable = true,
            sampleBad = "- Top item\n   - 3 spaces indent",
            sampleGood = "- Top item\n  - 2 spaces indent\n    - 4 spaces indent",
            explanation = "Indent nested list items with 2 or 4 spaces consistently."
        ),
        LintRule(
            id = "MD009",
            name = "no-trailing-spaces",
            description = "Trailing spaces at the end of lines should be removed",
            category = "whitespace",
            isFixable = true,
            sampleBad = "# Title   \nSome paragraph text  ",
            sampleGood = "# Title\nSome paragraph text",
            explanation = "Trailing spaces are invisible and often committed by accident. Use explicit `<br/>` for line breaks when intended."
        ),
        LintRule(
            id = "MD010",
            name = "no-hard-tabs",
            description = "Hard tab characters should be replaced with spaces",
            category = "whitespace",
            isFixable = true,
            sampleBad = "\t- Indented with tab",
            sampleGood = "  - Indented with 2 spaces",
            explanation = "Tabs can render with inconsistent widths across different editors and renderers."
        ),
        LintRule(
            id = "MD011",
            name = "no-reversed-links",
            description = "Reversed link syntax detected (parentheses and brackets swapped)",
            category = "links",
            isFixable = true,
            sampleBad = "(link text)[https://example.com]",
            sampleGood = "[link text](https://example.com)",
            explanation = "Standard markdown links use `[text](url)` format."
        ),
        LintRule(
            id = "MD012",
            name = "no-multiple-blanks",
            description = "Multiple consecutive blank lines should be collapsed to one",
            category = "whitespace",
            isFixable = true,
            sampleBad = "# Heading\n\n\n\nSome paragraph",
            sampleGood = "# Heading\n\nSome paragraph",
            explanation = "More than one consecutive blank line adds unnecessary whitespace to the source file."
        ),
        LintRule(
            id = "MD013",
            name = "line-length",
            description = "Line length should not exceed maximum threshold",
            category = "whitespace",
            isFixable = false,
            defaultEnabled = false,
            sampleBad = "This is a very long line that exceeds the standard eighty characters column threshold in plain text editors.",
            sampleGood = "This is a wrapped line that stays\nwithin standard line length bounds.",
            explanation = "Long lines can be difficult to read in some terminals and side-by-side git diffs.",
            params = listOf(
                RuleConfigParam("line_length", "Max Line Length", ParamType.INTEGER, 80)
            )
        ),
        LintRule(
            id = "MD014",
            name = "commands-show-output",
            description = "Dollar signs before commands without showing output",
            category = "code",
            isFixable = true,
            sampleBad = "```bash\n$ npm install\n$ npm start\n```",
            sampleGood = "```bash\nnpm install\nnpm start\n```",
            explanation = "Prefixing every line with `$` prevents users from easily copying commands into terminal."
        ),
        LintRule(
            id = "MD018",
            name = "no-missing-space-atx",
            description = "No space after hash on atx style heading",
            category = "headings",
            isFixable = true,
            sampleBad = "#Heading 1\n##Heading 2",
            sampleGood = "# Heading 1\n## Heading 2",
            explanation = "ATX headings require a space between the hash markers and heading text."
        ),
        LintRule(
            id = "MD019",
            name = "no-multiple-space-atx",
            description = "Multiple spaces after hash on atx style heading",
            category = "headings",
            isFixable = true,
            sampleBad = "#   Heading With Too Many Spaces",
            sampleGood = "# Heading With Single Space",
            explanation = "Use exactly one space between the hash symbol and the heading text."
        ),
        LintRule(
            id = "MD020",
            name = "no-missing-space-closed-atx",
            description = "No space inside hashes on closed atx style heading",
            category = "headings",
            isFixable = true,
            sampleBad = "#Heading#",
            sampleGood = "# Heading #",
            explanation = "Closed ATX headings require a space before closing hashes."
        ),
        LintRule(
            id = "MD021",
            name = "no-multiple-space-closed-atx",
            description = "Multiple spaces inside hashes on closed atx style heading",
            category = "headings",
            isFixable = true,
            sampleBad = "#   Heading   #",
            sampleGood = "# Heading #",
            explanation = "Use exactly one space inside closed ATX heading hashes."
        ),
        LintRule(
            id = "MD022",
            name = "blanks-around-headings",
            description = "Headings should be surrounded by blank lines",
            category = "headings",
            isFixable = true,
            sampleBad = "Some text\n# Heading\nMore text",
            sampleGood = "Some text\n\n# Heading\n\nMore text",
            explanation = "Blank lines before and after headings improve readability and prevent parsing ambiguity."
        ),
        LintRule(
            id = "MD023",
            name = "heading-start-left",
            description = "Headings must start at the beginning of the line",
            category = "headings",
            isFixable = true,
            sampleBad = "   # Indented Heading",
            sampleGood = "# Left Aligned Heading",
            explanation = "Headings with leading spaces may be parsed as code blocks or indented text."
        ),
        LintRule(
            id = "MD024",
            name = "no-duplicate-heading",
            description = "Multiple headings should not have identical content",
            category = "headings",
            isFixable = false,
            sampleBad = "## Configuration\n...\n## Configuration",
            sampleGood = "## Server Configuration\n...\n## Client Configuration",
            explanation = "Duplicate heading titles create ambiguous anchor links in table of contents."
        ),
        LintRule(
            id = "MD025",
            name = "single-title",
            description = "Multiple top-level headings (H1) in the same document",
            category = "headings",
            isFixable = false,
            sampleBad = "# Document Title\n...\n# Another H1 Title",
            sampleGood = "# Document Title\n## Section 1\n## Section 2",
            explanation = "A markdown document should ideally have a single H1 title serving as the primary heading."
        ),
        LintRule(
            id = "MD026",
            name = "no-trailing-punctuation",
            description = "Trailing punctuation in heading (e.g. ., :, !, ?)",
            category = "headings",
            isFixable = true,
            sampleBad = "# Introduction.",
            sampleGood = "# Introduction",
            explanation = "Headings are labels, not sentences, and should not end with terminal punctuation."
        ),
        LintRule(
            id = "MD027",
            name = "no-multiple-space-blockquote",
            description = "Multiple spaces after blockquote symbol",
            category = "whitespace",
            isFixable = true,
            sampleBad = ">   Quote text with 3 spaces",
            sampleGood = "> Quote text with 1 space",
            explanation = "Use a single space after the `>` blockquote marker."
        ),
        LintRule(
            id = "MD028",
            name = "no-blanks-blockquote",
            description = "Blank line inside blockquote without blockquote marker",
            category = "whitespace",
            isFixable = true,
            sampleBad = "> First paragraph\n\n> Second paragraph",
            sampleGood = "> First paragraph\n>\n> Second paragraph",
            explanation = "Empty lines in blockquotes should include the `>` marker so they remain part of the quote block."
        ),
        LintRule(
            id = "MD029",
            name = "ol-prefix",
            description = "Ordered list item prefix should follow consistent numbering",
            category = "lists",
            isFixable = true,
            sampleBad = "1. Item 1\n1. Item 2\n3. Item 3",
            sampleGood = "1. Item 1\n2. Item 2\n3. Item 3",
            explanation = "Ordered list prefixes should follow 1, 2, 3... or 1, 1, 1... consistently."
        ),
        LintRule(
            id = "MD030",
            name = "list-marker-space",
            description = "Spaces after list markers should be consistent (1 space)",
            category = "lists",
            isFixable = true,
            sampleBad = "-   Too many spaces after dash",
            sampleGood = "- Single space after dash",
            explanation = "Standard markdown uses exactly one space between list bullet/number and item content."
        ),
        LintRule(
            id = "MD031",
            name = "blanks-around-fences",
            description = "Fenced code blocks should be surrounded by blank lines",
            category = "code",
            isFixable = true,
            sampleBad = "Text before\n```json\n{}\n```\nText after",
            sampleGood = "Text before\n\n```json\n{}\n```\n\nText after",
            explanation = "Surround code fences with blank lines for reliable parsing across Markdown parsers."
        ),
        LintRule(
            id = "MD032",
            name = "blanks-around-lists",
            description = "Lists should be surrounded by blank lines",
            category = "lists",
            isFixable = true,
            sampleBad = "Paragraph:\n- List item 1\n- List item 2\nParagraph after",
            sampleGood = "Paragraph:\n\n- List item 1\n- List item 2\n\nParagraph after",
            explanation = "Separating lists from adjacent paragraphs prevents parsing discrepancies."
        ),
        LintRule(
            id = "MD033",
            name = "no-inline-html",
            description = "Inline HTML used in Markdown document",
            category = "html",
            isFixable = false,
            defaultEnabled = false,
            sampleBad = "Text with <span style=\"color: red;\">HTML</span>",
            sampleGood = "Text with **Markdown emphasis**",
            explanation = "Using pure Markdown syntax keeps documents portable across all markdown engines."
        ),
        LintRule(
            id = "MD034",
            name = "no-bare-urls",
            description = "Bare URL used without angle brackets or markdown link",
            category = "links",
            isFixable = true,
            sampleBad = "Visit https://google.com for info",
            sampleGood = "Visit <https://google.com> or [Google](https://google.com)",
            explanation = "Wrap plain URLs in `<...>` or `[text](url)` to prevent parser auto-link variations."
        ),
        LintRule(
            id = "MD035",
            name = "hr-style",
            description = "Horizontal rule style should be consistent",
            category = "whitespace",
            isFixable = true,
            sampleBad = "---\n\n***\n\n___",
            sampleGood = "---\n\n---",
            explanation = "Pick one horizontal rule style (`---`, `***`, or `___`) and use it consistently."
        ),
        LintRule(
            id = "MD036",
            name = "no-emphasis-as-heading",
            description = "Emphasis (bold/italic) used instead of a heading",
            category = "headings",
            isFixable = true,
            sampleBad = "**Section Title**\n\nParagraph content",
            sampleGood = "## Section Title\n\nParagraph content",
            explanation = "Use markdown headings (`##`) rather than standalone bold text for section headers."
        ),
        LintRule(
            id = "MD037",
            name = "no-space-in-emphasis",
            description = "Spaces inside emphasis markers",
            category = "emphasis",
            isFixable = true,
            sampleBad = "This is * italic * and ** bold ** text",
            sampleGood = "This is *italic* and **bold** text",
            explanation = "Do not place whitespace immediately inside asterisks or underscores."
        ),
        LintRule(
            id = "MD038",
            name = "no-space-in-code",
            description = "Spaces inside code span elements",
            category = "code",
            isFixable = true,
            sampleBad = "Run ` npm start ` command",
            sampleGood = "Run `npm start` command",
            explanation = "Spaces immediately inside backticks should be removed unless required for escaping backticks."
        ),
        LintRule(
            id = "MD039",
            name = "no-space-in-links",
            description = "Spaces inside link text brackets",
            category = "links",
            isFixable = true,
            sampleBad = "[ Link Text ](https://example.com)",
            sampleGood = "[Link Text](https://example.com)",
            explanation = "Avoid leading or trailing spaces inside markdown link text brackets."
        ),
        LintRule(
            id = "MD040",
            name = "fenced-code-language",
            description = "Fenced code blocks should have a language specified",
            category = "code",
            isFixable = false,
            sampleBad = "```\nconst x = 1;\n```",
            sampleGood = "```javascript\nconst x = 1;\n```",
            explanation = "Adding a language identifier enables proper syntax highlighting in all Markdown renderers."
        ),
        LintRule(
            id = "MD041",
            name = "first-line-heading",
            description = "First line in a file should be a top-level heading",
            category = "headings",
            isFixable = false,
            defaultEnabled = true,
            sampleBad = "Welcome to our repository.\n\n# Project Title",
            sampleGood = "# Project Title\n\nWelcome to our repository.",
            explanation = "Documents should start with an H1 heading representing the title."
        ),
        LintRule(
            id = "MD042",
            name = "no-empty-links",
            description = "No empty links (empty URL or empty text)",
            category = "links",
            isFixable = false,
            sampleBad = "[](https://example.com) or [text]()",
            sampleGood = "[Click here](https://example.com)",
            explanation = "Links should have both descriptive label text and a valid destination URL."
        ),
        LintRule(
            id = "MD044",
            name = "proper-names",
            description = "Proper names should have the correct case",
            category = "content",
            isFixable = true,
            sampleBad = "Built with Javascript and Github Actions.",
            sampleGood = "Built with JavaScript and GitHub Actions.",
            explanation = "Ensures common technology and brand names use canonical capitalization."
        ),
        LintRule(
            id = "MD045",
            name = "no-alt-text",
            description = "Images should have alternate text",
            category = "links",
            isFixable = false,
            sampleBad = "![](https://example.com/logo.png)",
            sampleGood = "![Company Logo](https://example.com/logo.png)",
            explanation = "Alt text is required for screen reader accessibility and image fallback display."
        ),
        LintRule(
            id = "MD046",
            name = "code-block-style",
            description = "Code block style should be consistent (fenced vs indented)",
            category = "code",
            isFixable = true,
            sampleBad = "    // Indented block\n\n```js\n// Fenced block\n```",
            sampleGood = "```js\n// Fenced block 1\n```\n\n```js\n// Fenced block 2\n```",
            explanation = "Prefer fenced code blocks (` ``` `) over 4-space indented blocks for consistency."
        ),
        LintRule(
            id = "MD047",
            name = "single-trailing-newline",
            description = "Files should end with a single newline character",
            category = "whitespace",
            isFixable = true,
            sampleBad = "# Title\nEnd of file without newline",
            sampleGood = "# Title\nEnd of file with newline\n",
            explanation = "POSIX standard requires text files to terminate with a single newline character."
        ),
        LintRule(
            id = "MD048",
            name = "code-fence-style",
            description = "Code fence style should be consistent (backticks vs tildes)",
            category = "code",
            isFixable = true,
            sampleBad = "```js\nconst a = 1;\n```\n\n~~~json\n{}\n~~~",
            sampleGood = "```js\nconst a = 1;\n```\n\n```json\n{}\n```",
            explanation = "Do not alternate between backticks (```) and tildes (~~~)."
        ),
        LintRule(
            id = "MD051",
            name = "link-fragments",
            description = "Link fragments should point to valid heading anchors",
            category = "links",
            isFixable = false,
            sampleBad = "[Go to Config](#configuration-settings)\n\n## Settings",
            sampleGood = "[Go to Settings](#settings)\n\n## Settings",
            explanation = "Internal anchor links (`#anchor`) must match a defined heading in the document."
        ),
        LintRule(
            id = "MD055",
            name = "table-pipe-style",
            description = "Table pipe style should be consistent (leading and trailing pipes)",
            category = "tables",
            isFixable = true,
            sampleBad = "| Col 1 | Col 2 |\n|---|---|\nCol 1 | Col 2",
            sampleGood = "| Col 1 | Col 2 |\n|---|---|\n| Val 1 | Val 2 |",
            explanation = "Markdown tables should consistently use outer border pipes `|`."
        ),
        LintRule(
            id = "MD056",
            name = "table-column-count",
            description = "Table rows should have consistent column count",
            category = "tables",
            isFixable = false,
            sampleBad = "| Header 1 | Header 2 | Header 3 |\n|---|---|---|\n| Only one |",
            sampleGood = "| Header 1 | Header 2 | Header 3 |\n|---|---|---|\n| Val 1 | Val 2 | Val 3 |",
            explanation = "Each data row in a markdown table must have the same number of cells as the header row."
        ),
        LintRule(
            id = "MD058",
            name = "blanks-around-tables",
            description = "Tables should be surrounded by blank lines",
            category = "tables",
            isFixable = true,
            sampleBad = "Summary text:\n| H1 | H2 |\n|---|---|\n| A | B |\nEnd of table",
            sampleGood = "Summary text:\n\n| H1 | H2 |\n|---|---|\n| A | B |\n\nEnd of table",
            explanation = "Surround Markdown tables with blank lines for reliable rendering across all engines."
        ),
        LintRule(
            id = "MD060",
            name = "descriptive-link-text",
            description = "Link text should be descriptive (avoid 'click here', 'link', 'here')",
            category = "links",
            isFixable = false,
            sampleBad = "For more information [click here](https://example.com).",
            sampleGood = "For more information, see our [Installation Guide](https://example.com).",
            explanation = "Non-descriptive link text hurts accessibility and SEO for screen reader users."
        )
    )

    fun getRuleById(id: String): LintRule? = allRules.find { it.id.equals(id, ignoreCase = true) }
}

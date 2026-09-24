package com.example.markdownlint.data

import com.example.markdownlint.model.MarkdownDocument

object SampleDocuments {

    val demoDocument = MarkdownDocument(
        id = 1,
        title = "MarkdownLint Demo",
        isSample = true,
        content = """# MarkdownLint Demo Document

Welcome to **MarkdownLint** for Android! This document has common markdown formatting issues for testing.

### Subheading (MD001: Skipped Level)
The heading above jumped from H1 directly to H3.

#Heading Without Space (MD018: Missing Space)
This heading is missing a space after the `#` character.

* Unordered list item 1
- Inconsistent bullet marker (MD004)
+ Mixed plus marker

Here is a reversed link: (Markdown Guide)[https://markdownguide.org] (MD011)

This line has trailing whitespace at the end!     

Here is a bare URL: https://github.com/DavidAnson/markdownlint (MD034)

Notice the capitalization of javascript and github (MD044 proper names).

```bash
$ npm install markdownlint
$ npm test
```

>   Blockquote with multiple spaces after symbol (MD027)

* bold with internal spaces * (MD037)

| Name | Role | Status |
| --- | --- | --- |
| Alice | Lead | Active |
| Bob | Dev | Inactive |

"""
    )

    val readmeTemplate = MarkdownDocument(
        id = 2,
        title = "Project README",
        isSample = true,
        content = """# Awesome Android App

A blazing fast, modern Android application built with Jetpack Compose and Kotlin.

## Features

- **Real-Time Linting**: Instantly flags markdown syntax and style issues.
- **One-Click Auto-Fix**: Resolves formatting discrepancies automatically.
- **Rule Explorer**: Deep-dive into MD001 to MD060 rules with good and bad examples.
- **Live Markdown Preview**: Formatted rich preview with copyable code blocks.

## Installation

Add the dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.example.markdownlint:engine:1.0.0")
}
```

## Quick Start

```kotlin
val engine = MarkdownLintEngine()
val issues = engine.lint("# My Title\n\nContent here.")
println("Found ${'$'}{issues.size} issues.")
```

## Configuration

| Rule | Description | Default |
| --- | --- | --- |
| MD001 | Heading increment | Enabled |
| MD009 | No trailing spaces | Enabled |
| MD018 | Space after ATX hash | Enabled |

## Contributing

Please read [Contributing Guidelines](https://github.com) before submitting pull requests.

## License

Distributed under the MIT License.
"""
    )

    val apiDocsTemplate = MarkdownDocument(
        id = 3,
        title = "REST API Specification",
        isSample = true,
        content = """# Linter Service API

Specification for the Markdown validation microservice.

## Endpoints

### 1. Lint Markdown Content

`POST /api/v1/lint`

Request Payload:

```json
{
  "content": "# Test Document\n\nSome body text.",
  "preset": "strict"
}
```

Response (200 OK):

```json
{
  "issuesCount": 0,
  "issues": [],
  "valid": true
}
```

### 2. Auto-Fix Document

`POST /api/v1/fix`

Request Payload:

```json
{
  "content": "#Header\n- bullet 1\n* bullet 2",
  "ruleIds": ["MD018", "MD004"]
}
```

Response (200 OK):

```json
{
  "fixedContent": "# Header\n- bullet 1\n- bullet 2\n",
  "fixedIssues": 2
}
```
"""
    )

    val allSamples = listOf(demoDocument, readmeTemplate, apiDocsTemplate)
}

# MarkdownLint for Android

A native Android application built with **Kotlin** and **Jetpack Compose** that brings the power of **MarkdownLint** (DavidAnson/markdownlint) to mobile devices and tablets.

## Features

- **Live Real-Time Linting**: Instant feedback with inline line gutter badges and violation counters as you write Markdown.
- **Comprehensive Rules Engine**: Full native Kotlin implementation of core Markdownlint rules (MD001 through MD060) spanning headings, unordered/ordered lists, whitespace, code fences, blockquotes, links, HTML, tables, and typography.
- **One-Click Auto-Fix**: Automatically resolve formatting errors (trailing whitespace, missing space after `#`, multiple blank lines, tabs to spaces, list bullet markers, blockquote spaces, etc.) with a single tap.
- **Rich Markdown Editor**: Dedicated formatting toolbar for inserting headings, bold/italic styles, code blocks, tables, task lists, links, images, and quotes.
- **Live Markdown Preview**: Split or tabbed formatted rendering with styled headers, blockquotes, syntax code blocks with copy action, checklists, and tables.
- **Rule Explorer & Documentation**: Searchable catalog of all rules with good/bad visual comparison examples, explanation notes, and individual toggle switches.
- **Configuration Profiles & Presets**: Built-in presets (Default Recommended, Strict, Prettier-Compatible, Relaxed, GitHub README) with JSON export to `.markdownlint.json`.
- **Local Document Storage**: Offline-first Room database for saving and managing multiple documents, templates, and custom presets.
- **Document Analytics**: Detailed breakdown of words, lines, characters, heading hierarchy, and lint issues.

## Tech Stack

- **Kotlin 2.0.21**
- **Jetpack Compose (Material 3)**
- **AndroidX Navigation Compose**
- **Room Database with KSP**
- **Kotlinx Serialization & Coroutines**
- **Adaptive Layouts** (Mobile and Tablet Navigation Rail)

## License

MIT License.

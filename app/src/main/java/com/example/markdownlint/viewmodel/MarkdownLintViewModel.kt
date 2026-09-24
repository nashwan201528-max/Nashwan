package com.example.markdownlint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.markdownlint.data.DocumentRepository
import com.example.markdownlint.data.SampleDocuments
import com.example.markdownlint.engine.MarkdownFixer
import com.example.markdownlint.engine.MarkdownLintEngine
import com.example.markdownlint.engine.PresetCatalog
import com.example.markdownlint.engine.RuleCatalog
import com.example.markdownlint.model.ConfigPreset
import com.example.markdownlint.model.LintConfiguration
import com.example.markdownlint.model.LintIssue
import com.example.markdownlint.model.MarkdownDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class LintUiState(
    val currentDocument: MarkdownDocument = SampleDocuments.demoDocument,
    val content: String = SampleDocuments.demoDocument.content,
    val issues: List<LintIssue> = emptyList(),
    val configuration: LintConfiguration = PresetCatalog.defaultConfiguration,
    val activePresetId: String = "default",
    val selectedLine: Int? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val statusMessage: String? = null
)

class MarkdownLintViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(LintUiState())
    val uiState: StateFlow<LintUiState> = _uiState.asStateFlow()

    val allDocuments: StateFlow<List<MarkdownDocument>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDocuments.allSamples)

    val allPresets: StateFlow<List<ConfigPreset>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PresetCatalog.builtInPresets)

    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()

    private var engine = MarkdownLintEngine(_uiState.value.configuration)

    init {
        // Initial lint
        runLint()

        // Sync with documents if empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.allDocuments.collect { docs ->
                if (docs.isNotEmpty() && _uiState.value.currentDocument.id == 0L) {
                    val first = docs.first()
                    _uiState.update { it.copy(currentDocument = first, content = first.content) }
                    runLint()
                }
            }
        }
    }

    fun onContentChange(newContent: String) {
        if (newContent == _uiState.value.content) return
        undoStack.add(_uiState.value.content)
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()

        _uiState.update {
            it.copy(
                content = newContent,
                canUndo = undoStack.isNotEmpty(),
                canRedo = false
            )
        }
        runLint()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val prev = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(_uiState.value.content)

        _uiState.update {
            it.copy(
                content = prev,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
        runLint()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(_uiState.value.content)

        _uiState.update {
            it.copy(
                content = next,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
        runLint()
    }

    fun insertSnippet(prefix: String, suffix: String, defaultPlaceholder: String) {
        val current = _uiState.value.content
        val snippet = "$prefix$defaultPlaceholder$suffix"
        val updated = if (current.isEmpty()) snippet else "$current\n$snippet"
        onContentChange(updated)
    }

    fun fixSingleIssue(issue: LintIssue) {
        val currentContent = _uiState.value.content
        val fixed = MarkdownFixer.fixSingleIssue(currentContent, issue)
        if (fixed != currentContent) {
            onContentChange(fixed)
            _uiState.update { it.copy(statusMessage = "Fixed ${issue.ruleId} on line ${issue.lineNumber}") }
        }
    }

    fun fixAllIssues() {
        val currentContent = _uiState.value.content
        val currentIssues = _uiState.value.issues
        val (fixedContent, count) = MarkdownFixer.fixAllIssues(currentContent, currentIssues)
        if (count > 0) {
            onContentChange(fixedContent)
            _uiState.update { it.copy(statusMessage = "Automatically resolved $count issues") }
        } else {
            _uiState.update { it.copy(statusMessage = "No auto-fixable issues remaining") }
        }
    }

    fun selectDocument(doc: MarkdownDocument) {
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                currentDocument = doc,
                content = doc.content,
                canUndo = false,
                canRedo = false,
                selectedLine = null
            )
        }
        runLint()
    }

    fun createNewDocument(title: String, content: String = "# $title\n\nWrite markdown content here.\n") {
        viewModelScope.launch(Dispatchers.IO) {
            val newDoc = MarkdownDocument(
                title = title.ifBlank { "Untitled Document" },
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveDocument(newDoc)
            val created = newDoc.copy(id = id)
            selectDocument(created)
        }
    }

    fun saveCurrentDocument() {
        val doc = _uiState.value.currentDocument
        val content = _uiState.value.content
        viewModelScope.launch(Dispatchers.IO) {
            val updated = doc.copy(content = content, updatedAt = System.currentTimeMillis())
            repository.saveDocument(updated)
            _uiState.update { it.copy(currentDocument = updated, statusMessage = "Document saved") }
        }
    }

    fun deleteDocument(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDocument(id)
            if (_uiState.value.currentDocument.id == id) {
                selectDocument(SampleDocuments.demoDocument)
            }
        }
    }

    fun toggleRule(ruleId: String) {
        val currentMap = _uiState.value.configuration.enabledRules.toMutableMap()
        val isCurrentlyEnabled = currentMap[ruleId] ?: true
        currentMap[ruleId] = !isCurrentlyEnabled

        val updatedConfig = _uiState.value.configuration.copy(enabledRules = currentMap)
        _uiState.update { it.copy(configuration = updatedConfig) }
        engine = MarkdownLintEngine(updatedConfig)
        runLint()
    }

    fun enableAllRules() {
        val allMap = RuleCatalog.allRules.associate { it.id to true }
        val updatedConfig = _uiState.value.configuration.copy(enabledRules = allMap)
        _uiState.update { it.copy(configuration = updatedConfig) }
        engine = MarkdownLintEngine(updatedConfig)
        runLint()
    }

    fun disableAllRules() {
        val allMap = RuleCatalog.allRules.associate { it.id to false }
        val updatedConfig = _uiState.value.configuration.copy(enabledRules = allMap)
        _uiState.update { it.copy(configuration = updatedConfig) }
        engine = MarkdownLintEngine(updatedConfig)
        runLint()
    }

    fun applyPreset(preset: ConfigPreset) {
        try {
            val parsedConfig = json.decodeFromString<LintConfiguration>(preset.configJson)
            _uiState.update { it.copy(configuration = parsedConfig, activePresetId = preset.id) }
            engine = MarkdownLintEngine(parsedConfig)
            runLint()
            _uiState.update { it.copy(statusMessage = "Applied preset '${preset.name}'") }
        } catch (e: Exception) {
            _uiState.update { it.copy(statusMessage = "Error parsing preset config") }
        }
    }

    fun saveCustomPreset(name: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val configJson = json.encodeToString(_uiState.value.configuration)
            val preset = ConfigPreset(
                id = "custom_" + System.currentTimeMillis(),
                name = name,
                description = description,
                isBuiltIn = false,
                configJson = configJson
            )
            repository.savePreset(preset)
            _uiState.update { it.copy(statusMessage = "Saved preset '$name'") }
        }
    }

    fun deletePreset(preset: ConfigPreset) {
        if (preset.isBuiltIn) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePreset(preset)
            _uiState.update { it.copy(statusMessage = "Deleted preset '${preset.name}'") }
        }
    }

    fun jumpToLine(lineNumber: Int) {
        _uiState.update { it.copy(selectedLine = lineNumber) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun runLint() {
        viewModelScope.launch(Dispatchers.Default) {
            val content = _uiState.value.content
            val result = engine.lint(content)
            _uiState.update { it.copy(issues = result) }
        }
    }
}

class MarkdownLintViewModelFactory(
    private val repository: DocumentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MarkdownLintViewModel(repository) as T
    }
}

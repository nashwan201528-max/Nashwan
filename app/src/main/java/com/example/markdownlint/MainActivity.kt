package com.example.markdownlint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.markdownlint.data.AppDatabase
import com.example.markdownlint.data.DocumentRepository
import com.example.markdownlint.model.IssueSeverity
import com.example.markdownlint.ui.components.FileStatsBottomSheet
import com.example.markdownlint.ui.screens.DocumentsScreen
import com.example.markdownlint.ui.screens.EditorScreen
import com.example.markdownlint.ui.screens.IssuesScreen
import com.example.markdownlint.ui.screens.PresetsScreen
import com.example.markdownlint.ui.screens.PreviewScreen
import com.example.markdownlint.ui.screens.RulesScreen
import com.example.markdownlint.ui.theme.AccentCyan
import com.example.markdownlint.ui.theme.MarkdownLintTheme
import com.example.markdownlint.ui.theme.SeverityError
import com.example.markdownlint.ui.theme.SeverityWarning
import com.example.markdownlint.viewmodel.MarkdownLintViewModel
import com.example.markdownlint.viewmodel.MarkdownLintViewModelFactory
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val icon: ImageVector, val tag: String) {
    EDITOR("Editor", Icons.Default.Edit, "nav_editor"),
    ISSUES("Issues", Icons.Default.BugReport, "nav_issues"),
    PREVIEW("Preview", Icons.Default.Preview, "nav_preview"),
    RULES("Rules", Icons.Default.Rule, "nav_rules"),
    PRESETS("Presets", Icons.Default.Tune, "nav_presets"),
    FILES("Files", Icons.Default.Folder, "nav_files")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
        val database = AppDatabase.getDatabase(this, coroutineScope)
        val repository = DocumentRepository(database.documentDao(), database.presetDao())

        val viewModel: MarkdownLintViewModel by viewModels {
            MarkdownLintViewModelFactory(repository)
        }

        setContent {
            MarkdownLintTheme {
                MarkdownLintApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownLintApp(viewModel: MarkdownLintViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allDocs by viewModel.allDocuments.collectAsStateWithLifecycle()
    val allPresets by viewModel.allPresets.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppDestination.EDITOR) }
    var showStatsSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val isTabletOrLandscape = configuration.screenWidthDp >= 600

    val fixableCount = uiState.issues.count { it.isFixable }
    val errorCount = uiState.issues.count { it.severity == IssueSeverity.ERROR }

    // Show status messages from ViewModel
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = "Markdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lint",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (fixableCount > 0 && currentDestination != AppDestination.EDITOR) {
                        IconButton(
                            onClick = { viewModel.fixAllIssues() },
                            modifier = Modifier.testTag("topbar_btn_fix_all")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Fix All Issues",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (!isTabletOrLandscape) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    AppDestination.entries.forEach { destination ->
                        val isSelected = currentDestination == destination

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            modifier = Modifier.testTag(destination.tag),
                            icon = {
                                if (destination == AppDestination.ISSUES && uiState.issues.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = if (errorCount > 0) SeverityError else SeverityWarning
                                            ) {
                                                Text("${uiState.issues.size}")
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = destination.icon, contentDescription = destination.label)
                                    }
                                } else {
                                    Icon(imageVector = destination.icon, contentDescription = destination.label)
                                }
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tablet / Wide Screen Side Navigation Rail
            if (isTabletOrLandscape) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    AppDestination.entries.forEach { destination ->
                        val isSelected = currentDestination == destination

                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            modifier = Modifier.testTag(destination.tag),
                            icon = {
                                if (destination == AppDestination.ISSUES && uiState.issues.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = if (errorCount > 0) SeverityError else SeverityWarning
                                            ) {
                                                Text("${uiState.issues.size}")
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = destination.icon, contentDescription = destination.label)
                                    }
                                } else {
                                    Icon(imageVector = destination.icon, contentDescription = destination.label)
                                }
                            },
                            label = { Text(destination.label, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                when (currentDestination) {
                    AppDestination.EDITOR -> {
                        EditorScreen(
                            state = uiState,
                            onContentChange = { viewModel.onContentChange(it) },
                            onUndo = { viewModel.undo() },
                            onRedo = { viewModel.redo() },
                            onFixAll = { viewModel.fixAllIssues() },
                            onSaveDoc = { viewModel.saveCurrentDocument() },
                            onOpenStats = { showStatsSheet = true },
                            onInsertSnippet = { prefix, suffix, defaultPlaceholder ->
                                viewModel.insertSnippet(prefix, suffix, defaultPlaceholder)
                            },
                            onNavigateToIssues = { currentDestination = AppDestination.ISSUES },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.ISSUES -> {
                        IssuesScreen(
                            issues = uiState.issues,
                            onIssueClick = { issue ->
                                viewModel.jumpToLine(issue.lineNumber)
                                currentDestination = AppDestination.EDITOR
                            },
                            onQuickFix = { viewModel.fixSingleIssue(it) },
                            onFixAll = { viewModel.fixAllIssues() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.PREVIEW -> {
                        PreviewScreen(
                            content = uiState.content,
                            title = uiState.currentDocument.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.RULES -> {
                        RulesScreen(
                            config = uiState.configuration,
                            onToggleRule = { viewModel.toggleRule(it) },
                            onEnableAll = { viewModel.enableAllRules() },
                            onDisableAll = { viewModel.disableAllRules() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.PRESETS -> {
                        PresetsScreen(
                            presets = allPresets,
                            currentPresetId = uiState.activePresetId,
                            currentConfig = uiState.configuration,
                            onApplyPreset = { viewModel.applyPreset(it) },
                            onSaveCustomPreset = { name, desc -> viewModel.saveCustomPreset(name, desc) },
                            onDeletePreset = { viewModel.deletePreset(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.FILES -> {
                        DocumentsScreen(
                            documents = allDocs,
                            currentDocId = uiState.currentDocument.id,
                            onSelectDocument = {
                                viewModel.selectDocument(it)
                                currentDestination = AppDestination.EDITOR
                            },
                            onCreateDocument = { title ->
                                viewModel.createNewDocument(title)
                                currentDestination = AppDestination.EDITOR
                            },
                            onDeleteDocument = { viewModel.deleteDocument(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // File Statistics Sheet
        if (showStatsSheet) {
            FileStatsBottomSheet(
                title = uiState.currentDocument.title,
                content = uiState.content,
                issues = uiState.issues,
                onDismiss = { showStatsSheet = false }
            )
        }
    }
}

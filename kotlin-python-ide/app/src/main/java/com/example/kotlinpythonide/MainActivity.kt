package com.example.kotlinpythonide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlinpythonide.runtime.ProjectRepository
import com.example.kotlinpythonide.runtime.PythonRuntime
import com.example.kotlinpythonide.ui.IdeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IdeTheme {
                PythonIdeApp()
            }
        }
    }
}

private enum class IdeTab(val label: String) {
    Explorer("Explorer"),
    Packages("Packages"),
    Settings("Settings"),
}

@Composable
private fun PythonIdeApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { ProjectRepository(context) }
    val scope = rememberCoroutineScope()
    val runtime = remember { PythonRuntime(context) }
    var projects by remember { mutableStateOf(repository.listProjects()) }
    var selectedProject by remember { mutableStateOf(projects.firstOrNull()) }
    var selectedFile by remember { mutableStateOf("main.py") }
    var editorText by remember { mutableStateOf("") }
    var consoleText by remember { mutableStateOf("Python runtime ready. Select Run to execute main.py.") }
    var activeTab by remember { mutableStateOf(IdeTab.Explorer) }
    var showNewProject by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }

    LaunchedEffect(selectedProject, selectedFile) {
        editorText = selectedProject?.let { repository.readFile(it, selectedFile) } ?: ""
    }

    fun saveFile() {
        selectedProject?.let { project ->
            repository.writeFile(project, selectedFile, editorText)
            consoleText = "Saved $selectedFile"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("KODEX", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Text(
                                    selectedProject?.name ?: "No project",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { saveFile() }, enabled = selectedProject != null) {
                            Icon(Icons.Default.Save, contentDescription = "Save file")
                        }
                        IconButton(
                            onClick = {
                                if (selectedProject != null) {
                                    isBusy = true
                                    scope.launch {
                                        saveFile()
                                        consoleText = withContext(Dispatchers.IO) {
                                            runtime.run(editorText, selectedProject!!)
                                        }
                                        isBusy = false
                                    }
                                }
                            },
                            enabled = selectedProject != null && !isBusy,
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run Python")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            contentWindowInsets = WindowInsets.navigationBars,
        ) { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    NavigationRailItem(
                        selected = activeTab == IdeTab.Explorer,
                        onClick = { activeTab = IdeTab.Explorer },
                        icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                        label = { Text(IdeTab.Explorer.label) },
                    )
                    NavigationRailItem(
                        selected = activeTab == IdeTab.Packages,
                        onClick = { activeTab = IdeTab.Packages },
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                        label = { Text(IdeTab.Packages.label) },
                    )
                    NavigationRailItem(
                        selected = activeTab == IdeTab.Settings,
                        onClick = { activeTab = IdeTab.Settings },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(IdeTab.Settings.label) },
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    when (activeTab) {
                        IdeTab.Explorer -> {
                            ExplorerPane(
                                projects = projects,
                                selectedProject = selectedProject,
                                selectedFile = selectedFile,
                                editorText = editorText,
                                consoleText = consoleText,
                                onNewProject = { showNewProject = true },
                                onSelectProject = { project ->
                                    selectedProject = project
                                    selectedFile = repository.projectFiles(project).firstOrNull()?.name ?: "main.py"
                                },
                                onSelectFile = { selectedFile = it.name },
                                onEditorChange = { editorText = it },
                            )
                        }
                        IdeTab.Packages -> {
                            PackagesPane(
                                project = selectedProject,
                                packageName = packageName,
                                onPackageNameChange = { packageName = it },
                                onInstall = {
                                    val project = selectedProject ?: return@PackagesPane
                                    if (packageName.isBlank()) return@PackagesPane
                                    isBusy = true
                                    scope.launch {
                                        consoleText = withContext(Dispatchers.IO) {
                                            runtime.install(packageName.trim(), project)
                                        }
                                        packageName = ""
                                        isBusy = false
                                    }
                                },
                                busy = isBusy,
                            )
                        }
                        IdeTab.Settings -> SettingsPane(repository = repository, runtime = runtime)
                    }
                }
            }
        }
    }

    if (showNewProject) {
        AlertDialog(
            onDismissRequest = { showNewProject = false },
            title = { Text("Create Python project") },
            text = {
                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    singleLine = true,
                    label = { Text("Project name") },
                    placeholder = { Text("weather-scripts") },
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val created = repository.createProject(newProjectName)
                        projects = repository.listProjects()
                        selectedProject = created
                        selectedFile = "main.py"
                        newProjectName = ""
                        showNewProject = false
                    },
                    enabled = newProjectName.isNotBlank(),
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProject = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ExplorerPane(
    projects: List<File>,
    selectedProject: File?,
    selectedFile: String,
    editorText: String,
    consoleText: String,
    onNewProject: () -> Unit,
    onSelectProject: (File) -> Unit,
    onSelectFile: (File) -> Unit,
    onEditorChange: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(210.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("PROJECTS", style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
                IconButton(onClick = onNewProject) {
                    Icon(Icons.Default.Add, contentDescription = "New project")
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(projects, key = { it.absolutePath }) { project ->
                    val selected = selectedProject?.absolutePath == project.absolutePath
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProject(project) },
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            project.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    selectedProject?.name ?: "Create a project to begin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (selectedProject == null) "Local workspace" else "${selectedProject.listFiles()?.size ?: 0} files",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .width(150.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                ) {
                    Text("FILES", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    selectedProject?.let { project ->
                        project.listFiles()
                            ?.filter { it.isFile }
                            ?.sortedBy { it.name }
                            ?.forEach { file ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectFile(file) },
                                    color = if (selectedFile == file.name) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp),
                                ) {
                                    Text(
                                        file.name,
                                        modifier = Modifier.padding(8.dp),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Surface(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        color = Color(0xFF11181D),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        BasicTextField(
                            value = editorText,
                            onValueChange = onEditorChange,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState()),
                            textStyle = TextStyle(
                                color = Color(0xFFE1E8E7),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (editorText.isEmpty()) {
                                    Text(
                                        "Write Python here...",
                                        color = Color(0xFF70807F),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    ConsolePane(consoleText = consoleText)
                }
            }
        }
    }
}

@Composable
private fun ConsolePane(consoleText: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        color = Color(0xFF0B1114),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(7.dp))
                Text("OUTPUT", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                consoleText,
                modifier = Modifier.verticalScroll(rememberScrollState()),
                color = Color(0xFFB8C8C5),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun PackagesPane(
    project: File?,
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    onInstall: () -> Unit,
    busy: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Project environment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Install pure-Python dependencies into an isolated package directory for this project.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Active workspace", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(project?.name ?: "No project selected", fontWeight = FontWeight.SemiBold)
                Text(
                    project?.resolve(".packages")?.absolutePath ?: "Select a project first",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = packageName,
                onValueChange = onPackageNameChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Package name") },
                placeholder = { Text("requests") },
            )
            Spacer(Modifier.width(10.dp))
            Button(onClick = onInstall, enabled = project != null && packageName.isNotBlank() && !busy) {
                Text(if (busy) "Installing…" else "Install")
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Compatibility note", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "The Android runtime supports Python packages that are pure Python or available as Chaquopy-compatible wheels. Desktop packages that require Linux system libraries will not work in an Android sandbox.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsPane(repository: ProjectRepository, runtime: PythonRuntime) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("IDE settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        SettingRow("Python runtime", "Embedded CPython ${runtime.version}")
        SettingRow("Storage", repository.root.absolutePath)
        SettingRow("Isolation", "One .packages directory per project")
        SettingRow("Minimum Android", "Android 8.0 (API 26)")
        Spacer(Modifier.height(22.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Projects stay inside the app sandbox. Export/import can be added later with Android's Storage Access Framework when you need to move work between devices.",
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
        Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}
package com.cloudfilemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudfilemanager.data.ConnectionState
import com.cloudfilemanager.data.RemoteFile
import com.cloudfilemanager.ui.FileManagerViewModel
import com.cloudfilemanager.ui.components.FileDetailPane
import com.cloudfilemanager.ui.components.FileListPane
import com.cloudfilemanager.ui.components.ServerConfigDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel = viewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val files by viewModel.files.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val serverConfig by viewModel.serverConfig.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()
    
    var showConfigDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<RemoteFile?>(null) }
    
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = connectionState) {
                        is ConnectionState.Connected -> {
                            Column {
                                Text("云文件管理器")
                                Text(
                                    text = state.serverName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        is ConnectionState.Connecting -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("正在连接...")
                            }
                        }
                        is ConnectionState.Disconnected -> {
                            Text("云文件管理器")
                        }
                        is ConnectionState.Error -> {
                            Column {
                                Text("云文件管理器")
                                Text(
                                    text = "连接失败",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                actions = {
                    when (connectionState) {
                        is ConnectionState.Connected -> {
                            IconButton(onClick = { showCreateFolderDialog = true }) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = "新建文件夹")
                            }
                            IconButton(onClick = { viewModel.disconnect() }) {
                                Icon(Icons.Default.LinkOff, contentDescription = "断开连接")
                            }
                        }
                        else -> {
                            IconButton(onClick = { showConfigDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "配置")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("关闭")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    ) { paddingValues ->
        when (connectionState) {
            is ConnectionState.Disconnected,
            is ConnectionState.Error -> {
                WelcomeScreen(
                    onConnectClick = { showConfigDialog = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            is ConnectionState.Connecting -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("正在连接服务器...")
                    }
                }
            }
            
            is ConnectionState.Connected -> {
                if (isTablet) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        FileListPane(
                            files = files,
                            currentPath = currentPath,
                            selectedFile = selectedFile,
                            isLoading = isLoading,
                            onFileClick = { viewModel.selectFile(it) },
                            onFileDoubleClick = { file ->
                                if (file.isDirectory) {
                                    viewModel.navigateToDirectory(file)
                                } else {
                                    viewModel.openFile(file)
                                }
                            },
                            onNavigateUp = { viewModel.navigateUp() },
                            onRefresh = { viewModel.loadFiles(currentPath) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        
                        VerticalDivider()
                        
                        FileDetailPane(
                            file = selectedFile,
                            fileContent = fileContent,
                            isLoading = isLoading,
                            onOpenFile = { viewModel.openFile(it) },
                            onDeleteFile = {
                                fileToDelete = it
                                showDeleteDialog = true
                            },
                            onClose = {
                                viewModel.clearFileContent()
                                viewModel.selectFile(null)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    FileListPane(
                        files = files,
                        currentPath = currentPath,
                        selectedFile = selectedFile,
                        isLoading = isLoading,
                        onFileClick = { viewModel.selectFile(it) },
                        onFileDoubleClick = { file ->
                            if (file.isDirectory) {
                                viewModel.navigateToDirectory(file)
                            } else {
                                viewModel.openFile(file)
                            }
                        },
                        onNavigateUp = { viewModel.navigateUp() },
                        onRefresh = { viewModel.loadFiles(currentPath) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }
    }
    
    if (showConfigDialog) {
        ServerConfigDialog(
            initialConfig = serverConfig,
            onDismiss = { showConfigDialog = false },
            onSave = { config ->
                viewModel.saveConfig(config)
                showConfigDialog = false
            },
            onConnect = { config ->
                viewModel.connect(config)
                showConfigDialog = false
            }
        )
    }
    
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createFolder(name)
                showCreateFolderDialog = false
            }
        )
    }
    
    if (showDeleteDialog && fileToDelete != null) {
        DeleteConfirmDialog(
            fileName = fileToDelete!!.name,
            onDismiss = {
                showDeleteDialog = false
                fileToDelete = null
            },
            onConfirm = {
                viewModel.deleteFile(fileToDelete!!)
                showDeleteDialog = false
                fileToDelete = null
            }
        )
    }
}

@Composable
fun WelcomeScreen(
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "欢迎使用云文件管理器",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "安全便捷地管理您的远程服务器文件",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onConnectClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加服务器")
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文件夹") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("文件夹名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onCreate(folderName) },
                enabled = folderName.isNotEmpty()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除") },
        text = {
            Text("确定要删除 \"$fileName\" 吗？此操作无法撤销。")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

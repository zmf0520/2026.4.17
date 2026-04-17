package com.cloudfilemanager.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cloudfilemanager.data.ConnectionState
import com.cloudfilemanager.data.RemoteFile
import com.cloudfilemanager.data.ServerConfig
import com.cloudfilemanager.ssh.SSHManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sshManager = SSHManager()
    private val prefs = application.getSharedPreferences("server_config", Context.MODE_PRIVATE)
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _currentPath = MutableStateFlow("/")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()
    
    private val _files = MutableStateFlow<List<RemoteFile>>(emptyList())
    val files: StateFlow<List<RemoteFile>> = _files.asStateFlow()
    
    private val _selectedFile = MutableStateFlow<RemoteFile?>(null)
    val selectedFile: StateFlow<RemoteFile?> = _selectedFile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _serverConfig = MutableStateFlow<ServerConfig?>(null)
    val serverConfig: StateFlow<ServerConfig?> = _serverConfig.asStateFlow()
    
    private val _fileContent = MutableStateFlow<String?>(null)
    val fileContent: StateFlow<String?> = _fileContent.asStateFlow()
    
    init {
        loadSavedConfig()
    }
    
    private fun loadSavedConfig() {
        val config = ServerConfig(
            host = prefs.getString("host", "") ?: "",
            port = prefs.getInt("port", 22),
            username = prefs.getString("username", "") ?: "",
            password = prefs.getString("password", null),
            privateKeyPath = prefs.getString("privateKeyPath", null),
            autoConnect = prefs.getBoolean("autoConnect", false),
            name = prefs.getString("name", "My Server") ?: "My Server"
        )
        _serverConfig.value = config
        
        if (config.autoConnect && config.host.isNotEmpty()) {
            connect(config)
        }
    }
    
    fun saveConfig(config: ServerConfig) {
        prefs.edit().apply {
            putString("name", config.name)
            putString("host", config.host)
            putInt("port", config.port)
            putString("username", config.username)
            putString("password", config.password)
            putString("privateKeyPath", config.privateKeyPath)
            putBoolean("autoConnect", config.autoConnect)
            apply()
        }
        _serverConfig.value = config
    }
    
    fun connect(config: ServerConfig) {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Connecting
            _isLoading.value = true
            
            sshManager.connect(config)
                .onSuccess {
                    _connectionState.value = ConnectionState.Connected(config.name)
                    saveConfig(config)
                    loadFiles("/")
                }
                .onFailure { error ->
                    _connectionState.value = ConnectionState.Error(error.message ?: "Connection failed")
                    _errorMessage.value = error.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun disconnect() {
        sshManager.disconnect()
        _connectionState.value = ConnectionState.Disconnected
        _files.value = emptyList()
        _currentPath.value = "/"
        _selectedFile.value = null
    }
    
    fun loadFiles(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            sshManager.listFiles(path)
                .onSuccess { fileList ->
                    _files.value = fileList
                    _currentPath.value = path
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun navigateToDirectory(file: RemoteFile) {
        if (file.isDirectory) {
            loadFiles(file.path)
        }
    }
    
    fun navigateUp() {
        val current = _currentPath.value
        if (current != "/") {
            val parentPath = current.substringBeforeLast("/")
            loadFiles(if (parentPath.isEmpty()) "/" else parentPath)
        }
    }
    
    fun selectFile(file: RemoteFile?) {
        _selectedFile.value = file
    }
    
    fun openFile(file: RemoteFile) {
        if (!file.isDirectory) {
            viewModelScope.launch {
                _isLoading.value = true
                sshManager.readFile(file.path)
                    .onSuccess { content ->
                        _fileContent.value = content
                    }
                    .onFailure { error ->
                        _errorMessage.value = error.message
                    }
                _isLoading.value = false
            }
        }
    }
    
    fun clearFileContent() {
        _fileContent.value = null
    }
    
    fun deleteFile(file: RemoteFile) {
        viewModelScope.launch {
            _isLoading.value = true
            
            sshManager.deleteFile(file.path)
                .onSuccess {
                    loadFiles(_currentPath.value)
                    if (_selectedFile.value == file) {
                        _selectedFile.value = null
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun createFolder(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val path = if (_currentPath.value == "/") {
                "/$name"
            } else {
                "${_currentPath.value}/$name"
            }
            
            sshManager.createFolder(path)
                .onSuccess {
                    loadFiles(_currentPath.value)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun renameFile(file: RemoteFile, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val parentPath = file.path.substringBeforeLast("/")
            val newPath = if (parentPath.isEmpty()) "/$newName" else "$parentPath/$newName"
            
            sshManager.renameFile(file.path, newPath)
                .onSuccess {
                    loadFiles(_currentPath.value)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        sshManager.disconnect()
    }
}

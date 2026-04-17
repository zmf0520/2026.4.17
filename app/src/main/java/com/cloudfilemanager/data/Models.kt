package com.cloudfilemanager.data

data class ServerConfig(
    val id: String = System.currentTimeMillis().toString(),
    val name: String = "",
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val password: String? = null,
    val privateKeyPath: String? = null,
    val autoConnect: Boolean = false
)

data class RemoteFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val permissions: String = ""
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast(".", "")
    
    val formattedSize: String
        get() = formatFileSize(size)
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val serverName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

sealed class FileOperation {
    data class Upload(val localPath: String, val remotePath: String) : FileOperation()
    data class Download(val remotePath: String, val localPath: String) : FileOperation()
    data class Delete(val path: String) : FileOperation()
    data class Rename(val oldPath: String, val newPath: String) : FileOperation()
    data class CreateFolder(val path: String) : FileOperation()
}

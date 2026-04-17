package com.cloudfilemanager.ssh

import android.util.Log
import com.cloudfilemanager.data.RemoteFile
import com.cloudfilemanager.data.ServerConfig
import com.jcraft.jsch.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class SSHManager {
    private var jsch: JSch = JSch()
    private var session: Session? = null
    private var channelSftp: ChannelSftp? = null
    
    companion object {
        private const val TAG = "SSHManager"
        private const val TIMEOUT = 30000
    }
    
    suspend fun connect(config: ServerConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (session?.isConnected == true) {
                return@withContext Result.success(Unit)
            }
            
            session = jsch.getSession(config.username, config.host, config.port).apply {
                if (!config.privateKeyPath.isNullOrEmpty()) {
                    jsch.addIdentity(config.privateKeyPath)
                } else if (!config.password.isNullOrEmpty()) {
                    setPassword(config.password)
                }
                
                setConfig("StrictHostKeyChecking", "no")
                setConfig("UserKnownHostsFile", "/dev/null")
                setTimeout(TIMEOUT)
                connect()
            }
            
            val channel = session?.openChannel("sftp") as? ChannelSftp
            channel?.connect()
            channelSftp = channel
            
            Log.i(TAG, "Connected to ${config.host}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}")
            disconnect()
            Result.failure(e)
        }
    }
    
    fun disconnect() {
        try {
            channelSftp?.disconnect()
            session?.disconnect()
            channelSftp = null
            session = null
            Log.i(TAG, "Disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        }
    }
    
    val isConnected: Boolean
        get() = session?.isConnected == true && channelSftp?.isConnected == true
    
    suspend fun listFiles(path: String = "/"): Result<List<RemoteFile>> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            val files = mutableListOf<RemoteFile>()
            
            val entries = sftp.ls(path)
            entries?.forEach { entry ->
                val item = entry as ChannelSftp.LsEntry
                if (item.filename != "." && item.filename != "..") {
                    files.add(
                        RemoteFile(
                            name = item.filename,
                            path = if (path == "/") "/${item.filename}" else "$path/${item.filename}",
                            isDirectory = item.attrs.isDir,
                            size = item.attrs.size,
                            lastModified = item.attrs.mTime * 1000L,
                            permissions = item.attrs.permissionsString
                        )
                    )
                }
            }
            
            Result.success(files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })))
        } catch (e: Exception) {
            Log.e(TAG, "List files failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun downloadFile(remotePath: String, localPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            sftp.get(remotePath, FileOutputStream(localPath))
            Log.i(TAG, "Downloaded: $remotePath -> $localPath")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun uploadFile(localPath: String, remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            sftp.put(FileInputStream(localPath), remotePath)
            Log.i(TAG, "Uploaded: $localPath -> $remotePath")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            val attrs = sftp.stat(path)
            if (attrs.isDir) {
                sftp.rmdir(path)
            } else {
                sftp.rm(path)
            }
            Log.i(TAG, "Deleted: $path")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun renameFile(oldPath: String, newPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            sftp.rename(oldPath, newPath)
            Log.i(TAG, "Renamed: $oldPath -> $newPath")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Rename failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun createFolder(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            sftp.mkdir(path)
            Log.i(TAG, "Created folder: $path")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Create folder failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            val stream = sftp.get(path)
            val content = stream.bufferedReader().readText()
            stream.close()
            Result.success(content)
        } catch (e: Exception) {
            Log.e(TAG, "Read file failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentPath(): String {
        return try {
            channelSftp?.pwd() ?: "/"
        } catch (e: Exception) {
            "/"
        }
    }
    
    suspend fun changeDirectory(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = channelSftp ?: throw IllegalStateException("Not connected")
            sftp.cd(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Change directory failed: ${e.message}")
            Result.failure(e)
        }
    }
}

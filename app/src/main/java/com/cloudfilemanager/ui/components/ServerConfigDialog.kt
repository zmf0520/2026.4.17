package com.cloudfilemanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cloudfilemanager.data.ServerConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigDialog(
    initialConfig: ServerConfig?,
    onDismiss: () -> Unit,
    onSave: (ServerConfig) -> Unit,
    onConnect: (ServerConfig) -> Unit
) {
    var name by remember { mutableStateOf(initialConfig?.name ?: "My Server") }
    var host by remember { mutableStateOf(initialConfig?.host ?: "") }
    var port by remember { mutableStateOf(initialConfig?.port?.toString() ?: "22") }
    var username by remember { mutableStateOf(initialConfig?.username ?: "") }
    var password by remember { mutableStateOf(initialConfig?.password ?: "") }
    var privateKeyPath by remember { mutableStateOf(initialConfig?.privateKeyPath ?: "") }
    var autoConnect by remember { mutableStateOf(initialConfig?.autoConnect ?: false) }
    var useKeyAuth by remember { mutableStateOf(!initialConfig?.privateKeyPath.isNullOrEmpty()) }
    var showPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "服务器配置",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("服务器名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Label, contentDescription = null)
                        }
                    )
                    
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("主机地址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Dns, contentDescription = null)
                        }
                    )
                    
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it.filter { c -> c.isDigit() } },
                        label = { Text("端口") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.SettingsEthernet, contentDescription = null)
                        }
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("用户名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "认证方式",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !useKeyAuth,
                                onClick = { useKeyAuth = false }
                            )
                            Text("密码", modifier = Modifier.padding(end = 16.dp))
                            
                            RadioButton(
                                selected = useKeyAuth,
                                onClick = { useKeyAuth = true }
                            )
                            Text("密钥")
                        }
                    }
                    
                    if (useKeyAuth) {
                        OutlinedTextField(
                            value = privateKeyPath,
                            onValueChange = { privateKeyPath = it },
                            label = { Text("私钥文件路径") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.VpnKey, contentDescription = null)
                            },
                            placeholder = {
                                Text("/storage/emulated/0/key.pem")
                            }
                        )
                    } else {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密码") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "自动连接",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = autoConnect,
                            onCheckedChange = { autoConnect = it }
                        )
                    }
                    
                    if (autoConnect) {
                        Text(
                            text = "应用启动时将自动连接到此服务器",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    OutlinedButton(
                        onClick = {
                            val config = ServerConfig(
                                name = name,
                                host = host,
                                port = port.toIntOrNull() ?: 22,
                                username = username,
                                password = if (!useKeyAuth) password else null,
                                privateKeyPath = if (useKeyAuth) privateKeyPath else null,
                                autoConnect = autoConnect
                            )
                            onSave(config)
                        }
                    ) {
                        Text("保存")
                    }
                    
                    Button(
                        onClick = {
                            val config = ServerConfig(
                                name = name,
                                host = host,
                                port = port.toIntOrNull() ?: 22,
                                username = username,
                                password = if (!useKeyAuth) password else null,
                                privateKeyPath = if (useKeyAuth) privateKeyPath else null,
                                autoConnect = autoConnect
                            )
                            onConnect(config)
                        }
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("连接")
                    }
                }
            }
        }
    }
}

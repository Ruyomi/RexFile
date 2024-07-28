package com.ruyomi.rexfile.demo

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ruyomi.dev.utils.rex.file.RexFileConfig
import com.ruyomi.dev.utils.rex.file.RexFileModel
import com.ruyomi.dev.utils.rex.file.file
import com.ruyomi.dev.utils.rex.file.hasAllFilePermission
import com.ruyomi.dev.utils.rex.file.hasDocPermission
import com.ruyomi.dev.utils.rex.file.hasRootPermission
import com.ruyomi.dev.utils.rex.file.hasShizukuPermission
import com.ruyomi.dev.utils.rex.file.hasStoragePermission
import com.ruyomi.dev.utils.rex.file.peekShizukuService
import com.ruyomi.dev.utils.rex.file.registerAllFilePermission
import com.ruyomi.dev.utils.rex.file.registerDocPermission
import com.ruyomi.dev.utils.rex.file.registerShizukuPermission
import com.ruyomi.dev.utils.rex.file.registerStoragePermission
import com.ruyomi.dev.utils.rex.file.requestAllFilePermission
import com.ruyomi.dev.utils.rex.file.requestDocPermission
import com.ruyomi.dev.utils.rex.file.requestRootPermission
import com.ruyomi.dev.utils.rex.file.requestShizukuPermission
import com.ruyomi.dev.utils.rex.file.requestStoragePermission
import com.ruyomi.dev.utils.rex.file.useBug
import com.ruyomi.rexfile.demo.ui.theme.RexFileDemoTheme

class MainActivity : ComponentActivity() {

    private val allFilePermission = registerAllFilePermission(
        granted = {
            "授权成功（所有文件访问权限）".showToast(this)
        },
        denied = {
            "授权失败（所有文件访问权限）".showToast(this)
        }
    )

    private val docFilePermission = registerDocPermission(
        granted = {
            "授权成功（DocumentFile访问权限）".showToast(this)
        },
        denied = {
            "授权失败（DocumentFile访问权限）".showToast(this)
        }
    )

    private val storagePermission = registerStoragePermission(
        granted = {
            "授权成功（文件读写权限）".showToast(this)
        },
        denied = {
            "授权失败（文件读写权限）".showToast(this)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RexFileConfig.instance.init(this)
        if (!hasAllFilePermission()) {
            allFilePermission.requestAllFilePermission()
        }
        if (!hasStoragePermission()) {
            storagePermission.requestStoragePermission()
        }
        registerShizukuPermission(
            granted = {
                "授权成功（Shizuku）".showToast(this)
            },
            denied = {
                "授权失败（Shizuku）".showToast(this)
            }
        )

        setContent {
            RexFileDemoTheme {
                val context = LocalContext.current
                val list = remember {
                    mutableStateListOf<String>()
                }
                val scrollState = rememberScrollState()
                var path by remember {
                    mutableStateOf("/storage/emulated/0/")
                }
                var model by remember {
                    mutableStateOf(RexFileConfig.instance.fileModel)
                }

                LaunchedEffect(key1 = model) {
                    when (model) {
                        RexFileModel.FILE -> {
                            RexFileConfig.instance.init(context, model)
                            if (hasStoragePermission() && hasAllFilePermission()) {
                                list.clear()
                                list.addAll(file(path.useBug()).list())
                            }
                        }

                        RexFileModel.SHIZUKU -> {
                            RexFileConfig.instance.init(context, model)
                            if (hasShizukuPermission() && peekShizukuService()) {
                                list.clear()
                                list.addAll(file(path).list())
                            } else {
                                requestShizukuPermission(0)
                            }
                        }

                        RexFileModel.ROOT -> {
                            RexFileConfig.instance.init(context, model)
                            if (hasRootPermission()) {
                                list.clear()
                                list.addAll(file(path.useBug()).list())
                            } else {
                                requestRootPermission()
                            }
                        }

                        else -> {
                            if (!path.contains("Android/data".useBug())) {
                                "请在Android/data目录下使用DocumentFile".showToast(this@MainActivity)
                            } else {
                                RexFileConfig.instance.init(context, model)
                                if (path.hasDocPermission()) {
                                    list.clear()
                                    list.addAll(file(path.useBug()).list())
                                } else {
                                    docFilePermission.requestDocPermission(path)
                                }
                            }
                        }
                    }
                }
                LaunchedEffect(key1 = path) {
                    if (hasStoragePermission() && hasAllFilePermission()) {
                        list.clear()
                        list.addAll(file(path.useBug()).list())
                    }
                }

                BackHandler {
                    if (path != "/storage/emulated/0") {
                        path = file(path.useBug()).getParent()
                    } else {
                        finish()
                    }
                }

                // MainColumn
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            var expanded by remember {
                                mutableStateOf(false)
                            }
                            Text(
                                modifier = Modifier.clickable {
                                    expanded = true
                                },
                                text = RexFileConfig.instance.fileModel.name
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                RexFileModel.entries.forEach {
                                    DropdownMenuItem(
                                        text = { Text(text = it.name) },
                                        onClick = {
                                            model = it
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .scrollable(scrollState, Orientation.Vertical),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(list) {
                            Item(
                                text = file(it).name,
                                onClick = {
                                    if (file(it).isDirectory()) {
                                        path = it
                                    } else {
                                        "这是一个文件".showToast(this@MainActivity)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RexFileConfig.instance.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Item(
    text: String,
    onClick: () -> Unit = { }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(text = text)
        }
    }
}

fun String.showToast(activity: Activity) = Toast.makeText(activity, this, Toast.LENGTH_SHORT).show()

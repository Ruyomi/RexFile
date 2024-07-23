package com.ruyomi.rexfile.demo

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ruyomi.dev.utils.rexfile.file.RexFileConfig
import com.ruyomi.dev.utils.rexfile.file.RexFileModel
import com.ruyomi.dev.utils.rexfile.file.file
import com.ruyomi.dev.utils.rexfile.file.hasDocPermission
import com.ruyomi.dev.utils.rexfile.file.readString
import com.ruyomi.dev.utils.rexfile.file.registerAllFilePermission
import com.ruyomi.dev.utils.rexfile.file.registerDocPermission
import com.ruyomi.dev.utils.rexfile.file.registerStoragePermission
import com.ruyomi.dev.utils.rexfile.file.requestAllFilePermission
import com.ruyomi.dev.utils.rexfile.file.requestDocPermission
import com.ruyomi.dev.utils.rexfile.file.requestStoragePermission
import com.ruyomi.dev.utils.rexfile.file.useBug
import com.ruyomi.dev.utils.rexfile.file.write
import com.ruyomi.rexfile.demo.ui.theme.RexFileDemoTheme

class MainActivity : ComponentActivity() {

    val allFile = registerAllFilePermission(
        granted = {
            Toast.makeText(this, "好人", Toast.LENGTH_SHORT).show()
        },
        denied = {
            Toast.makeText(this, "Fuck好人", Toast.LENGTH_SHORT).show()
        }
    )

    val docFile = registerDocPermission(
        granted = {
            Toast.makeText(this, "好人", Toast.LENGTH_SHORT).show()
        },
        denied = {
            Toast.makeText(this, "Fuck好人", Toast.LENGTH_SHORT).show()
        }
    )

    val filePermission = registerStoragePermission(
        granted = {
            Toast.makeText(this, "好人", Toast.LENGTH_SHORT).show()
        },
        denied = {
            Toast.makeText(this, "Fuck好人", Toast.LENGTH_SHORT).show()
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RexFileConfig.instance.init(this, RexFileModel.DOCUMENT)
        filePermission.requestStoragePermission()
        allFile.requestAllFilePermission()

        if (!"/storage/emulated/0/Android/data/bin.mt.plus/".useBug().hasDocPermission()) {
            docFile.requestDocPermission("/storage/emulated/0/Android/data/bin.mt.plus/".useBug())
        } else {
            file("/storage/emulated/0/Android/data/bin.mt.plus/a/a/a/a".useBug()).apply {
                write("Hello").toString().showToast(this@MainActivity)
                readString().showToast(this@MainActivity)
            }
        }
        setContent {
            RexFileDemoTheme {
                val context = LocalContext.current
                Column {

                }
            }
        }
    }
}

fun String.showToast(activity: Activity) = Toast.makeText(activity, this, Toast.LENGTH_SHORT).show()

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RexFileDemoTheme {
        Greeting("Android")
    }
}
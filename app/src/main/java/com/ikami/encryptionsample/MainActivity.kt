package com.ikami.encryptionsample

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.material.snackbar.Snackbar
import com.ikami.encryptionsample.utils.Constants
import com.ikami.encryptionsample.utils.EncryptionUtil
import com.ikami.encryptionsample.utils.FileUtil
import com.ikami.encryptionsample.utils.ProgressDialog
import com.ikami.encryptionsample.workers.DecryptionWorker
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var pickVideoLauncher: ActivityResultLauncher<String>
    private lateinit var pickTextFileLauncher: ActivityResultLauncher<String>
    private lateinit var uploadWorkRequest: WorkRequest
    private val encryptionUtils = EncryptionUtil()
    private val masterKey = Constants.MASTER_KEY
    private var videoFileCount: Int = 1
    private var decryptedVideoFileCount: Int = 0
    var decryptionStateLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val progressDialog = ProgressDialog()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()

    }


    private fun initializeUI() {
        println("initializeUI called ======")

        // Setup video picker launcher
        pickVideoLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { videoUri ->
                videoUri?.let {

                    Log.e("Encode video file called ======", "TRUE")

                    contentResolver.openInputStream(it)
                        ?.let { it1 ->
                            encryptionUtils.encryptVideo(it1, masterKey, videoFileCount, this)
                            videoFileCount++

                        }


                }
            }
        // Setup file picker launcher
        pickTextFileLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri ->
                fileUri?.let {

                    Log.e("file picker URI path is ======", "${it.path}")
                    //  readFile(it)
                    contentResolver.openInputStream(it)
                        ?.let { it1 ->
                            encryptionUtils.decryptVideo(
                                it1,
                                masterKey,
                                decryptedVideoFileCount,
                                this
                            )
                            decryptedVideoFileCount++
                        }

                }
            }


        // Setup SELECT VIDEO button
        findViewById<Button>(R.id.button_select_video).setOnClickListener {
            askForPermissions()
            pickVideoLauncher.launch("video/*")

        }

        findViewById<Button>(R.id.button_decrypt_video).setOnClickListener {
            askForPermissions()
            pickTextFileLauncher.launch("text/plain")

        }

        findViewById<Button>(R.id.button_decrypt_multi_video).setOnClickListener {

            progressDialog.show(this)
            // Create a Work Request
            uploadWorkRequest = OneTimeWorkRequestBuilder<DecryptionWorker>().build()
            WorkManager.getInstance().enqueue(uploadWorkRequest)
            observeWorkManager()
            observeDecryptionState()

        }
        findViewById<Button>(R.id.button_lesson_view).setOnClickListener {

        }

    }

    private fun observeWorkManager() {
        WorkManager.getInstance()
            .getWorkInfoByIdLiveData(uploadWorkRequest.id).observe(this, Observer { workInfo ->
                decryptionStateLiveData.value =
                    if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                        workInfo.outputData.getBoolean("is_success", false)
                    } else {
                        false
                    }

            })
    }

    private fun observeDecryptionState() {
        val launcherLayout = findViewById<LinearLayoutCompat>(R.id.launcher_layout)
        decryptionStateLiveData.observe(this, Observer {
            if (it == true) {
                progressDialog.dismiss()
                Snackbar.make(
                    this, launcherLayout, "DECRYPTION IS SUCCESSFUL",
                    Snackbar.LENGTH_LONG
                ).show()

            } else {
                Log.e("observeDecryptionState is ======", "$it")
            }
        })
    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
                return
            }
            createDir()
        } else {
            createDir()
        }
    }

    private fun createDir() {
        val decryptionDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            FileUtil.DOCUMENT_DECRYPTION_DIRECTORY
        )

        val encryptionDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            FileUtil.DOCUMENT_ENCRYPTION_DIRECTORY
        )
        if (!decryptionDirectory.exists()) {
            decryptionDirectory.mkdirs()
        }
        if (!encryptionDirectory.exists()) {
            encryptionDirectory.mkdirs()
        }
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        // return File(uri.path).name

        var result = ""
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use { it ->
                if (it != null && it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return result

    }

}

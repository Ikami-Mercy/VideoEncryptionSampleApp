package com.ikami.encryptionsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ikami.encryptionsample.databinding.ActivityLessonViewBinding
import java.io.File
import java.io.IOException


class LessonViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonViewBinding
    private lateinit var adapter: LessonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLessonViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        getEncryptedVideos()
    }

    private fun getEncryptedVideos() {

        try {
            val videoFileDirectory = getDir("uLessonEncryptedVideos", Context.MODE_PRIVATE)
            val files = videoFileDirectory.listFiles()
            if (files.isNullOrEmpty()){
                Toast.makeText(this, "No encrypted file found", Toast.LENGTH_SHORT).show()
            } else {
                val lessons = mutableListOf<LessonUIModel>()

                for (i in files.indices){
                    Log.e("Files", "FileName:" + files[i].name)
                    Log.e("Files", "FilePath:" + files[i].path)
                    val fileName = files[i].name
                    val filePath = files[i].path
                    val lessonUIModel = LessonUIModel(fileName, filePath)
                    lessons.add(lessonUIModel)
                }

                adapter = LessonAdapter{ position, lessonItem ->
                    val intent = Intent(this, VideoPlayerActivity::class.java)

                    intent.putExtra(FILE_PATH, lessonItem.filePath)
                    startActivity(intent)
                }

                binding.container.lessonsRv.adapter = adapter
                adapter.submitList(lessons)
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, filePath: String, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                Log.e(this::class.java.simpleName, "file don't exists")
                it.outputStream().use { cache ->
                    Log.e(this::class.java.simpleName, "filename: $fileName")
                    context.assets.open(filePath).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            } else {
                Log.e(this::class.java.simpleName, "file exists")
            }
        }
}
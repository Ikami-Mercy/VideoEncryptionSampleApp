package com.ikami.encryptionsample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ikami.encryptionsample.databinding.ActivityLessonViewBinding

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
            val mainPathName = "EncryptedVideos"
            val file = assets.list(mainPathName)
            if (file == null) {
                Log.e(this::class.java.simpleName, "No file found")
            } else {
                Log.e(this::class.java.simpleName, "number of files: ${file.size}")
                if (file.isNotEmpty()){
                    val lessons = mutableListOf<LessonUIModel>()
                    file.forEach {
                        val filePath = "$mainPathName/$it"
                        Log.e(this::class.java.simpleName, "filename: $mainPathName/$it")
                        val lessonUIModel = LessonUIModel(it, filePath)
                        lessons.add(lessonUIModel)
                    }

                    adapter = LessonAdapter {
                        Toast.makeText(this, "item position: $it", Toast.LENGTH_SHORT).show()
                    }

                    binding.container.lessonsRv.adapter = adapter
                    adapter.submitList(lessons)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
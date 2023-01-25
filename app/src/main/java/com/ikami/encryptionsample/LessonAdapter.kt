package com.ikami.encryptionsample

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ikami.encryptionsample.databinding.VideoItemBinding

class LessonAdapter (private val onLessonItemClicked: (position: Int, lessonItem: LessonUIModel) -> Unit):
    ListAdapter<LessonUIModel, LessonAdapter.LessonViewHolder>(LessonDiffCallback()) {

    inner class LessonViewHolder (private val binding: VideoItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            clickListener: (position: Int, lessonItem: LessonUIModel) -> Unit,
            lessonName: String,
            itemPosition: Int,
            item: LessonUIModel
        ) {
            binding.lessonName.text = lessonName
            binding.root.setOnClickListener {
                clickListener.invoke(itemPosition, item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = VideoItemBinding.inflate(inflater, parent, false)
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val item = getItem(position)
        Log.e(this::class.java.simpleName, "lesson name: ${item.lessonName}")
        holder.bind(onLessonItemClicked, item.lessonName, position, item)
    }
}

class LessonDiffCallback: DiffUtil.ItemCallback<LessonUIModel>(){
    override fun areItemsTheSame(oldItem: LessonUIModel, newItem: LessonUIModel): Boolean {
        return oldItem.lessonName == newItem.lessonName
    }

    override fun areContentsTheSame(oldItem: LessonUIModel, newItem: LessonUIModel): Boolean {
        return oldItem == newItem
    }
}

data class LessonUIModel(
    val lessonName: String,
    val filePath: String
)
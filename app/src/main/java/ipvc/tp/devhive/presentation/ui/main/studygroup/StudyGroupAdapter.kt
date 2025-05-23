package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.StudyGroup
import ipvc.tp.devhive.presentation.util.DateFormatUtils

class StudyGroupAdapter :
    ListAdapter<StudyGroup, StudyGroupAdapter.StudyGroupViewHolder>(StudyGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_group, parent, false)
        return StudyGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudyGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudyGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivGroupImage: ImageView = itemView.findViewById(R.id.iv_group_image)
        private val tvGroupName: TextView = itemView.findViewById(R.id.tv_group_name)
        private val tvGroupDescription: TextView = itemView.findViewById(R.id.tv_group_description)
        private val tvMemberCount: TextView = itemView.findViewById(R.id.tv_member_count)
        private val tvCategories: TextView = itemView.findViewById(R.id.tv_categories)

        fun bind(studyGroup: StudyGroup) {
            tvGroupName.text = studyGroup.name
            tvGroupDescription.text = studyGroup.description
            tvMemberCount.text = itemView.context.getString(
                R.string.member_count,
                studyGroup.members.size
            )
            tvCategories.text = studyGroup.categories.joinToString(", ")

            // Carrega a imagem com Glide
            Glide.with(itemView.context)
                .load(studyGroup.imageUrl)
                .placeholder(R.drawable.placeholder_group)
                .error(R.drawable.placeholder_group)
                .centerCrop()
                .into(ivGroupImage)
        }
    }

    class StudyGroupDiffCallback : DiffUtil.ItemCallback<StudyGroup>() {
        override fun areItemsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
            return oldItem == newItem
        }
    }
}

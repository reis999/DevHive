package ipvc.tp.devhive.presentation.ui.main.studygroup.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ItemStudyGroupExploreBinding
import ipvc.tp.devhive.domain.model.StudyGroup

class ExploreGroupsAdapter(
    private val onGroupClickListener: (studyGroup: StudyGroup) -> Unit
) : ListAdapter<StudyGroup, ExploreGroupsAdapter.ExploreGroupViewHolder>(StudyGroupDiffCallback()) { // Reutilize o DiffCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreGroupViewHolder {
        val binding = ItemStudyGroupExploreBinding.inflate( // Use ViewBinding
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExploreGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExploreGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExploreGroupViewHolder(private val binding: ItemStudyGroupExploreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.btnJoinExplore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val studyGroup = getItem(position)
                    onGroupClickListener(studyGroup)
                }
            }
        }

        fun bind(studyGroup: StudyGroup) {
            binding.tvGroupName.text = studyGroup.name
            binding.tvGroupDescription.text = studyGroup.description
            binding.tvMemberCount.text = itemView.context.getString(
                R.string.member_count,
                studyGroup.members.size
            )
            binding.tvCategories.text = studyGroup.categories.joinToString(", ")

            Glide.with(itemView.context)
                .load(studyGroup.imageUrl)
                .placeholder(R.drawable.placeholder_group)
                .error(R.drawable.placeholder_group)
                .centerCrop()
                .into(binding.ivGroupImage)

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
package ipvc.tp.devhive.presentation.ui.main.studygroup.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ItemManageMemberBinding
import ipvc.tp.devhive.domain.model.User

data class MemberItem(
    val user: User,
    val isAdminInGroup: Boolean,
    val isCurrentUserViewing: Boolean
)

class MemberAdapter(
    private val onKickClickListener: (User) -> Unit,
    val currentUserId: String,
    val groupAdminIds: List<String>
) : ListAdapter<User, MemberAdapter.MemberViewHolder>(MemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemManageMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding, onKickClickListener, currentUserId, groupAdminIds)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MemberViewHolder(
        private val binding: ItemManageMemberBinding,
        private val onKickClickListener: (User) -> Unit,
        private val currentUserId: String,
        private val groupAdminIds: List<String>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvMemberName.text = user.name
            binding.tvMemberUsername.text = "@${user.username}"

            Glide.with(binding.root.context)
                .load(user.profileImageUrl.ifEmpty { R.drawable.placeholder_user })
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .circleCrop()
                .into(binding.ivMemberAvatar)

            val isUserAdminInThisGroup = groupAdminIds.contains(user.id)
            val isViewingUserAdminOfGroup = groupAdminIds.contains(currentUserId)


            if (isUserAdminInThisGroup) {
                binding.chipMemberRole.text = binding.root.context.getString(R.string.admin)
                binding.chipMemberRole.isVisible = true
                binding.btnKickMember.isVisible = false
            } else {
                binding.chipMemberRole.text = binding.root.context.getString(R.string.member)
                binding.chipMemberRole.isVisible = true
                binding.btnKickMember.isVisible = isViewingUserAdminOfGroup && user.id != currentUserId
            }

            if (binding.btnKickMember.isVisible) {
                binding.btnKickMember.setOnClickListener {
                    onKickClickListener(user)
                }
            } else {
                binding.btnKickMember.setOnClickListener(null)
            }
        }
    }

    class MemberDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
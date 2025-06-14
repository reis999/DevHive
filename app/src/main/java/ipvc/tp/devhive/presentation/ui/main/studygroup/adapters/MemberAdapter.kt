package ipvc.tp.devhive.presentation.ui.main.studygroup

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

data class MemberItem( // Para passar dados adicionais ao adapter se necessário
    val user: User,
    val isAdminInGroup: Boolean, // Se este user é admin NO GRUPO ATUAL
    val isCurrentUserViewing: Boolean // Se este user é o que está a ver a lista
)

class MemberAdapter(
    private val onKickClickListener: (User) -> Unit,
    val currentUserId: String, // ID do user que está a usar a app
    val groupAdminIds: List<String> // IDs dos admins do grupo
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
            binding.tvMemberUsername.text = "@${user.username}" // Assumindo que User tem username

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
                binding.btnKickMember.isVisible = false // Não se pode expulsar admins (regra de negócio)
            } else {
                binding.chipMemberRole.text = binding.root.context.getString(R.string.member)
                binding.chipMemberRole.isVisible = true // Ou false se não quiser mostrar para membros normais

                // Lógica de visibilidade do botão "Kick":
                // 1. O user que está a ver (currentUserId) TEM que ser admin do grupo.
                // 2. O user que está a ser listado (user.id) NÃO PODE ser ele mesmo (currentUserId).
                // 3. O user que está a ser listado (user.id) NÃO PODE ser um admin do grupo.
                binding.btnKickMember.isVisible = isViewingUserAdminOfGroup && user.id != currentUserId
            }

            if (binding.btnKickMember.isVisible) {
                binding.btnKickMember.setOnClickListener {
                    onKickClickListener(user)
                }
            } else {
                binding.btnKickMember.setOnClickListener(null) // Remover listener se não visível
            }
        }
    }

    class MemberDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem // Assumindo que User é data class
        }
    }
}
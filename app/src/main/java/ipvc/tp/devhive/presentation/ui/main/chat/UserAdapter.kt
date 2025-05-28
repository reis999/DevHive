package ipvc.tp.devhive.presentation.ui.main.chat

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
import ipvc.tp.devhive.domain.model.User

class UserAdapter(private val listener: OnUserClickListener) :
    ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    interface OnUserClickListener {
        fun onUserClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvUserBio: TextView = itemView.findViewById(R.id.tv_user_bio)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(getItem(position))
                }
            }
        }

        fun bind(user: User) {
            tvUserName.text = user.name
            tvUserBio.text = user.bio

            // Carrega a imagem do usuário
            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(ivUserAvatar)
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(ivUserAvatar)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}

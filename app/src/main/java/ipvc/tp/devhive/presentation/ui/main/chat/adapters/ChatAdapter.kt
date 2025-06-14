package ipvc.tp.devhive.presentation.ui.main.chat.adapters

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
import ipvc.tp.devhive.domain.model.Chat
import ipvc.tp.devhive.domain.model.User
import ipvc.tp.devhive.presentation.util.DateFormatUtils

class ChatAdapter(
    private val listener: OnChatClickListener,
    private val currentUser: User
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    interface OnChatClickListener {
        fun onChatClick(chat: Chat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view, currentUser, listener)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Modifique o construtor do ViewHolder também
    inner class ChatViewHolder(
        itemView: View,
        private val currentUser: User,
        private val clickListener: OnChatClickListener // Adicionado para o click
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivChatImage: ImageView = itemView.findViewById(R.id.iv_chat_image)
        private val tvChatName: TextView = itemView.findViewById(R.id.tv_chat_name)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvUnreadCount: TextView = itemView.findViewById(R.id.tv_unread_count)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onChatClick(getItem(position))
                }
            }
        }

        fun bind(chat: Chat) {

            if (chat.otherParticipantId == currentUser.id) {
                tvChatName.text = chat.participant1Name
            } else {
                tvChatName.text = chat.otherParticipantName
            }

            tvLastMessage.text = chat.lastMessagePreview
            tvTime.text = DateFormatUtils.getRelativeTimeSpan(chat.lastMessageAt)

            val unreadCount = chat.unreadCount
            if (unreadCount > 0) {
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.text = unreadCount.toString()
            } else {
                tvUnreadCount.visibility = View.GONE
            }

            if (chat.otherParticipantImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(chat.otherParticipantImageUrl)
                    .placeholder(R.drawable.placeholder_chat)
                    .error(R.drawable.placeholder_chat)
                    .circleCrop()
                    .into(ivChatImage)
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.placeholder_chat)
                    .circleCrop()
                    .into(ivChatImage)
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}

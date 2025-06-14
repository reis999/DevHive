package ipvc.tp.devhive.presentation.ui.main.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.Message
import ipvc.tp.devhive.presentation.util.DateFormatUtils

class MessageAdapter(private val currentUserId: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderUid == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false) // Crie este layout
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false) // Crie este layout
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tv_message_content_sent)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tv_message_time_sent)

        fun bind(message: Message) {
            tvMessageContent.text = message.content
            tvMessageTime.text = DateFormatUtils.getRelativeTimeSpan(message.createdAt) // Implemente formatTime
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tv_message_content_received)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tv_message_time_received)
        // private val ivSenderAvatar: ImageView = itemView.findViewById(R.id.iv_sender_avatar_received) // Opcional

        fun bind(message: Message) {
            tvMessageContent.text = message.content
            tvMessageTime.text = DateFormatUtils.getRelativeTimeSpan(message.createdAt)
            // Glide.with(itemView.context)... para avatar se tiver
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
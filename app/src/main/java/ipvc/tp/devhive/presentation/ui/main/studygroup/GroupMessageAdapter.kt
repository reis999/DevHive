package ipvc.tp.devhive.presentation.ui.main.studygroup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ipvc.tp.devhive.R
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.presentation.util.DateFormatUtils
import de.hdodenhof.circleimageview.CircleImageView

class GroupMessageAdapter(
    private val currentUserId: String
) : ListAdapter<GroupMessage, RecyclerView.ViewHolder>(GroupMessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

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
                .inflate(R.layout.item_group_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tv_message_content)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(message: GroupMessage) {
            tvMessageContent.text = message.content
            tvTimestamp.text = DateFormatUtils.getRelativeTimeSpan(message.createdAt)
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSenderAvatar: CircleImageView = itemView.findViewById(R.id.iv_sender_avatar)
        private val tvSenderName: TextView = itemView.findViewById(R.id.tv_sender_name)
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tv_message_content)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(message: GroupMessage) {
            tvSenderName.text = message.senderName
            tvMessageContent.text = message.content
            tvTimestamp.text = DateFormatUtils.getRelativeTimeSpan(message.createdAt)

            // Carrega a imagem do remetente
            if (message.senderImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(message.senderImageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(ivSenderAvatar)
            } else {
                ivSenderAvatar.setImageResource(R.drawable.profile_placeholder)
            }
        }
    }

    class GroupMessageDiffCallback : DiffUtil.ItemCallback<GroupMessage>() {
        override fun areItemsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
            return oldItem == newItem
        }
    }
}

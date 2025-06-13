package ipvc.tp.devhive.presentation.ui.main.studygroup.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import ipvc.tp.devhive.R
import ipvc.tp.devhive.databinding.ItemGroupMessageReceivedBinding
import ipvc.tp.devhive.databinding.ItemGroupMessageSentBinding
import ipvc.tp.devhive.databinding.ItemReceivedAttachmentMessageBinding
import ipvc.tp.devhive.databinding.ItemSentAttachmentMessageBinding
import ipvc.tp.devhive.domain.model.GroupMessage
import ipvc.tp.devhive.presentation.util.DateFormatUtils
import kotlin.math.log10
import kotlin.math.pow

class GroupMessageAdapter(
    private val currentUserId: String
) : ListAdapter<GroupMessage, RecyclerView.ViewHolder>(GroupMessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT_TEXT = 1
        private const val VIEW_TYPE_RECEIVED_TEXT = 2
        private const val VIEW_TYPE_SENT_ATTACHMENT = 3
        private const val VIEW_TYPE_RECEIVED_ATTACHMENT = 4
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        val isSentByCurrentUser = message.senderUid == currentUserId
        val hasAttachments = message.attachments.isNotEmpty()

        return when {
            isSentByCurrentUser && hasAttachments -> VIEW_TYPE_SENT_ATTACHMENT
            isSentByCurrentUser && !hasAttachments -> VIEW_TYPE_SENT_TEXT
            !isSentByCurrentUser && hasAttachments -> VIEW_TYPE_RECEIVED_ATTACHMENT
            !isSentByCurrentUser && !hasAttachments -> VIEW_TYPE_RECEIVED_TEXT
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT_TEXT -> {
                // Use ViewBinding para seus layouts de texto também, se possível
                // Assumindo que você tem ItemGroupMessageSentBinding gerado
                val binding = ItemGroupMessageSentBinding.inflate(inflater, parent, false)
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED_TEXT -> {
                // Assumindo que você tem ItemGroupMessageReceivedBinding gerado
                val binding = ItemGroupMessageReceivedBinding.inflate(inflater, parent, false)
                ReceivedMessageViewHolder(binding)
            }
            VIEW_TYPE_SENT_ATTACHMENT -> {
                val binding = ItemSentAttachmentMessageBinding.inflate(inflater, parent, false)
                AttachmentMessageSentViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED_ATTACHMENT -> {
                val binding = ItemReceivedAttachmentMessageBinding.inflate(inflater, parent, false)
                AttachmentMessageReceivedViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is AttachmentMessageSentViewHolder -> holder.bind(message)
            is AttachmentMessageReceivedViewHolder -> holder.bind(message)
        }
    }

    // --- ViewHolder para mensagens de texto ENVIADAS ---
    inner class SentMessageViewHolder(private val binding: ItemGroupMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            binding.tvMessageContentSent.text = message.content
            binding.tvTimestampSent.text = DateFormatUtils.getRelativeTimeSpan(message.createdAt)
        }
    }

    // --- ViewHolder para mensagens de texto RECEBIDAS ---
    inner class ReceivedMessageViewHolder(private val binding: ItemGroupMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            binding.tvReceiverName.text = message.senderName
            binding.tvMessageContent.text = message.content
            binding.tvTimestampReceived.text = DateFormatUtils.getRelativeTimeSpan(message.createdAt) // Verifique o ID

            if (message.senderImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(message.senderImageUrl)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.ivSenderAvatar)
            } else {
                binding.ivSenderAvatar.setImageResource(R.drawable.placeholder_user)
            }
        }
    }

    // --- ViewHolder para mensagens com anexo ENVIADAS ---
    inner class AttachmentMessageSentViewHolder(
        private val binding: ItemSentAttachmentMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            if (message.content.isNotBlank()) {
                binding.tvMessageContentSentAttachment.text = message.content
                binding.tvMessageContentSentAttachment.visibility = View.VISIBLE
                (binding.attachmentDetailsContainerSent.layoutParams as LinearLayout.LayoutParams).topMargin =
                    itemView.context.resources.getDimensionPixelSize(R.dimen.spacing_small)
            } else {
                binding.tvMessageContentSentAttachment.visibility = View.GONE
                (binding.attachmentDetailsContainerSent.layoutParams as LinearLayout.LayoutParams).topMargin = 0
            }
            binding.tvTimestampSentAttachment.text = formatTimestamp(message.createdAt)

            message.attachments.firstOrNull()?.let { attachment ->
                binding.tvAttachmentFileNameSent.text = attachment.name
                binding.tvAttachmentFileSizeSent.text = formatFileSize(attachment.size)

                binding.ivAttachmentIconSent.visibility = View.VISIBLE
                binding.ivAttachmentIconSent.setImageResource(getIconForFileType(attachment.fileExtension))

                binding.btnDownloadAttachmentSent.setOnClickListener {
                    onDownloadClickListener?.invoke(attachment.url, attachment.name, attachment.type)
                }
                binding.attachmentDetailsContainerSent.visibility = View.VISIBLE
            } ?: run {
                binding.attachmentDetailsContainerSent.visibility = View.GONE
            }
        }
    }

    // --- ViewHolder para mensagens com anexo RECEBIDAS ---
    inner class AttachmentMessageReceivedViewHolder(
        private val binding: ItemReceivedAttachmentMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: GroupMessage) {
            binding.tvSenderNameReceivedAttachment.text = message.senderName
            Glide.with(binding.ivSenderProfileReceivedAttachment.context)
                .load(message.senderImageUrl)
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .into(binding.ivSenderProfileReceivedAttachment)

            if (message.content.isNotBlank()) {
                binding.tvMessageContentReceivedAttachment.text = message.content
                binding.tvMessageContentReceivedAttachment.visibility = View.VISIBLE
                (binding.attachmentDetailsContainerReceived.layoutParams as LinearLayout.LayoutParams).topMargin =
                    itemView.context.resources.getDimensionPixelSize(R.dimen.spacing_small)
            } else {
                binding.tvMessageContentReceivedAttachment.visibility = View.GONE
                (binding.attachmentDetailsContainerReceived.layoutParams as LinearLayout.LayoutParams).topMargin = 0
            }
            binding.tvTimestampReceivedAttachment.text = formatTimestamp(message.createdAt)

            message.attachments.firstOrNull()?.let { attachment ->
                binding.tvAttachmentFileNameReceived.text = attachment.name
                binding.tvAttachmentFileSizeReceived.text = formatFileSize(attachment.size) // No seu GroupMessage.Attachment, o campo é 'size' ou 'sizeBytes'? Ajuste aqui.

                binding.ivAttachmentIconReceived.visibility = View.VISIBLE
                binding.ivAttachmentIconReceived.setImageResource(getIconForFileType(attachment.fileExtension))

                binding.btnDownloadAttachmentReceived.setOnClickListener {
                    onDownloadClickListener?.invoke(attachment.url, attachment.name, attachment.type)
                }
                binding.attachmentDetailsContainerReceived.visibility = View.VISIBLE
            } ?: run {
                binding.attachmentDetailsContainerReceived.visibility = View.GONE
            }
        }
    }

    private var onDownloadClickListener: ((url: String, fileName: String, mimeType: String) -> Unit)? = null
    fun setOnDownloadClickListener(listener: (url: String, fileName: String, mimeType: String) -> Unit) {
        onDownloadClickListener = listener
    }

    @SuppressLint("DefaultLocale")
    private fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = if (sizeBytes < 1024) 0 else (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt()
        val validDigitGroups = digitGroups.coerceIn(0, units.size - 1)
        return String.format("%.1f %s", sizeBytes / 1024.0.pow(validDigitGroups.toDouble()), units[validDigitGroups])
    }

    private fun getIconForFileType(extension: String?): Int {
        return when (extension?.lowercase()) {
            "pdf" -> R.drawable.ic_file_pdf
            "doc", "docx" -> R.drawable.ic_file_word
            "xls", "xlsx" -> R.drawable.ic_file_excel
            "ppt", "pptx" -> R.drawable.ic_file_powerpoint
            "zip", "rar" -> R.drawable.ic_file_archive
            "txt" -> R.drawable.ic_file_text
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> R.drawable.ic_file_image // Crie R.drawable.ic_file_image
            else -> R.drawable.ic_file_generic
        }
    }

    private fun formatTimestamp(timestamp: Timestamp): String {
        return DateFormatUtils.getRelativeTimeSpan(timestamp)
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

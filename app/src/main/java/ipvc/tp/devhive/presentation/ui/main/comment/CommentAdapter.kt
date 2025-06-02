package ipvc.tp.devhive.presentation.ui.main.comment

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
import ipvc.tp.devhive.domain.model.Comment
import ipvc.tp.devhive.presentation.util.DateFormatUtils

class CommentAdapter(
    private val listener: OnCommentClickListener
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    interface OnCommentClickListener {
        fun onCommentLikeClick(comment: Comment, position: Int)
        fun onUserClick(userId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvCommentDate: TextView = itemView.findViewById(R.id.tv_comment_date)
        private val tvCommentContent: TextView = itemView.findViewById(R.id.tv_comment_content)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        private val ivLike: ImageView = itemView.findViewById(R.id.iv_like)

        fun bind(comment: Comment, position: Int) {
            tvUserName.text = comment.userName
            tvCommentDate.text = DateFormatUtils.formatFullDate(comment.createdAt.toDate())
            tvCommentContent.text = comment.content
            tvLikeCount.text = comment.likes.toString()

            // Carrega a imagem do usuário
            if (comment.userImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(comment.userImageUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(ivUserAvatar)
            } else {
                ivUserAvatar.setImageResource(R.drawable.profile_placeholder)
            }

            // Configura o ícone de like
            val likeIcon = if (comment.liked) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_outline
            }
            ivLike.setImageResource(likeIcon)

            // Configura os cliques
            ivLike.setOnClickListener {
                listener.onCommentLikeClick(comment, position)
            }

            tvLikeCount.setOnClickListener {
                listener.onCommentLikeClick(comment, position)
            }

            ivUserAvatar.setOnClickListener {
                listener.onUserClick(comment.userId)
            }

            tvUserName.setOnClickListener {
                listener.onUserClick(comment.userId)
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}


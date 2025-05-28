package ipvc.tp.devhive.presentation.ui.main.material

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
import ipvc.tp.devhive.domain.model.Material
import ipvc.tp.devhive.presentation.util.DateFormatUtils

class MaterialAdapter(private val listener: OnMaterialClickListener) :
    ListAdapter<Material, MaterialAdapter.MaterialViewHolder>(MaterialDiffCallback()) {

    interface OnMaterialClickListener {
        fun onMaterialClick(material: Material)
        fun onBookmarkClick(material: Material, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_material, parent, false)
        return MaterialViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvSubject: TextView = itemView.findViewById(R.id.tv_subject)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val ivBookmark: ImageView = itemView.findViewById(R.id.iv_bookmark)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMaterialClick(getItem(position))
                }
            }

            ivBookmark.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBookmarkClick(getItem(position), position)
                }
            }
        }

        fun bind(material: Material) {
            tvTitle.text = material.title
            tvDescription.text = material.description
            tvSubject.text = material.subject
            tvDate.text = DateFormatUtils.getRelativeTimeSpan(material.createdAt)

            // Carrega a imagem com Glide
            Glide.with(itemView.context)
                .load(material.thumbnailUrl)
                .placeholder(R.drawable.placeholder_thumbnail)
                .error(R.drawable.placeholder_thumbnail)
                .centerCrop()
                .into(ivThumbnail)

            // Atualiza o ícone de favorito
            val bookmarkIcon = if (material.bookmarked) {
                R.drawable.ic_bookmark
            } else {
                R.drawable.ic_bookmark_border
            }
            ivBookmark.setImageResource(bookmarkIcon)
        }
    }

    class MaterialDiffCallback : DiffUtil.ItemCallback<Material>() {
        override fun areItemsTheSame(oldItem: Material, newItem: Material): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Material, newItem: Material): Boolean {
            return oldItem == newItem
        }
    }
}

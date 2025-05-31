package ipvc.tp.devhive.presentation.ui.intro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ipvc.tp.devhive.R

class IntroSlideAdapter(private val introSlides: List<IntroSlide>) :
    RecyclerView.Adapter<IntroSlideAdapter.IntroSlideViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroSlideViewHolder {
        return IntroSlideViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.fragment_intro_slide,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: IntroSlideViewHolder, position: Int) {
        holder.bind(introSlides[position])
    }

    override fun getItemCount(): Int {
        return introSlides.size
    }

    inner class IntroSlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageSlide = view.findViewById<ImageView>(R.id.iv_slide_image)
        private val textTitle = view.findViewById<TextView>(R.id.tv_slide_title)
        private val textSubtitle = view.findViewById<TextView>(R.id.tv_subtitle)
        private val textDescription = view.findViewById<TextView>(R.id.tv_slide_description)

        fun bind(introSlide: IntroSlide) {
            imageSlide.setImageResource(introSlide.imageResId)
            textTitle.setText(introSlide.titleResId)

            if (introSlide.subtitleResId != null) {
                textSubtitle.setText(introSlide.subtitleResId)
                textSubtitle.visibility = View.VISIBLE
            } else {
                textSubtitle.visibility = View.GONE
            }

            textDescription.setText(introSlide.descriptionResId)
        }
    }
}

data class IntroSlide(
    val imageResId: Int,
    val titleResId: Int,
    val subtitleResId: Int? = null,
    val descriptionResId: Int
)
package ipvc.tp.devhive.presentation.ui.intro

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton

class IntroActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var layoutIndicators: LinearLayout
    private lateinit var btnSkip: MaterialButton
    private lateinit var btnNext: MaterialButton

    private val introSlideAdapter = IntroSlideAdapter(
        listOf(
            IntroSlide(
                R.drawable.ic_slide1,
                R.string.intro_title_1,
                R.string.intro_subtitle_1,
                R.string.intro_description_1
            ),
            IntroSlide(
                R.drawable.ic_slide2,
                R.string.intro_title_2,
                descriptionResId = R.string.intro_description_2
            ),
            IntroSlide(
                R.drawable.ic_slide3,
                R.string.intro_title_3,
                descriptionResId = R.string.intro_description_3
            ),
            IntroSlide(
                R.drawable.ic_slide4,
                R.string.intro_title_4,
                R.string.intro_subtitle_4,
                R.string.intro_description_4
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        initViews()
        setupViewPager()
        setupIndicators()
        setupButtons()
        setCurrentIndicator(0)
    }

    private fun initViews() {
        viewPager = findViewById(R.id.view_pager)
        layoutIndicators = findViewById(R.id.layout_indicators)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)
    }

    private fun setupViewPager() {
        viewPager.adapter = introSlideAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                updateButtonText(position)
            }
        })
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(introSlideAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.page_indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }
    }

    private fun setupButtons() {
        btnSkip.setOnClickListener {
            navigateToLogin()
        }

        btnNext.setOnClickListener {
            if (viewPager.currentItem == introSlideAdapter.itemCount - 1) {
                navigateToLogin()
            } else {
                viewPager.currentItem += 1
            }
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.page_indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.page_indicator_inactive
                    )
                )
            }
        }
    }

    private fun updateButtonText(position: Int) {
        if (position == introSlideAdapter.itemCount - 1) {
            btnNext.text = getString(R.string.finish) // ou R.string.get_started
        } else {
            btnNext.text = getString(R.string.next)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
package ipvc.tp.devhive.presentation.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
                R.drawable.intro_slide1,
                R.string.intro_title_1,
                R.string.intro_description_1
            ),
            IntroSlide(
                R.drawable.intro_slide2,
                R.string.intro_title_2,
                R.string.intro_description_2
            ),
            IntroSlide(
                R.drawable.intro_slide3,
                R.string.intro_title_3,
                R.string.intro_description_3
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Inicializa as views
        viewPager = findViewById(R.id.view_pager)
        layoutIndicators = findViewById(R.id.layout_indicators)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)

        // Configura o ViewPager
        viewPager.adapter = introSlideAdapter

        // Configura os indicadores
        setupIndicators()
        setCurrentIndicator(0)

        // Configura o listener do ViewPager
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                // Atualiza o texto do botão "Próximo" na última página
                if (position == introSlideAdapter.itemCount - 1) {
                    btnNext.text = getString(R.string.get_started)
                } else {
                    btnNext.text = getString(R.string.next)
                }
            }
        })

        // Configura os botões
        btnSkip.setOnClickListener {
            navigateToLogin()
        }

        btnNext.setOnClickListener {
            if (viewPager.currentItem == introSlideAdapter.itemCount - 1) {
                // Última página, navega para o login
                navigateToLogin()
            } else {
                // Avança para a próxima página
                viewPager.currentItem += 1
            }
        }
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
                    R.drawable.indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
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
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

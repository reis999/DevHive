package ipvc.tp.devhive.presentation.ui.main.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import ipvc.tp.devhive.R
import ipvc.tp.devhive.presentation.util.LocaleHelper

@AndroidEntryPoint
class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var radioGroupLanguage: RadioGroup
    private lateinit var btnApplyLanguage: MaterialButton

    private var selectedLanguageCode: String? = null
    private var initialLanguageCode: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        toolbar = findViewById(R.id.toolbar_language_selection)
        radioGroupLanguage = findViewById(R.id.radio_group_language_selection)
        btnApplyLanguage = findViewById(R.id.btn_apply_language_selection)

        setupToolbar()
        populateLanguageOptions()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun populateLanguageOptions() {
        initialLanguageCode = LocaleHelper.getSelectedLanguageCode(this)
        selectedLanguageCode = initialLanguageCode

        LocaleHelper.supportedLanguages.forEach { (code, name) ->
            val radioButton = createStyledRadioButton(code, name)
            radioGroupLanguage.addView(radioButton)

            if (code == selectedLanguageCode) {
                radioButton.isChecked = true
            }
        }

        updateApplyButtonState()
    }

    private fun createStyledRadioButton(languageCode: String, displayName: String): RadioButton {
        return RadioButton(this).apply {
            id = View.generateViewId()
            text = displayName
            tag = languageCode
            textSize = 16f

            val paddingPx = (16 * resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

            try {
                background = ContextCompat.getDrawable(this@LanguageSelectionActivity, R.drawable.language_radio_background)
            } catch (e: Exception) {
                setBackgroundResource(android.R.drawable.btn_default)
            }

            val marginPx = (8 * resources.displayMetrics.density).toInt()
            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(marginPx, marginPx/2, marginPx, marginPx/2)
            }

            // Altura mínima
            minHeight = (64 * resources.displayMetrics.density).toInt()
            gravity = Gravity.CENTER_VERTICAL

            // Efeito de toque
            isClickable = true
            isFocusable = true

            try {
                val radioDrawable = ContextCompat.getDrawable(this@LanguageSelectionActivity, R.drawable.radio_selector)
                setCompoundDrawablesWithIntrinsicBounds(null, null, radioDrawable, null)
                compoundDrawablePadding = (16 * resources.displayMetrics.density).toInt()
                buttonDrawable = null
            } catch (_: Exception) {
            }
        }
    }

    private fun updateApplyButtonState() {
        val hasChanged = selectedLanguageCode != initialLanguageCode

        btnApplyLanguage.apply {
            isEnabled = hasChanged
            alpha = if (hasChanged) 1.0f else 0.6f


            text = if (hasChanged) {
                getString(R.string.apply_changes)
            } else {
                getString(R.string.apply)
            }
        }
    }

    private fun setupListeners() {
        radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = radioGroupLanguage.findViewById<RadioButton>(checkedId)
            selectedLanguageCode = checkedRadioButton?.tag as? String
            updateApplyButtonState()
        }

        btnApplyLanguage.setOnClickListener {
            selectedLanguageCode?.let { code ->
                val currentLangCode = LocaleHelper.getSelectedLanguageCode(this)
                if (code != currentLangCode) {
                    showLanguageChangeConfirmation(code)
                } else {
                    finish()
                }
            }
        }
    }

    private fun showLanguageChangeConfirmation(languageCode: String) {
        val languageName = LocaleHelper.getLanguageNameByCode(languageCode)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_language_change))
            .setMessage(getString(R.string.confirm_language_change_message, languageName))
            .setPositiveButton(getString(R.string.apply)) { _, _ ->
                applyLanguageChange(languageCode)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun applyLanguageChange(languageCode: String) {
        btnApplyLanguage.apply {
            isEnabled = false
            text = getString(R.string.applying)
        }

        LocaleHelper.setAppLocale(this, languageCode)
        setResult(Activity.RESULT_OK)
        Toast.makeText(this, getString(R.string.language_changed_message), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
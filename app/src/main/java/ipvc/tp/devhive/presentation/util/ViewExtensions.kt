package ipvc.tp.devhive.presentation.util

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

/**
 * Extensões para facilitar o trabalho com Views
 */

/**
 * Mostra um Snackbar com a mensagem fornecida
 */
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

/**
 * Mostra um Toast com a mensagem fornecida
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Define a visibilidade da View como VISIBLE
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * Define a visibilidade da View como GONE
 */
fun View.gone() {
    visibility = View.GONE
}


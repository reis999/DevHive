package ipvc.tp.devhive.presentation.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
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
 * Esconde o teclado virtual
 */
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Define a visibilidade da View como VISIBLE
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * Define a visibilidade da View como INVISIBLE
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Define a visibilidade da View como GONE
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Alterna a visibilidade da View entre VISIBLE e GONE
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

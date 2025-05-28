package ipvc.tp.devhive.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utilitários para manipulação de datas
 */
object DateUtils {

    private val fullDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val timeOnlyFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

    /**
     * Formata uma data para exibição completa
     */
    fun formatFullDate(date: Date): String {
        return fullDateFormat.format(date)
    }

    /**
     * Formata uma data para exibição apenas da data
     */
    fun formatDateOnly(date: Date): String {
        return dateOnlyFormat.format(date)
    }

    /**
     * Formata uma data para exibição apenas da hora
     */
    fun formatTimeOnly(date: Date): String {
        return timeOnlyFormat.format(date)
    }

    /**
     * Retorna uma string relativa para a data (ex: "há 5 minutos", "ontem", etc.)
     */
    fun getRelativeTimeSpan(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time

        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            diffInMinutes < 1 -> "agora mesmo"
            diffInMinutes < 60 -> "há $diffInMinutes ${if (diffInMinutes == 1L) "minuto" else "minutos"}"
            diffInHours < 24 -> "há $diffInHours ${if (diffInHours == 1L) "hora" else "horas"}"
            diffInDays < 7 -> "há $diffInDays ${if (diffInDays == 1L) "dia" else "dias"}"
            else -> formatDateOnly(date)
        }
    }
}

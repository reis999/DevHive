package ipvc.tp.devhive.presentation.util

/**
 * Classe utilizada para eventos que devem ser consumidos apenas uma vez
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    /**
     * Retorna o conteúdo e impede que seja usado novamente
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Retorna o conteúdo mesmo que já tenha sido manipulado
     */
    fun peekContent(): T = content
}

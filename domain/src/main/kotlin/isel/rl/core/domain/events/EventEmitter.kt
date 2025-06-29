import isel.rl.core.domain.events.Event

/**
 * EventEmitter interface that represents an event emitter.
 */
interface EventEmitter {
    /**
     * Emits an event.
     *
     * @param event the event to emit
     */
    fun emit(event: Event)

    fun complete()

    /**
     * Adds a callback to be executed when the emitter completes.
     *
     * @param callback the callback
     */
    fun onCompletion(callback: () -> Unit)

    /**
     * Adds a callback to be executed when the emitter encounters an error.
     *
     * @param callback the callback
     */
    fun onError(callback: (Throwable) -> Unit)

    /**
     * Adds a callback to be executed when the emitter times out.
     *
     * @param callback the callback
     */
    fun onTimeout(callback: () -> Unit)
}

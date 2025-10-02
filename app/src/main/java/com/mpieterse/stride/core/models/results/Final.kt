package com.mpieterse.stride.core.models.results

typealias GenericError = FinalError

sealed interface Final<out D, out E : GenericError> {
    data class Success<out D, out E : GenericError>(val product: D) : Final<D, E>
    data class Failure<out D, out E : GenericError>(val problem: E) : Final<D, E>


// --- Extensions


    /**
     * Convenience method called when the operation is a [Final.Success].
     */
    fun onSuccess(execute: (D) -> Unit): Final<D, E> {
        if (this is Success) execute(product)
        return this
    }


    /**
     * Convenience method called when the operation is a [Final.Failure].
     */
    fun onFailure(execute: (E) -> Unit): Final<D, E> {
        if (this is Failure) execute(problem)
        return this
    }
}
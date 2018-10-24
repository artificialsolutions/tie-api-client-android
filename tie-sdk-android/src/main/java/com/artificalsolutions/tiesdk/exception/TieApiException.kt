package com.artificalsolutions.tiesdk.exception

import com.artificalsolutions.tiesdk.model.TieErrorResponse

/**
 * Exception thrown when the engine responds with an error message.
 */
public class TieApiException: Exception {
    /**
     * Engine defined information about the error.
     */
    public val error: TieErrorResponse?;

    internal constructor(error: TieErrorResponse?) : super(error?.message) {
        this.error = error
    }
}
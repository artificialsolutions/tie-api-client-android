package com.artificalsolutions.tiesdk.model

/**
 * Information about an error supplied by the engine.
 */
data class TieErrorResponse(val status: String?, val message: String, val sessionId: String?, val input: TieInput?)
package com.artificalsolutions.tiesdk.model

/**
 * Response to a successful close session call.
 */
data class TieCloseSessionResponse(val response: TieResponseData)

/**
 * Contents of the response to a successful close session call.
 */
data class TieResponseData(val status: String, val message: String)
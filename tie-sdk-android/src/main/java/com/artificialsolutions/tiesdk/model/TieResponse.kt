package com.artificialsolutions.tiesdk.model

/**
 * Successful response from the engine.
 */
data class TieResponse(val status: String, val input: TieInput, val output: TieOutput, val sessionId: String?)

/**
 * The user-supplied input as read by the engine.
 */
data class TieInput(val text: String, val parameters: HashMap<String, String>)

/**
 * The response of the engine.
 */
data class TieOutput(val text: String, val emotion: String, val link: String, val parameters: HashMap<String, String>)
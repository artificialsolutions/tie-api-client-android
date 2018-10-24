package com.artificalsolutions.tiesdk.exception

import java.lang.Exception

/**
 * Exception thrown when attempting to use TieApiService without initializing it first.
 */
public class NotInitializedException(message: String?) : Exception(message) {}
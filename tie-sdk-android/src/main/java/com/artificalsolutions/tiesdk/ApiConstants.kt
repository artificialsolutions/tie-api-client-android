package com.artificalsolutions.tiesdk

internal class ApiConstants {
    companion object {
        internal val SHARED_PREFERENCES_FILE: String = "teneo_api_client"
        internal val PREFERENCES_KEY_SESSION_ID: String = "session_cookie"
        internal val API_VIEW_NAME: String = "tieapi"
        internal val API_VIEW_TYPE: String = "tieapi"
        internal val SESSION_COOKIE_PREFIX = "JSESSIONID="
        internal val CLOSE_SESSION_COMMAMND = "endsession"
    }
}
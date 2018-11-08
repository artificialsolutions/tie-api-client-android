package com.artificialsolutions.tiesdk

import android.content.Context
import android.content.SharedPreferences
import com.artificialsolutions.tiesdk.exception.NotInitializedException
import com.artificialsolutions.tiesdk.model.TieCloseSessionResponse
import com.artificialsolutions.tiesdk.model.TieResponse
import io.reactivex.Single
import io.reactivex.functions.Consumer

/**
 * Singleton client for interacting with the Teneo engine.
 *
 * Usage:
 * <li>
 * <ul>initialize with the TieApiClient.sharedInstace.setup(context, baseUrl, endpoint) call.</ul>
 * <ul>Call TieApiClient.sharedInstace.sendIput(userInput) to interact with the engine.</ul>
 * <ul>End session by calling TieApiClient.sharedInstace.close()</ul>
 * </li>
 */
public class TieApiService {
    private var sessionId: String = ""

    companion object {
        /**
         * The singleton instance of TieApiService. Call setup before usage.
         */
        @JvmStatic val sharedInstance by lazy {
            TieApiService()
        }
    }

    internal constructor() {}

    private val service by lazy {
        TieApi.create(baseUrl!!)
    }

    private var baseUrl: String? = null
    private var endpoint: String? = null
    private var context: Context? = null

    private var sharedPreferences: SharedPreferences? = null

    /**
     * Initializes the api client.
     *
     * You need to call this function once before calling any other api methods.
     *
     * @param context context for getting shared preferences.
     * @param baseUrl base url of the Teneo engine.
     * @param endpoint used endpoint of the Teneo engine.
     */
    public fun setup(context: Context, baseUrl: String, endpoint: String) {
        this.context = context
        this.baseUrl = baseUrl
        this.endpoint = endpoint
        this.sharedPreferences = context.getSharedPreferences(ApiConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        this.sessionId = sharedPreferences?.getString(ApiConstants.PREFERENCES_KEY_SESSION_ID, "") ?: ""
    }

    /**
     * Sends input to the engine. You need to call setup once before calling this.
     *
     * @param userInput the text that is sent to the engine
     * @param parameters key-value pairs of implementation specific string parameters. Reserved keys are text, viewname, and viewtype.
     */
    public fun sendInput(userInput: String, parameters: HashMap<String, String>? = null): Single<TieResponse> {
        verifySetup()

        var sessionCookie: String? = null
        if (!sessionId.isNullOrBlank()) {
            sessionCookie = "${ApiConstants.SESSION_COOKIE_PREFIX}$sessionId"
        }

        return service.sendInput(url = endpoint!!, userInput = userInput, parameters = parameters ?: HashMap(), viewName = ApiConstants.API_VIEW_NAME, viewType = ApiConstants.API_VIEW_TYPE, sessionId = sessionCookie)
                .singleOrError()
                .doOnSuccess(Consumer { result ->  persistSession(result.sessionId!!)  })
    }

    /**
     * Closes the session.
     *
     * Any subsequent calls to sendInput will start a new session.
     */
    public fun close(): Single<TieCloseSessionResponse> {
        val url = "$baseUrl$endpoint${ApiConstants.CLOSE_SESSION_COMMAMND}"
        val session = "${ApiConstants.SESSION_COOKIE_PREFIX}$sessionId"
        clearSession()
        return service.close(url = url, sessionId = session).singleOrError()
    }

    private fun persistSession(newSessionId: String) {
        if (newSessionId != this.sessionId) {
            this.sessionId = newSessionId

            if (sharedPreferences != null) {
                val editor = sharedPreferences!!.edit()
                editor.putString(ApiConstants.PREFERENCES_KEY_SESSION_ID, sessionId)
                editor.apply()
            }
        }
    }

    private fun clearSession() {
        sessionId = ""
        if (sharedPreferences != null) {
            val editor = sharedPreferences!!.edit()
            editor.remove(ApiConstants.PREFERENCES_KEY_SESSION_ID)
            editor.apply()
        }
    }

    private fun verifySetup() {
        if (baseUrl.isNullOrEmpty()) {
            throw NotInitializedException("You need to call setup before any API functions.")
        }
    }
}

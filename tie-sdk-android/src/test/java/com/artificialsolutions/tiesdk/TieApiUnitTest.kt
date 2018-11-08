package com.artificialsolutions.tiesdk

import android.content.Context
import org.junit.Before
import org.junit.Test
import android.content.SharedPreferences
import com.artificialsolutions.tiesdk.exception.TieApiException
import com.artificialsolutions.tiesdk.model.*
import com.google.gson.Gson
import com.google.gson.JsonParseException
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert
import org.mockito.Mockito

class TieApiUnitTest {
    private val emptyOutputModelResponse: TieResponse = TieResponse(
            "",
            TieInput("", HashMap<String, String>()),
            TieOutput("", "", "", HashMap<String, String>()),
            "")
    private val emptyOutputModelResponseJson = Gson().toJson(emptyOutputModelResponse)

    private val defaultCloseModelResponse: TieCloseSessionResponse = TieCloseSessionResponse(
            response = TieResponseData(status = "1", message = "logout")
    )
    private val defaultCloseModelResponseJson = Gson().toJson(defaultCloseModelResponse)

    private var service: TieApiService? = null
    private var server: MockWebServer? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val context = mock(Context::class.java)

        val sharedPreferences = mock(SharedPreferences::class.java)
        `when`(context!!.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)

        val editor = Mockito.mock(SharedPreferences.Editor::class.java)
        `when`(sharedPreferences.edit()).thenReturn(editor)

        server = MockWebServer()
        server!!.start()

        service = setupService(context, server!!)
    }

    @After
    fun teardown() {
        server?.shutdown()
    }

    private fun setupService(context: Context, server: MockWebServer) : TieApiService {
        val service = TieApiService()
        service.setup(context = context, baseUrl = server.url("/").toString(), endpoint = "")

        return service
    }

    @Test
    fun outputTest() {
        server!!.enqueue(MockResponse().setBody(emptyOutputModelResponseJson))

        service!!.sendInput("my name is test").subscribe()

        val request1 = server!!.takeRequest()
        val expectedBody = "userinput=my%20name%20is%20test&viewname=tieapi&viewtype=tieapi"

        Assert.assertEquals(expectedBody, request1.body.readUtf8().trim())
    }

    @Test
    fun inputParsingTest () {
        val sessionId = "testSessionId"
        val input = "my name is test1"

        val output = """{"status": 0,"input": {"text": "my name is test1","parameters": {}},"output": {"text": "test1 is a nice name","emotion": "","link": "","parameters": {}},"sessionId": "testSessionId"}"""
        server!!.enqueue(MockResponse().setBody(output))
        service!!.sendInput(input).subscribe {result ->
            Assert.assertEquals("my name is test1", result.input.text)
            Assert.assertEquals("test1 is a nice name", result.output.text)
            Assert.assertEquals(sessionId, result.sessionId)
        }
    }

    @Test
    fun inputParametersTest () {
        val input = "my name is test1"

        val output = """{"status": 0,"input": {"text": "my name is test1","parameters": {"param1":"val1","param2":"val2"}},"output": {"text": "test1 is a nice name","emotion": "","link": "","parameters": {"param1":"val1","param2":"val2"}},"sessionId": "testSessionId"}"""
        server!!.enqueue(MockResponse().setBody(output))

        val parameters = HashMap<String, String>()
        parameters.put("param1", "val1")
        parameters.put("param2", "val2")
        service!!.sendInput(input, parameters).subscribe {result ->
            Assert.assertEquals(parameters, result.input.parameters)
            Assert.assertEquals(parameters, result.output.parameters)
        }

        val expectedBody = "userinput=my%20name%20is%20test1&param1=val1&param2=val2&viewname=tieapi&viewtype=tieapi"
        val request1 = server!!.takeRequest()
        val body = request1.body.readUtf8().trim()
        Assert.assertEquals(expectedBody, body)
    }

    @Test
    fun malformedResponseTest() {
        val response = """{"Malformed json:"":/{{{{{}"""
        server!!.enqueue(MockResponse().setBody(response))

        var exception: Throwable? = null

        service!!.sendInput("").subscribe(
                {},
                {error -> exception = error}
        )

        Assert.assertNotNull(exception)
        Assert.assertTrue(exception is JsonParseException)
    }

    @Test
    fun networkExceptionTest () {
        val input = "this test should throw an exception"

        var exception: Throwable? = null

        server!!.enqueue(MockResponse().setResponseCode(500))
        service!!.sendInput(input).subscribe(
                {},
                {error -> exception = error}
        )

        Assert.assertNotNull(exception)
    }

    @Test
    fun backendExceptionTest () {
        val input = "this test should throw an exception"

        var exception: Throwable? = null

        val errorMessage = """{"status":"-1", "message":"An error occured.", "sessionId":"testSession"}"""

        server!!.enqueue(MockResponse().setBody(errorMessage))
        service!!.sendInput(input).subscribe(
                {},
                {error -> exception = error}
        )

        Assert.assertNotNull(exception)
        Assert.assertTrue(exception is TieApiException)
        Assert.assertEquals("An error occured.", exception?.message)
        Assert.assertEquals("An error occured.", (exception as TieApiException).error?.message)
    }

    @Test
    fun sessionRetentionTest() {
        val sessionId = "testSessionId"
        val testOutput: TieResponse = TieResponse(
                status = "1",
                input = TieInput(text = "this is test input", parameters = HashMap<String, String>()),
                output = TieOutput(text = "this is test output", emotion = "", link = "", parameters = HashMap<String, String>()),
                sessionId = sessionId)

        val gson = Gson()
        val output = gson.toJson(testOutput)

        server!!.enqueue(MockResponse().setBody(output))
        server!!.enqueue(MockResponse().setBody(output))

        // First request should not contain session cookie
        service!!.sendInput("my name is test").subscribe()
        val request1 = server!!.takeRequest()
        Assert.assertNull(request1.headers.get("Cookie"))

        // Second request should contain the session id provided in first answer
        service!!.sendInput("what is my name").subscribe()
        val request2 = server!!.takeRequest()
        Assert.assertEquals("${ApiConstants.SESSION_COOKIE_PREFIX}$sessionId", request2.headers.get("Cookie"))
    }

    @Test
    fun sessionCloseTest() {
        val sessionId = "testSessionId"
        var serviceSessionId: String? = null

        val testResponseBody: TieResponse = TieResponse(
                status = "1",
                input = TieInput(text = "this is test input", parameters = HashMap<String, String>()),
                output = TieOutput(text = "this is test output", emotion = "", link = "", parameters = HashMap<String, String>()),
                sessionId = sessionId)

        // Get initial session id
        val responseBody1 = Gson().toJson(testResponseBody)
        server!!.enqueue(MockResponse().setBody(responseBody1))
        service!!.sendInput("Getting session id").subscribe(){ result -> serviceSessionId = result.sessionId}
        Assert.assertEquals(sessionId, serviceSessionId)
        server!!.takeRequest()

        // Close session
        var closeResult: TieCloseSessionResponse? = null
        server!!.enqueue(MockResponse().setBody(defaultCloseModelResponseJson))
        service!!.close().subscribe({ result -> closeResult = result })
        server!!.takeRequest()

        Assert.assertEquals(defaultCloseModelResponse.response.message, closeResult?.response?.message)
        Assert.assertEquals(defaultCloseModelResponse.response.status, closeResult?.response?.status)

        // Send another request, assert we are not sending a session id
        server!!.enqueue(MockResponse().setBody(emptyOutputModelResponseJson))
        service!!.sendInput("Closed").subscribe()

        val request1 = server!!.takeRequest()
        Assert.assertNull(request1.getHeader("Cookie"))
    }

    @Test
    fun errorOnCloseConnectionTest() {
        val sessionId = "testSessionId"
        var serviceSessionId: String? = null

        val testResponseBody: TieResponse = TieResponse(
                status = "1",
                input = TieInput(text = "this is test input", parameters = HashMap<String, String>()),
                output = TieOutput(text = "this is test output", emotion = "", link = "", parameters = HashMap<String, String>()),
                sessionId = sessionId)

        // Get initial session id
        val responseBody1 = Gson().toJson(testResponseBody)
        server!!.enqueue(MockResponse().setBody(responseBody1))
        service!!.sendInput("Getting session id").subscribe(){ result -> serviceSessionId = result.sessionId}
        Assert.assertEquals(sessionId, serviceSessionId)
        server!!.takeRequest()

        // Try close session but fail
        var exception: Throwable? = null
        server!!.enqueue(MockResponse().setResponseCode(404))
        service!!.close().subscribe(
            {},
            {error -> exception = error})
        server!!.takeRequest()

        Assert.assertNotNull(exception)

        // Send another request, assert we are not sending a session id
        server!!.enqueue(MockResponse().setBody(emptyOutputModelResponseJson))
        service!!.sendInput("Should be closed").subscribe()

        val request1 = server!!.takeRequest()
        Assert.assertNull(request1.getHeader("Cookie"))
    }
}
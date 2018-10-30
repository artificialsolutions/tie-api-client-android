# tie-api-client-android
This library provides a way of communicating with a Teneo Engine server instance.
## Usage

### Example usage
Messages are sent to the engine by calling sendInput on the TieApiService singleton:
``` java
TieApiService.getSharedInstance().sendInput(text, parameters)
	.subscribeOn(Schedulers.io())
	.observeOn(AndroidSchedulers.mainThread())
	.subscribeWith(new DisposableSingleObserver<TieResponse>() {
	    @Override
	    public void onSuccess(TieResponse result) {
	        // Do something with the result
	    }
	    @Override
	    public void onError(Throwable e) {
	        // Do something with the exception
	    }
	});
```

## Installation
You can add the library to your project through jCenter. Make sure to reference the jcenter repository, and then add the dependency
com.artificialsolutions.tie-sdk-android:tie-sdk-android:1.0.0
Using the library also requires rxjava2.

build.gradle example:
```
buildscript {
    repositories {
        jcenter()
    }
}

dependencies {
    implementation 'com.artificialsolutions.tie-sdk-android:tie-sdk-android:1.0.0'
    implementation 'io.reactivex.rxjava2:rxandroid:2.0.1'
}
```

## API Documentation
### TieApiService.setup
Before usage, setup needs to be called once with context, base url, and endpoint.

``` java
TieApiService.getSharedInstance().setup(getApplicationContext(), teneoEngineBaseUrl, endpoint);
```

### TieApiService.input
Communication with Teneo Interaction Engine is done with sendInput function, that
returns an observable that produces either a TieResponse object with onSuccess or a Throwable on onError.

``` java
TieApiService.getSharedInstance().sendInput(userInput, parameters)
```

**Parameters:**
*userInput* : String
The input string that is sent to the engine.

*parameters* : HashMap<String, String>
Any engine instance specific parameters. The following keywords are reserved: *viewtype*, *viewname* and *userinput*.

**Response:**
On success a **TieResponse** object is returned.
- input: TieInput
    - text: String
    - parameters: HashMap<String, String>
- output: TieOutput
    - text: String
    - parameters: HashMap<String, String>
    - link: String
    - emotion: String
- status: String

On fail the call returns a Throwable. The exception can be **JsonParseException**, **IOException**, **NotInitializedException** when setup has not been called, or **TieApiException** when the engine responds with an error.
TieApiException:
- error: TieErrorResponse
    - status: String
    - input: TieInput
        - text: String
        - parameters: HashMap<String, String>
    - message: String

### TieApiService.close
The sdk maintains the session until it is expired by the server, in which case the session is renewed automatically, or until it is closed by calling the close function.

``` java
TieApiService.getSharedInstance().close();
```

### Error handling
Server-sent error messages are returned with sendInput ja close functions as TieApiException objects.

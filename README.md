# Datadog SDK for Android

> A client-side Android library to interact with Datadog.

## Getting Started 

## Gradle Dependency

```groovy
dependencies {
    implementation("com.datadoghq:dd-sdk-android:x.x.x")
}
```

## Usage

If you were a user of the SDK version `0.2.5` or lower, take a look at our 
[Migration Guide](Migrating_To_1.0.0.md).

### Initial Setup

Before you can use the SDK, you need to setup the library with your application
context and your API token. You can create a token from the Integrations > API
in Datadog. **Make sure you create a key of type `Client Token`.**

```kotlin
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Datadog.initialize(this, BuildConfig.DD_CLIENT_TOKEN)
    }
}
```

### Setup for Europe

If you're targetting our [Europe servers](https://datadoghq.eu), you can
initialize the library like this: 

```kotlin
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Datadog.initialize(this, BuildConfig.DD_CLIENT_TOKEN, Datadog.DATADOG_EU)
    }
}
```

### Logger Initialization

You can create a `Logger` instance using the dedicated builder, as follow:

```kotlin
    logger = Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.example.app.android")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("name")
            .build();
```

### Logging

You can then send logs with the following methods, mimicking the ones available
in the Android Framework: 

```kotlin
    logger.d("A debug message.")
    logger.i("Some relevant information ?")
    logger.w("An important warning…")
    logger.e("An error was met!")
    logger.wtf("What a Terrible Failure!")
```

### Logging Errors

If you caught an exception and want to log it with a message, you can do so as
follow:

```kotlin
    try {
        doSomething()
    } catch (e : IOException) {
        logger.e("Error while doing something", e)
    }
```

> Note: All logging methods can have a throwable attached to them.

### Adding context

#### Tags

Tags take the form of a single String, but can also represent key-value pairs when using a colon, and are 
You can add tags to a specific logger as follow: 

```kotlin
    // This will add a tag "build_type:debug" or "build_type:release" accordingly
    logger.addTag("build_type", BuildConfig.BUILD_TYPE)
    
    // This will add a tag "android"
    logger.addTag("android")
```

You can remove tags from a specific logger as follow: 

```kotlin
    // This will remove any tag starting with "build_type:"
    logger.removeTagsWithKey("build_type")
    
    // This will remove the tag "android"
    logger.removeTag("android")
``` 

#### Attributes

Attributes are always in the form of a key-value pair. The value can be any primitive, String or Date.
You can add attributes to a specific logger as follow:

```kotlin
    // This will add an attribute "version_code" with an integer value
    logger.addAttribute("version_code", BuildConfig.VERSION_CODE)
    // This will add an attribute "version_name" with a String value
    logger.addAttribute("version_name", BuildConfig.VERSION_NAME)
```

You can remove attributes from a specific logger as follow: 

```kotlin
    logger.removeAttribute("version_code")
    logger.removeAttribute("version_name")
``` 

#### Local Attributes

Sometimes, you might want to log a message with attributes only for that specific message. You can 
do so by providing a map alongside the message, each entry being added as an attribute.

```kotlin
    logger.i("onPageStarted", attributes = mapOf("http.url", url))
```

In Java you can do so as follow:
```java
    mLogger.d(
            "onPageStarted",
            null, 
            new HashMap<String, Object>() {{
                put("http.url", url);
            }}
    );
```

## Contributing

Pull requests are welcome, but please open an issue first to discuss what you
would like to change. For more information, read the 
[Contributing Guide](CONTRIBUTING.md).

## License

[Apache License, v2.0](LICENSE)

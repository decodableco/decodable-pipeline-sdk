# Decodable Java SDK

_Version 1.0.0-SNAPSHOT_

This repository contains a software development kit (SDK) for implementing [Apache Flink](https://flink.apache.org/) jobs
and running them on Decodable as a [custom pipeline](https://docs.decodable.co/docs/create-pipelines-using-your-own-apache-flink-jobs).

## Structure

* _sdk_: The Decodable SDK
* _examples_: Examples for using the SDK

## Installation

The SDK is not available via Maven Central yet. For the time being, build and install it into your local Maven repository yourself, as described under [Building the SDK](#building-the-sdk).
Then add the SDK dependency to the _pom.xml_ of your Maven project:

```xml
...
<dependency>
	<groupId>co.decodable</groupId>
	<artifactId>decodable-sdk-java</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
...
```

Or, to your _build.gradle_ when using Gradle:

```
...
implementation 'co.decodable:decodable-sdk-java:1.0.0-SNAPSHOT'
...
```

## Usage

See the project under _examples/apache-maven/custom-pipelines-hello-world/_ for a complete example project
which shows how to use the Decodable SDK for implementing and testing Flink jobs to be executed as custom pipelines on Decodable.

Refer to the [documentation](https://docs.decodable.co/docs/create-pipelines-using-your-own-apache-flink-jobs) for instructions on how to deploy your job as a custom pipeline on the Decodable platform.

## Building the SDK

Gradle is used for building the SDK.

Change to the _sdk_ directory and run the following to produce the SDK binary:

```bash
./gradlew build
```

Run the following to install the SDK JAR into your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

## License

This code base is available under the Apache License, version 2.

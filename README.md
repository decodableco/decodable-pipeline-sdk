# Decodable Pipeline SDK

_Version 1.0.0.Beta6_

This repository contains a software development kit (SDK) for implementing [Apache Flink](https://flink.apache.org/) jobs
and running them on Decodable as a [custom pipeline](https://docs.decodable.co/docs/create-pipelines-using-your-own-apache-flink-jobs).

## Structure

* _sdk_: The Decodable SDK
* _examples_: Examples for using the SDK

## Requirements

The following components are required in order to use this SDK:

* Java 11 (note that Java 17 is not supported by Apache Flink 1.16 yet)
* Docker (for integration tests)

## Installation

The SDK is available on Maven Central.
Add the SDK dependency to the _pom.xml_ of your Maven project:

```xml
...
<dependency>
  <groupId>co.decodable</groupId>
  <artifactId>decodable-pipeline-sdk</artifactId>
  <version>1.0.0.Beta6</version>
</dependency>
...
```

Or, to your _build.gradle_ when using Gradle:

```
...
implementation 'co.decodable:decodable-pipeline-sdk:1.0.0.Beta6'
...
```

## Usage

See the project under _examples/custom-pipelines-hello-world/_ for a complete example project
which shows how to use the Decodable SDK for implementing and testing Flink jobs to be executed as custom pipelines on Decodable.

Refer to the [API documentation](https://docs.decodable.co/api/pipeline-sdk.html) to learn how to use this SDK for implement your custom Flink jobs.

Refer to the [documentation](https://docs.decodable.co/docs/create-pipelines-using-your-own-apache-flink-jobs) for instructions on how to deploy your job as a custom pipeline on the Decodable platform.

## Contributing

We look forward to any contributions to this SDK.
The project uses [GitHub Issues](https://github.com/decodableco/decodable-pipeline-sdk/issues) for tracking feature requests and bug reports.
Before picking up any larger work items, it is recommended to reach out via the [Decodable Community](decodablecommunity.slack.com) Slack space.

### Building the SDK

Gradle is used for building the SDK.

Change to the _sdk_ directory and run the following to produce the SDK binary:

```bash
./gradlew build
```

Run the following to apply the auto-formatter to the source code:

```bash
./gradlew spotlessApply
```

Run the following to install the SDK JAR into your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

### Commit Conventions

This project uses [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) for its commit messages,
which serve as the input for generated release notes.
Each commit message must be in the following two forms:

```
#<issue key> <type>: <description>

Example:

#42 feat: Providing support for the Flink Table API
```

Optionally, a message body may be specified too:

```
#42 feat: Providing support for the Flink Table API

Streams can be accessed using Flink SQL now, also ...
```

### Release Process

This project runs a fully automated release process on GitHub Actions.
You must be a committer to this repository in order to perform a release.
To trigger the release of a new SDK version, execute the [release](https://github.com/decodableco/decodable-pipeline-sdk/actions/workflows/release.yml) workflow, specifying the version to be released (e.g. `1.1.0.Final`) and the next version for main (e.g. `1.2.0-SNAPSHOT`).
The release pipeline performs the following steps:

* Updating versions in the SDK and example project
* Deploying the SDK artifact to Maven Central
* Publishing the API documentation
* Creating a release on GitHub, including a changelog

## License

This code base is available under the Apache License, version 2.

# Custom Pipelines Hello World Example

This is a simple Flink job which uses the Decodable Custom Pipelines SDK.
It takes data from one Decodable stream, _purchase-orders_, processes the data (simply upper-casing the customer name of each purchase order record), and writes the modified records to another stream, _purchase\_order\_processed_.

The job is implemented twice, showing two different flavours:

* **[DataStreamJob.java](./src/main/java/co/decodable/examples/cpdemo/DataStreamJob.java)** (using Flink's DataStream API)
* **[TableAPIJob.java](./src/main/java/co/decodable/examples/cpdemo/TableAPIJob.java)** (using Flink's Table API)

## Build

Run the following to build this example project using [Apache Maven](https://maven.apache.org/):

```bash
./mvnw clean verify
```

Run the following to build this example project using [Gradle](https://gradle.org/):

```bash
./gradlew clean build
```

## Deployment

Set the `job_file_path` property for the pipeline resource in _decodable-resources.yaml_ depending on your chosen build tool:

* `target/custom-pipelines-hello-world-0.1.jar` when using Maven
* `build/libs/custom-pipelines-hello-world-0.1-all.jar` when using Gradle

Deploy the example to Decodable using the CLI:

```bash
decodable apply decodable-resources.yaml
```

This also deploys a [REST source connector](https://docs.decodable.co/connect/source/rest.html) for posting purchase orders to the input stream which is processed by the pipeline.

## Usage

Place a purchase order like so:

```bash
export DECODABLE_API_KEY_BASE64=$(echo -n 'my very secret API key' | base64)

curl -X POST `decodable query --name purchase-orders-source --kind connection | yq .status.properties.url` \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer ${DECODABLE_API_KEY_BASE64}" \
  -d @purchase-order.json
```

Examine the processed data in the output stream:

```bash
decodable stream preview `decodable query --name purchase-orders-processed --kind stream --keep-ids | yq .metadata.id`
```

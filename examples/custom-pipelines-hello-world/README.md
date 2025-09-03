# Custom Pipelines Example Jobs using the SDK

This project contains several basic Flink jobs which use the Decodable Custom Pipelines SDK.
The jobs are all centered around the same example which is about consuming data from one Decodable stream, _purchase-orders_, processing the records (upper-casing the customer name of each purchase order record), and writing the modified records to another stream, _purchase\_order\_processed_.

The example is implemented in a few different flavours, demonstrating how to work with:

* **keyed and keyless append streams** using both Flink's DataStream API and Table API 
  * [KeyedAppendStreamJob.java](./src/main/java/co/decodable/examples/cpdemo/KeyedAppendStreamJob.java)
  * [KeyedAppendStreamJobTableAPI.java](./src/main/java/co/decodable/examples/cpdemo/KeyedAppendStreamJobTableAPI.java)
  * [Keyed2KeylessAppendStreamJob.java](./src/main/java/co/decodable/examples/cpdemo/Keyed2KeylessAppendStreamJob.java)
  * [KeylessAppendStreamJob.java](./src/main/java/co/decodable/examples/cpdemo/KeylessAppendStreamJob.java)
  * [KeylessAppendStreamJobTableAPI.java](./src/main/java/co/decodable/examples/cpdemo/KeylessAppendStreamJobTableAPI.java)
  * [Keyless2KeyedAppendStreamJob.java](./src/main/java/co/decodable/examples/cpdemo/Keyless2KeyedAppendStreamJob.java)

* **change streams** using Flink's DataStream API
  * [ChangeStreamJob.java](./src/main/java/co/decodable/examples/cpdemo/ChangeStreamJob.java) 

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

* `target/custom-pipelines-hello-world-0.3.jar` when using Maven
* `build/libs/custom-pipelines-hello-world-0.3-all.jar` when using Gradle

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

# Custom Pipelines Hello World Example

This is a simple Flink job which uses the Decodable Custom Pipelines SDK.
It takes data from one Decodable stream, _purchase-orders_, processes the data (simply upper-casing the customer name of each purchase order record), and writes the modified records to another stream, _purchase\_order\_processed_.

## Build

Run the following to build this example project:

```bash
./mvnw clean verify
```

## Deployment

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

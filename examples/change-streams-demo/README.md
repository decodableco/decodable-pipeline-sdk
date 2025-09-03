# Processing Change Streams with the Decodable Pipeline SDK

This project contains example jobs showing how to process **change streams** using Flink's DataStream API.

* [ReviewProcessorJobChangeStreams.java](src/main/java/co/decodable/examples/reviews/datastream/ReviewProcessorJobChangeStreams.java): Stateless processing of CDC records by means of a `RichMapFunction` which adds computed data to the original records.
* [ReviewAggJobChangeStreams.java](src/main/java/co/decodable/examples/reviews/datastream/ReviewAggJobChangeStreams.java): Stateful processing of CDC records by means of a `KeyedProcessFunction` to compute a custom aggregation on top of the original records.

## Project Build

Run the following to build this example project using [Apache Maven](https://maven.apache.org/):

```bash
./mvnw clean verify
```

Run the following to build this example project using [Gradle](https://gradle.org/):

```bash
./gradlew clean build
```
## Local Infra Setup

### Database Container

There is a turn-key ready Docker _[compose.yaml](./docker/compose.yaml)_ to start a MySQL container including the necessary sample data.

```bash
docker compose -f docker/compose.yaml up
```

### Ngrok Tunnel

In order for Decodable to be able to access the containerized MySQL instance running locally, you can run ngrok to setup a TCP tunnel.

> [!IMPORTANT]
> Make sure that you have [ngrok installed](https://ngrok.com/download).

1. Log in to ngrok and **copy your auth token** which you can find [here](https://dashboard.ngrok.com/get-started/your-authtoken).

2. Open the file _[ngrok.yml](./ngrok.yml)_, **paste your auth token** into [line 3](./ngrok.yml#L3), and save the updated file.

3. Run `ngrok start --all --config ngrok.yml` in a terminal which will set up a tunnel to access your local machine.

Terminal output should look similar to the following:

```
ngrok                                                                            (Ctrl+C to quit)

🤫 Decouple policy and sensitive data with vaults: https://ngrok.com/r/secrets

Session Status                online
Account                       <your_account> (Plan: Free)
Update                        update available (version 3.27.0, Ctrl-U to update)
Version                       3.14.0
Region                        Europe (eu)
Latency                       21ms
Web Interface                 http://127.0.0.1:4040
Forwarding                    tcp://2.tcp.eu.ngrok.io:16303 -> localhost:3306

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00 
```

**Take note of the line related to `Forwarding`.**

You'll need the **hostname** and **port** for the **MySQL tcp tunnel** - in the example above `2.tcp.eu.ngrok.io` and `16303` - which you'll use later to configure your Decodable connection and make sure the connector can reach the locally running MySQL instance from the public internet.

## Decodable Deployment

In _[decodable-resources.yaml](./decodable-resources.yaml)_ you find all Decodable resources needed to deploy the full example.

> [!IMPORTANT]
> Before you deploy this using the Decodable CLI you have to specify two placeholders to match your local setup:

* `<YOUR_MYSQL_HOST>`: adapt [placeholder 1](./decodable-resources.yaml#L128) with your proper **ngrok hostname** for the MySQL TCP tunnel
* `<YOUR_MYSQL_PORT>`: adapt [placeholder 2](./decodable-resources.yaml#L131) with your proper **ngrok port** for the MySQL TCP tunnel

Next, set the `job_file_path` property for both Decodable pipeline resources depending on your chosen build tool:

* for Maven: `target/change-streams-demo-0.1.jar`
* for Gradle: `build/libs/change-streams-demo-0.1-all.jar`

With this configuration in place, use the [Decodable CLI](https://docs.decodable.co/cli.html) to deploy all resources:

```bash
decodable apply decodable-resources.yaml
```

The first step is that the CLI will upload and validate the JAR file. Right after that the Decodable resources specified in the YAML manifest are created and the connection as well as the two pipelines are automatically started.

Wait 1-2 minutes until all resources are up and running. 

## Inspect Processing Results

It's time to examine the processed data in the corresponding streams directly in the Decodable Web UI or by using the CLI.

### Original CDC Records (Source Connection)

```bash
decodable stream preview `decodable query --name reviews --kind stream --keep-ids | yq .metadata.id`
```

```json lines
... 
{"after":{"id":26,"itemId":"BLTHSPKR8W","reviewText":"Compact and portable. Perfect for beach trips and picnics."},"before":null,"op":"r","ts_ms":1756891709283}
{"after":{"id":27,"itemId":"ERGOKEYBD1","reviewText":"Comfortable typing experience. Reduced my wrist pain significantly!"},"before":null,"op":"r","ts_ms":1756891709284}
{"after":{"id":30,"itemId":"STLBTL750M","reviewText":"Keeps drinks cold for 24 hours! No leaks and easy to clean."},"before":null,"op":"r","ts_ms":1756891709284}
{"after":{"id":28,"itemId":"ERGOKEYBD1","reviewText":"Takes time to get used to the layout but worth it for ergonomics."},"before":null,"op":"r","ts_ms":1756891709284}
{"after":{"id":29,"itemId":"ERGOKEYBD1","reviewText":"Great keyboard but some keys feel mushy. Build quality could be better."},"before":null,"op":"r","ts_ms":1756891709284}
...
```

### Processed Records (Stateless Custom Java Pipeline)

```bash
decodable stream preview `decodable query --name reviews_processed --kind stream --keep-ids | yq .metadata.id`
```

```json lines
...
{"after":{"charCount":58,"id":26,"itemId":"BLTHSPKR8W","reviewText":"Compact and portable. Perfect for beach trips and picnics."},"before":null,"op":"r","ts_ms":1756891709283}
{"after":{"charCount":67,"id":27,"itemId":"ERGOKEYBD1","reviewText":"Comfortable typing experience. Reduced my wrist pain significantly!"},"before":null,"op":"r","ts_ms":1756891709284}
{"after":{"charCount":59,"id":30,"itemId":"STLBTL750M","reviewText":"Keeps drinks cold for 24 hours! No leaks and easy to clean."},"before":null,"op":"r","ts_ms":1756891709284}
{"after":{"charCount":65,"id":28,"itemId":"ERGOKEYBD1","reviewText":"Takes time to get used to the layout but worth it for ergonomics."},"before":null,"op":"r","ts_ms":1756891709284}
{"after":{"charCount":71,"id":29,"itemId":"ERGOKEYBD1","reviewText":"Great keyboard but some keys feel mushy. Build quality could be better."},"before":null,"op":"r","ts_ms":1756891709284}
...
```

### Aggregated Records (Stateful Custom Java Pipeline)

```bash
decodable stream preview `decodable query --name reviews_agg --kind stream --keep-ids | yq .metadata.id`
```

```json lines
...
{"after":{"cnt":1,"itemId":"LEDDESK75W","reviewTexts":{"18":"Bright and adjustable lighting. Great for late-night work sessions!"}},"before":null,"op":"c","ts_ms":1756891709282}
{"after":{"cnt":2,"itemId":"LEDDESK75W","reviewTexts":{"18":"Bright and adjustable lighting. Great for late-night work sessions!","19":"Good lamp but the touch controls are sometimes unresponsive."}},"before":{"cnt":1,"itemId":"LEDDESK75W","reviewTexts":{"18":"Bright and adjustable lighting. Great for late-night work sessions!"}},"op":"u","ts_ms":1756891709283}
{"after":{"cnt":1,"itemId":"BKPCK42LTR","reviewTexts":{"16":"Great backpack but shoulder straps could be more padded for heavy loads."}},"before":null,"op":"c","ts_ms":1756891709282}
{"after":{"cnt":2,"itemId":"BKPCK42LTR","reviewTexts":{"16":"Great backpack but shoulder straps could be more padded for heavy loads.","17":"Excellent build quality. Water-resistant and fits my laptop perfectly."}},"before":{"cnt":1,"itemId":"BKPCK42LTR","reviewTexts":{"16":"Great backpack but shoulder straps could be more padded for heavy loads."}},"op":"u","ts_ms":1756891709282}
{"after":{"cnt":1,"itemId":"PHNCSE12PR","reviewTexts":{"22":"Excellent protection and fits perfectly. Survived multiple drops!"}},"before":null,"op":"c","ts_ms":1756891709283}
{"after":{"cnt":2,"itemId":"PHNCSE12PR","reviewTexts":{"22":"Excellent protection and fits perfectly. Survived multiple drops!","23":"Case is bulky but protection is worth it. Wireless charging works fine."}},"before":{"cnt":1,"itemId":"PHNCSE12PR","reviewTexts":{"22":"Excellent protection and fits perfectly. Survived multiple drops!"}},"op":"u","ts_ms":1756891709283}
{"after":{"cnt":3,"itemId":"LEDDESK75W","reviewTexts":{"18":"Bright and adjustable lighting. Great for late-night work sessions!","19":"Good lamp but the touch controls are sometimes unresponsive.","20":"Perfect brightness levels and the USB charging port is very convenient."}},"before":{"cnt":2,"itemId":"LEDDESK75W","reviewTexts":{"18":"Bright and adjustable lighting. Great for late-night work sessions!","19":"Good lamp but the touch controls are sometimes unresponsive."}},"op":"u","ts_ms":1756891709283}
{"after":{"cnt":4,"itemId":"LEDDESK75W","reviewTexts":{"18":"Bright and adjustable lighting. Great for late-night work sessions!","19":"Good lamp but the touch controls are sometimes unresponsive.","20":"Perfect brightness levels and the USB charging port is very convenient.","21":"Sturdy base and flexible arm. Light is even and doesn't cause eye strain."}},"before":{"cnt":3,"itemId":"LEDDESK75W","reviewTexts":{"18":"Bright and adjustable lighting. Great for late-night work sessions!","19":"Good lamp but the touch controls are sometimes unresponsive.","20":"Perfect brightness levels and the USB charging port is very convenient."}},"op":"u","ts_ms":1756891709283}
...
```

Feel free to connect to the containerized MySQL instance and apply changes (`INSERTs, UPDATEs, DELETEs`) to the `Review` table in the `demo` database. This allows you to inspect how:

* the source connection first captures these database changes
* each of the two custom Java pipelines then processes the CDC events accordingly
* the resulting records are written into the two output streams `reviews_processed` and `reviews_agg`

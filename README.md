# COMP3011.assignment1

## Architecture

The STT project is split into controllers, services, DTOs and config. Controllers handles HTTP requests, services contains the main logic and shared state. DTOs are used to carry request and response data, while config creates the client used for the STT API.

Dependencies are pased through the constructors and provided by spring. Shared state is kept inside services and uses atomic types so multiple requests can safely happen at once.

`ApiExceptionHandler` handles errors in one place and converts them into the required `ErrorResponse` format required by the YAML.

## Concurrency

Transciprtion endpoint is probably I/O bound, most of the time would be spent waiting for the external OpenAI STT API, one of the TITAN consoole output actually shows 2.2 seconds:

STDOUT: 2026-09-16T10:44:56.652Z  INFO 1214 --- [Assignment1] [nio-8080-exec-8] c.a.service.TranscriptionService         : Transcribed 307484 bytes with model gpt-4o-mini-transcribe in 2239 ms

Source: Job: 34e5a566 by rey.nguyen

Which would include several factors such as uploading, processing and receiving the reponse (might even be going through a TITAN proxy which might slow things down even more)

Concurrency is improved using virtual threads. When a request is waiting for the STT API, underlying threads can be used for other threads. This doesn't mean that the API would be faster, but would certainly allow for more than 200 blocking requests to run at the same time without being limited by Apache Tomcat's thread pool of 200.

Shared states such as token counts and shutdown uses atomic types, allowing for multiple requests to happen safely at the same time.

## Deployment Notes

The deployment environment uses a proxy for internet access. `ProxySelector.getDefault()` is used so Javas `HttpClient` uses the JVM proxy settings.

The project builds with Java 25 (per project setup instructions in Assignment1.pdf slides) and deployment runs Java 26. 
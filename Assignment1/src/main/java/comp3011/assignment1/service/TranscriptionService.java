package comp3011.assignment1.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import comp3011.assignment1.dto.OpenAiTranscriptionResponse;

@Service
public class TranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);
    private static final String TRANSCRIPTIONS_PATH = "/audio/transcriptions";
    private static final String DEFAULT_FILENAME = "recording.webm";

    private final RestClient openAiRestClient;
    private final StatsService statsService;
    private final String model;

    public TranscriptionService(
            RestClient openAiRestClient,
            StatsService statsService,
            @Value("${openai.api.model}") String model) {
        this.openAiRestClient = openAiRestClient;
        this.statsService = statsService;
        this.model = model;
    }

    public String transcribe(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No audio data was uploaded.");
        }

        long startedAt = System.nanoTime();
        OpenAiTranscriptionResponse response = callSpeechToText(audio);
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;

        // stt api might not return usage so only count token if it does
        if (response.usage() != null) {
            statsService.recordTokenUsage(response.usage().inputTokens(), response.usage().outputTokens());
        }

        log.info("Transcribed {} bytes with model {} in {} ms", audio.getSize(), model, elapsedMillis);

        return response.text() == null ? "" : response.text();
    }

    private OpenAiTranscriptionResponse callSpeechToText(MultipartFile audio) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", asNamedResource(audio));
        form.add("model", model);
        form.add("response_format", "json");

        try {
            OpenAiTranscriptionResponse response = openAiRestClient.post()
                    .uri(TRANSCRIPTIONS_PATH)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(OpenAiTranscriptionResponse.class);

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The speech to text service returned an empty response.");
            }
            return response;
        } catch (RestClientResponseException ex) {
            // extra careful api error details, opting full error for a generic one
            log.warn("Speech to text service returned status {}", ex.getStatusCode().value());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The speech to text service could not process the recording.");
        } catch (RestClientException ex) {
            log.warn("Speech to text service was unreachable: {}", ex.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                    "The speech to text service did not respond in time.");
        }
    }

    private ByteArrayResource asNamedResource(MultipartFile audio) {
        byte[] content;
        try {
            content = audio.getBytes();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded audio could not be read.");
        }

        // file name tells stt api what audio format is being sent
        String filename = audio.getOriginalFilename() == null ? DEFAULT_FILENAME : audio.getOriginalFilename();
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}

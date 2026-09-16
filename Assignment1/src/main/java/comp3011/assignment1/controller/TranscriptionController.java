package comp3011.assignment1.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.dto.TranscriptionResponse;
import comp3011.assignment1.service.TranscriptionService;

@RestController
@RequestMapping("/api/v1")
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TranscriptionResponse transcribe(@RequestParam("audio") MultipartFile audio) {
        return new TranscriptionResponse(transcriptionService.transcribe(audio));
    }
}

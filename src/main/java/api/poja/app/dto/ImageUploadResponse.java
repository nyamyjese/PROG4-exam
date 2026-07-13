package api.poja.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ImageUploadResponse(
    UUID id, String fileName, String email, String s3key, LocalDateTime creationDate) {}

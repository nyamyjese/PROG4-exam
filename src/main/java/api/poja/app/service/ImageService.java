package api.poja.app.service;

import api.poja.app.dto.ImageResponse;
import api.poja.app.dto.ImageUploadResponse;
import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.BlackAndWhiteConversionRequested;
import api.poja.app.entity.ImageEntity;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.repository.ImageRepository;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ImageService {

  private static final List<String> ALLOWED_CONTENT_TYPES =
      List.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);

  private static final Duration PRESIGN_DURATION = Duration.ofHours(1);

  private final ImageRepository imageRepository;
  private final BucketComponent bucketComponent;
  private final EventProducer<BlackAndWhiteConversionRequested> eventProducer;

  @SneakyThrows
  @Transactional
  public ImageUploadResponse uploadImage(MultipartFile file, String email) {
    validateFile(file);

    UUID id = UUID.randomUUID();
    String originalFilename = file.getOriginalFilename();
    String extension = getExtension(originalFilename);
    String s3Key = "originals/" + id + "." + extension;

    File tempFile = toTempFile(file, extension);
    bucketComponent.upload(tempFile, s3Key);

    ImageEntity entity =
        ImageEntity.builder()
            .id(id)
            .fileName(originalFilename)
            .email(email)
            .s3Key(s3Key)
            .createdAt(LocalDateTime.now())
            .build();
    imageRepository.save(entity);

    var event =
        BlackAndWhiteConversionRequested.builder()
            .id(id)
            .s3KeyOriginal(s3Key)
            .extension(extension)
            .email(email)
            .fileName(originalFilename)
            .build();
    eventProducer.accept(List.of(event));

    String presignedUrl = bucketComponent.presign(s3Key, PRESIGN_DURATION).toString();
    return new ImageUploadResponse(
        id, originalFilename, email, presignedUrl, entity.getCreatedAt());
  }

  public List<ImageResponse> findAll() {
    return imageRepository.findAll().stream().map(this::toResponse).toList();
  }

  private ImageResponse toResponse(ImageEntity entity) {
    String presignedUrl = bucketComponent.presign(entity.getS3Key(), PRESIGN_DURATION).toString();

    return new ImageResponse(
        entity.getId(),
        entity.getFileName(),
        entity.getEmail(),
        presignedUrl,
        entity.getCreatedAt());
  }

  private File toTempFile(MultipartFile file, String extension) throws IOException {
    File tempFile = File.createTempFile("upload-", "." + extension);
    file.transferTo(tempFile);
    return tempFile;
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("The file is empty or missing");
    }
    if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
      throw new IllegalArgumentException("Only JPEG and PNG formats are accepted");
    }
  }

  private String getExtension(String filename) {
    if (filename != null && filename.contains(".")) {
      return filename.substring(filename.lastIndexOf('.') + 1);
    }
    return "jpg";
  }
}

package api.poja.app.endpoint.rest.controller;

import api.poja.app.dto.ImageResponse;
import api.poja.app.dto.ImageUploadResponse;
import api.poja.app.service.ImageService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@AllArgsConstructor
@Validated
public class ImageController {

  private final ImageService imageService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageUploadResponse> uploadImage(
      @RequestParam("file") MultipartFile file,
      @RequestParam("email") @NotBlank @Email String email) {

    ImageUploadResponse response = imageService.uploadImage(file, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<ImageResponse>> findAll() {
    return ResponseEntity.ok(imageService.findAll());
  }
}

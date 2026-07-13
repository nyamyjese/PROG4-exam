package api.poja.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageEntity {
  @Id private UUID id;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String email;

  @Column(name = "s3_key", nullable = false, length = 1000)
  private String s3Key;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}

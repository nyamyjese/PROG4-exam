package api.poja.app.repository;

import api.poja.app.entity.ImageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {}

package api.poja.app.service.event;

import api.poja.app.endpoint.event.model.BlackAndWhiteConversionRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BlackAndWhiteConversionRequestedService
    implements Consumer<BlackAndWhiteConversionRequested> {

  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(BlackAndWhiteConversionRequested event) {
    File originalFile = bucketComponent.download(event.getS3KeyOriginal());

    File bwFile = convertToBlackAndWhite(originalFile, event.getExtension());

    String bwKey = "black-and-white/" + event.getId() + "." + event.getExtension();
    bucketComponent.upload(bwFile, bwKey);

    String presignedUrl = bucketComponent.presign(bwKey, java.time.Duration.ofDays(7)).toString();

    var recipient = new InternetAddress(event.getEmail());
    var email =
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Votre image en noir et blanc est prête",
            "Bonjour,\n\nVotre image \""
                + event.getFileName()
                + "\" a été convertie en noir et blanc.\n\n"
                + "Lien de téléchargement (valable 7 jours) :\n"
                + presignedUrl
                + "\n\nCordialement.",
            List.of());
    mailer.accept(email);
  }

  private File convertToBlackAndWhite(File originalFile, String extension) throws Exception {
    BufferedImage originalImage = ImageIO.read(originalFile);
    BufferedImage grayImage =
        new BufferedImage(
            originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    Graphics g = grayImage.getGraphics();
    g.drawImage(originalImage, 0, 0, null);
    g.dispose();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String format = extension.equalsIgnoreCase("png") ? "png" : "jpg";
    ImageIO.write(grayImage, format, baos);

    File tempFile = File.createTempFile("bw-", "." + extension);
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(baos.toByteArray());
    }
    return tempFile;
  }
}

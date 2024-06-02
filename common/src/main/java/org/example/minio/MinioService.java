package org.example.minio;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

  private final MinioProperties properties;
  private final MinioClient client;

  /**
   * Download image.
   *
   * @param id image id
   * @return image bytes
   * @throws Exception when some went wrong
   */
  public byte[] downloadImage(String id) throws Exception {
    var objectArgs = GetObjectArgs.builder()
            .bucket(properties.getBucket())
            .object(id)
            .build();
    return IOUtils.toByteArray(client.getObject(objectArgs));
  }

  /**
   * Upload image.
   *
   * @param file image
   * @return image
   * @throws Exception when some went wrong
   */
  public UUID uploadImage(MultipartFile file) throws Exception {
    var id = UUID.randomUUID();

    client.putObject(
            PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(id.toString())
                    .stream(file.getInputStream(), file.getSize(), properties.getImageSize())
                    .contentType(file.getContentType())
                    .build()
    );

    return id;
  }

  /**
   * Delete image.
   *
   * @param id image id
   * @throws Exception when some went wrong
   */
  public void deleteImage(String id) throws Exception {
    client.removeObject(
            RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(id)
                    .build());
  }
}

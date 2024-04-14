package org.example.services;

import org.example.config.MinioProperties;
import org.example.domain.Image;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioProperties properties;
    private final MinioClient client;

    public byte[] downloadImage(String id) throws Exception {
        var objectArgs = GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(id)
                .build();
        return IOUtils.toByteArray(client.getObject(objectArgs));
    }

    public Image uploadImage(MultipartFile file) throws Exception {
        var id = UUID.randomUUID();

        client.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(id.toString())
                        .stream(file.getInputStream(), file.getSize(), properties.getImageSize())
                        .contentType(file.getContentType())
                        .build()
        );

        return new Image().setImageId(id.toString()).setFilename(file.getOriginalFilename()).setSize(file.getSize());
    }

    public void deleteImage(String id) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(id)
                        .build());
    }
}

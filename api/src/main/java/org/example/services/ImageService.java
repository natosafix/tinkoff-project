package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.domain.Image;
import org.example.exceptions.ForbiddenException;
import org.example.exceptions.ImageNotFoundException;
import org.example.minio.MinioService;
import org.example.repositories.ImageRepository;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
  private final MinioService minioService;
  private final UserService userService;
  private final ImageRepository imageRepository;

  public Image getImage(String imageId) {
    return imageRepository.findById(UUID.fromString(imageId))
            .orElseThrow(() -> new ImageNotFoundException(imageId));
  }

  /**
   * Get user images.
   *
   * @param username username
   * @return user images
   */
  public List<Image> getUserImages(String username) {
    var user = userService.getUserByUsername(username);

    return imageRepository.findAll(Example.of(new Image().setUser(user)));
  }

  /**
   * Download image.
   *
   * @param imageId image id
   * @param authorUsername author username
   * @return image bytes
   * @throws Exception when some went wrong
   */
  public byte[] downloadImage(String imageId, String authorUsername) throws Exception {
    var image = getImage(imageId);
    var user = userService.getUserByUsername(authorUsername);
    checkUserAccess(user.getId(), image);

    return minioService.downloadImage(image.getImageId().toString());
  }

  /**
   * Upload image.
   *
   * @param file image
   * @param authorUsername author username
   * @return image
   * @throws Exception when some went wrong
   */
  public Image uploadImage(MultipartFile file, String authorUsername) throws Exception {
    var user = userService.getUserByUsername(authorUsername);
    var imageId = minioService.uploadImage(file);
    var image = new Image()
            .setImageId(imageId)
            .setFilename(file.getOriginalFilename())
            .setSize(file.getSize());

    image.setUser(user);
    imageRepository.save(image);

    return image;
  }

  /**
   * Delete image.
   *
   * @param imageId image id
   * @param authorUsername author username
   * @throws Exception when some went wrong
   */
  public void deleteImage(String imageId, String authorUsername) throws Exception {
    var image = getImage(imageId);
    var user = userService.getUserByUsername(authorUsername);
    checkUserAccess(user.getId(), image);

    imageRepository.deleteById(UUID.fromString(imageId));
    minioService.deleteImage(image.getImageId().toString());
  }

  private void checkUserAccess(int currentUserId, Image image) {
    var imageOwnerId = image.getUser().getId();

    if (!imageOwnerId.equals(currentUserId)) {
      throw new ForbiddenException();
    }
  }
}

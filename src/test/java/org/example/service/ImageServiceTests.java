package org.example.service;

import org.example.domain.Image;
import org.example.domain.User;
import org.example.exceptions.ForbiddenException;
import org.example.exceptions.ImageNotFoundException;
import org.example.services.ImageService;
import org.example.services.MinioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class ImageServiceTests extends ServiceTestsBase {

  private ImageService imageService;
  @Mock
  private MinioService minioService;

  @AfterEach
  @BeforeEach
  public void prepare() {
    imageService = new ImageService(minioService, userService, imageRepository);
  }

  @Test
  public void uploadImage_ShouldSaveImage() throws Exception {
    var user = getTestUser();
    var image = addImageToUser(user);
    var file = new MockMultipartFile("name", image.getFilename(), "image/png", new byte[0]);
    doReturn(image).when(minioService).uploadImage(file);

    var imageActual = imageService.uploadImage(file, user.getUsername());

    assertEquals(image, imageActual);
    var imageRepo = imageRepository.findById(image.getImageId()).get();
    assertImagesEquals(image, imageRepo);
  }

  @Test
  public void downloadImage_ShouldReturnBytes() throws Exception {
    var user = getTestUser();
    var image = addImageToUser(user);
    var imageBytes = new byte[]{0, 1};
    doReturn(imageBytes).when(minioService).downloadImage(image.getImageId());

    var imageBytesActual = imageService.downloadImage(image.getImageId(), user.getUsername());

    assertEquals(imageBytes, imageBytesActual);
  }

  @Test
  public void downloadImage_ShouldThrowForbiddenException_WhenImageBelongsOtherUser() throws Exception {
    var user = getTestUser();
    var otherUser = getTestUser();
    var image = addImageToUser(user);
    var imageBytes = new byte[]{0, 1};
    doReturn(imageBytes).when(minioService).downloadImage(image.getImageId());

    Executable action = () -> imageService.downloadImage(image.getImageId(), otherUser.getUsername());

    assertThrows(ForbiddenException.class, action, "Недостаточно прав для использования ресурса");
  }

  @Test
  public void downloadImage_ShouldThrowImageNotFoundException_WhenImageIdNotExists() throws Exception {
    var user = getTestUser();
    var imageBytes = new byte[]{0, 1};
    var imageId = UUID.randomUUID().toString();
    doReturn(imageBytes).when(minioService).downloadImage(imageId);

    Executable action = () -> imageService.downloadImage(imageId, user.getUsername());

    assertThrows(ImageNotFoundException.class, action, "Не найдена картинка с id=" + imageId);
  }

  @Test
  public void getUserImages_ShouldReturnUserImages() {
    var user = getTestUser();
    var images = List.of(addImageToUser(user), addImageToUser(user));

    var imagesActual = imageService.getUserImages(user.getUsername());

    assertEquals(2, imagesActual.size());
    assertImagesEquals(images.get(0), imagesActual.get(0));
    assertImagesEquals(images.get(1), imagesActual.get(1));
  }

  @Test
  public void getUserImages_ShouldReturnEmpty_WhenUserDoNotHaveImages() {
    var user = getTestUser();

    var imagesActual = imageService.getUserImages(user.getUsername());

    assertArrayEquals(new Image[0], imagesActual.toArray(new Image[0]));
  }

  @Test
  public void deleteImage_ShouldDelete() throws Exception {
    var user = getTestUser();
    var image = addImageToUser(user);
    doNothing().when(minioService).deleteImage(image.getImageId());

    imageService.deleteImage(image.getImageId(), user.getUsername());

    assertFalse(imageRepository.findById(image.getImageId()).isPresent());
  }

  @Test
  public void deleteImage_ShouldThrowForbiddenException_WhenImageBelongsOtherUser() throws Exception {
    var user = getTestUser();
    var otherUser = getTestUser();
    var image = addImageToUser(user);
    doNothing().when(minioService).deleteImage(image.getImageId());

    Executable action = () -> imageService.deleteImage(image.getImageId(), otherUser.getUsername());

    assertThrows(ForbiddenException.class, action, "Недостаточно прав для использования ресурса");
  }

  @Test
  public void deleteImage_ShouldThrowImageNotFoundException_WhenImageIdNotExists() throws Exception {
    var user = getTestUser();
    var imageId = UUID.randomUUID().toString();
    doNothing().when(minioService).deleteImage(imageId);

    Executable action = () -> imageService.deleteImage(imageId, user.getUsername());

    assertThrows(ImageNotFoundException.class, action, "Не найдена картинка с id=" + imageId);
  }

  private Image addImageToUser(User user) {
    var image = new Image()
            .setImageId(UUID.randomUUID().toString())
            .setFilename(UUID.randomUUID().toString())
            .setSize(10L)
            .setUser(user);
    return imageRepository.save(image);
  }

  private void assertImagesEquals(Image image, Image otherImage) {
    assertEquals(image.getImageId(), otherImage.getImageId());
    assertEquals(image.getFilename(), otherImage.getFilename());
    assertEquals(image.getSize(), otherImage.getSize());
    assertEquals(image.getUser().getUsername(), otherImage.getUser().getUsername());
  }
}

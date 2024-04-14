package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.domain.Image;
import org.example.exceptions.ForbiddenException;
import org.example.exceptions.ImageNotFoundException;
import org.example.repositories.ImageRepository;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final MinioService minioService;
    private final UserService userService;
    private final ImageRepository imageRepository;

    private Image getImage(String imageId) {
        return imageRepository.findById(imageId).orElseThrow(() -> new ImageNotFoundException(imageId));
    }

    public List<Image> getUserImages(String username) {
        var user = userService.getUserByUsername(username);

        return imageRepository.findAll(Example.of(new Image().setUser(user)));
    }

    public byte[] downloadImage(String imageId, String authorUsername) throws Exception {
        var image = getImage(imageId);
        var user = userService.getUserByUsername(authorUsername);
        checkUserAccess(user.getId(), image);

        return minioService.downloadImage(image.getImageId());
    }

    public Image uploadImage(MultipartFile file, String authorUsername) throws Exception {
        var user = userService.getUserByUsername(authorUsername);
        var image = minioService.uploadImage(file);

        image.setUser(user);
        imageRepository.save(image);

        return image;
    }

    public void deleteImage(String imageId, String authorUsername) throws Exception {
        var image = getImage(imageId);
        var user = userService.getUserByUsername(authorUsername);
        checkUserAccess(user.getId(), image);

        imageRepository.deleteById(imageId);
        minioService.deleteImage(image.getImageId());
    }

    private void checkUserAccess(int currentUserId, Image image) {
        var imageOwnerId = image.getUser().getId();

        if (!imageOwnerId.equals(currentUserId)) {
            throw new ForbiddenException();
        }
    }
}

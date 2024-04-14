package org.example.mapper;

import org.example.dtos.GetImagesResponse;
import org.example.dtos.UploadImageResponse;
import org.example.domain.Image;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    UploadImageResponse imageToUploadImageResponse(Image image);

    default GetImagesResponse imagesToGetImagesResponse(List<Image> images) {
        return new GetImagesResponse(images.toArray(new Image[0]));
    }
}

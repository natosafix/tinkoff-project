package org.example.mapper;

import org.example.domain.ImageFiltersRequest;
import org.example.dtos.ApplyImageFiltersResponse;
import org.example.dtos.GetModifiedImageByRequestIdResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ImageFiltersRequestMapper {

    ApplyImageFiltersResponse imageFiltersRequestToApplyImageFiltersResponse(ImageFiltersRequest imageFiltersRequest);

    @Mapping(target = "imageId", source = "imageFiltersRequest", qualifiedByName = "getActualImageId")
    GetModifiedImageByRequestIdResponse imageFiltersRequestToGetModifiedImageByRequestId(ImageFiltersRequest imageFiltersRequest);

    @Named("getActualImageId")
    default String getActualImageId(ImageFiltersRequest imageFiltersRequest) {
        if (imageFiltersRequest.getFilteredImage() == null) {
            return imageFiltersRequest.getSourceImage().getImageId();
        }

        return imageFiltersRequest.getFilteredImage().getImageId();
    }
}

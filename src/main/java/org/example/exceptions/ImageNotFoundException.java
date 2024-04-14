package org.example.exceptions;

public class ImageNotFoundException extends BaseNotFoundException {
    public ImageNotFoundException(String id) {
        super("Не найдена картинка с id=" + id);
    }
}

package org.example.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseNotFoundException extends RuntimeException {
    public BaseNotFoundException(String reason) {
        super(reason);
    }
}

package org.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Недостаточно прав для использования ресурса")
public class ForbiddenException extends RuntimeException {
}

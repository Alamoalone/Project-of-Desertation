package lt.codeacademy.eshop.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lt.codeacademy.eshop.user.dto.UserDto;

import java.util.Objects;

public class PasswordMatchValidator implements ConstraintValidator<RepeatPassword, UserDto> {

    @Override
    public boolean isValid(UserDto userDto, ConstraintValidatorContext context) {
        return Objects.nonNull(userDto.getPassword()) && userDto.getPassword().equals(userDto.getRepeatPassword());
    }
}

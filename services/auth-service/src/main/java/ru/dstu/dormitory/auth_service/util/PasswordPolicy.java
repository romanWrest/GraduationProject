package ru.dstu.dormitory.auth_service.util;

import lombok.experimental.UtilityClass;
import ru.dstu.dormitory.auth_service.exception.WeakPasswordException;

import java.util.regex.Pattern;

@UtilityClass
public class PasswordPolicy {

    private static final Pattern LETTER = Pattern.compile(".*[A-Za-zА-Яа-яЁё].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final int MIN_LENGTH = 8;

    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new WeakPasswordException("Пароль должен быть не короче %d символов".formatted(MIN_LENGTH));
        }
        if (!LETTER.matcher(password).matches() || !DIGIT.matcher(password).matches()) {
            throw new WeakPasswordException("Пароль должен содержать как минимум одну букву и одну цифру");
        }
    }
}

package ru.dstu.dormitory.auth_service.util;

import org.junit.jupiter.api.Test;
import ru.dstu.dormitory.auth_service.exception.WeakPasswordException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void rejects_short() {
        assertThatThrownBy(() -> PasswordPolicy.validate("Ab1"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejects_null() {
        assertThatThrownBy(() -> PasswordPolicy.validate(null))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejects_no_digit() {
        assertThatThrownBy(() -> PasswordPolicy.validate("Password"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejects_no_letter() {
        assertThatThrownBy(() -> PasswordPolicy.validate("12345678"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void accepts_valid() {
        assertThatCode(() -> PasswordPolicy.validate("Password12"))
                .doesNotThrowAnyException();
    }
}

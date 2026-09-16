package ru.dstu.dormitory.auth_service.aspect;

import org.junit.jupiter.api.Test;
import ru.dstu.dormitory.auth_service.web.dto.request.LoginRequest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultArgMaskerTest {

    private final DefaultArgMasker masker = new DefaultArgMasker();

    @Test
    void null_returns_null() {
        assertThat(masker.mask("x", null)).isNull();
    }

    @Test
    void string_is_masked() {
        assertThat(masker.mask("password", "secret")).isEqualTo("***");
    }

    @Test
    void uuid_is_masked() {
        assertThat(masker.mask("id", UUID.randomUUID())).isEqualTo("***");
    }

    @Test
    void instant_is_masked() {
        assertThat(masker.mask("at", Instant.now())).isEqualTo("***");
    }

    @Test
    void enum_is_masked() {
        assertThat(masker.mask("e", State.A)).isEqualTo("***");
    }

    @Test
    void object_uses_classname_placeholder() {
        Object masked = masker.mask("req", new LoginRequest("a@b.c", "p"));
        assertThat(masked).isEqualTo("<LoginRequest:masked>");
    }

    enum State { A, B }
}

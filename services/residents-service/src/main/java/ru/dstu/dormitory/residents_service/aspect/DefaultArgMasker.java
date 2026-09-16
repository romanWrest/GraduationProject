package ru.dstu.dormitory.residents_service.aspect;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * Реализация по умолчанию {@link ArgMasker}.
 * <ul>
 *     <li>{@code null} → {@code null}</li>
 *     <li>Строки и примитивы → {@code "***"}</li>
 *     <li>DTO/объекты → {@code "<ClassName:masked>"} (не сериализуется)</li>
 * </ul>
 */
@Component
public class DefaultArgMasker implements ArgMasker {

    private static final String MASK = "***";

    @Override
    public Object mask(String paramName, Object value) {
        if (value == null) {
            return null;
        }
        if (isScalar(value)) {
            return MASK;
        }
        return "<%s:masked>".formatted(value.getClass().getSimpleName());
    }

    private boolean isScalar(Object value) {
        return value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof UUID
                || value instanceof Temporal
                || value instanceof BigDecimal
                || value instanceof BigInteger
                || value.getClass().isPrimitive()
                || value.getClass().isEnum();
    }
}

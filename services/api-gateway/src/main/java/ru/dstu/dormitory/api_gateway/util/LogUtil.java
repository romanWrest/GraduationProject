package ru.dstu.dormitory.api_gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.dstu.dormitory.api_gateway.aspect.ArgMasker;
import ru.dstu.dormitory.api_gateway.aspect.annotation.LogMethod;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Утилиты форматирования объектов для логирования.
 */
@Slf4j
@UtilityClass
public class LogUtil {

    private static final ObjectWriter PRETTY_WRITER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .writerWithDefaultPrettyPrinter();

    public String prettifyPOJO(Object object) {
        try {
            return PRETTY_WRITER.writeValueAsString(object);
        } catch (Exception e) {
            log.warn("Ошибка сериализации {}: {}", object.getClass().getName(), e.getMessage());
            return object.toString();
        }
    }

    public String buildPrettyArgs(Object[] args, String[] paramNames,
                                  LogMethod ann, ArgMasker masker) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        if (paramNames == null || paramNames.length != args.length) {
            return prettifyPOJO(args);
        }

        Set<String> logSet = Set.of(ann.logArgs());
        Set<String> maskSet = Set.of(ann.maskArgs());
        boolean filterByLogArgs = !logSet.isEmpty();

        Map<String, Object> safeArgs = new LinkedHashMap<>();

        for (int i = 0; i < args.length; i++) {
            String name = paramNames[i];

            if (maskSet.contains(name)) {
                safeArgs.put(name, masker.mask(name, args[i]));
            } else if (!filterByLogArgs || logSet.contains(name)) {
                safeArgs.put(name, args[i]);
            }
        }

        return prettifyPOJO(safeArgs);
    }
}

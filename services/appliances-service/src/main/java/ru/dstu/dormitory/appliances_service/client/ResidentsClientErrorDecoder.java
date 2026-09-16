package ru.dstu.dormitory.appliances_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.dstu.dormitory.appliances_service.exception.ResidentNotEnrolledException;

/**
 * Декодер, который различает «не найден» (404) и «недоступен» (5xx / прочее) для residents-service.
 * 404 — это бизнес-валидация (жилец не заселён), а не сбой сервиса, поэтому fallback на 404 срабатывать
 * не должен. На 5xx и сетевых ошибках возвращаем стандартное исключение Feign и даём fallback'у
 * отработать; на 404 — бросаем доменное исключение.
 */
@Slf4j
public class ResidentsClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            log.debug("residents-service 404 на {}, трактуем как ResidentNotEnrolled", methodKey);
            return new ResidentNotEnrolledException(
                    "Жилец не заселён в общежитие. Обратитесь к коменданту.");
        }
        return defaultDecoder.decode(methodKey, response);
    }
}

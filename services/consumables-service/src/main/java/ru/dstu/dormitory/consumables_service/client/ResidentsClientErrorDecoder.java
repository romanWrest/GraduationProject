package ru.dstu.dormitory.consumables_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.dstu.dormitory.consumables_service.exception.ResidentNotEnrolledException;

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

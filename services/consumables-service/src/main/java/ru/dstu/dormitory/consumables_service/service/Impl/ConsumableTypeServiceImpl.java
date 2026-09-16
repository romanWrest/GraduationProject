package ru.dstu.dormitory.consumables_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.consumables_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableTypeRepository;
import ru.dstu.dormitory.consumables_service.exception.ConsumableTypeNotFoundException;
import ru.dstu.dormitory.consumables_service.exception.InvalidStockOperationException;
import ru.dstu.dormitory.consumables_service.service.ConsumableTypeService;
import ru.dstu.dormitory.consumables_service.util.LogPatterns;
import ru.dstu.dormitory.consumables_service.web.dto.request.CreateConsumableTypeRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.UpdateConsumableTypeRequest;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumableTypeServiceImpl implements ConsumableTypeService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final ConsumableTypeRepository typeRepository;

    @Override
    @LogMethod
    @Transactional
    public ConsumableType create(CreateConsumableTypeRequest request) {
        if (typeRepository.existsByName(request.name())) {
            throw new InvalidStockOperationException(
                    "Тип расходника с таким названием уже существует: " + request.name());
        }
        int threshold = request.lowStockThreshold() != null
                ? request.lowStockThreshold()
                : DEFAULT_LOW_STOCK_THRESHOLD;

        ConsumableType type = ConsumableType.builder()
                .name(request.name())
                .unit(request.unit())
                .stock(request.stock())
                .lowStockThreshold(threshold)
                .build();

        ConsumableType saved = typeRepository.save(type);
        log.info(LogPatterns.CONSUMABLE_TYPE_CREATED, saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @LogMethod
    @Transactional
    public ConsumableType update(UUID id, UpdateConsumableTypeRequest request) {
        ConsumableType type = getById(id);
        if (request.name() != null && !request.name().equals(type.getName())) {
            if (typeRepository.existsByName(request.name())) {
                throw new InvalidStockOperationException(
                        "Тип расходника с таким названием уже существует: " + request.name());
            }
            type.setName(request.name());
        }
        if (request.lowStockThreshold() != null) {
            type.setLowStockThreshold(request.lowStockThreshold());
        }
        ConsumableType saved = typeRepository.save(type);
        log.info(LogPatterns.CONSUMABLE_TYPE_UPDATED, saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumableType getById(UUID id) {
        return typeRepository.findById(id)
                .orElseThrow(() -> new ConsumableTypeNotFoundException(
                        "Тип расходника не найден: id=" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableType> listAll() {
        return typeRepository.findAll();
    }
}

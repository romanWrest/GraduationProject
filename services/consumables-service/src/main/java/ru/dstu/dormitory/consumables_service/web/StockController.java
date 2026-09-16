package ru.dstu.dormitory.consumables_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dstu.dormitory.consumables_service.mapper.ConsumableMapper;
import ru.dstu.dormitory.consumables_service.service.StockService;
import ru.dstu.dormitory.consumables_service.web.dto.response.StockItemDto;

import java.util.List;

@Tag(name = "Stock", description = "Текущие остатки расходников на складе")
@RestController
@RequestMapping("/api/v1/consumables/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final ConsumableMapper mapper;

    @Operation(summary = "Получить остатки по всем типам (PROPERTY_MANAGER, ADMIN)")
    @GetMapping
    @PreAuthorize("hasAnyRole('PROPERTY_MANAGER', 'ADMIN')")
    public ResponseEntity<List<StockItemDto>> stock() {
        return ResponseEntity.ok(mapper.toStockItemList(stockService.listAll()));
    }
}

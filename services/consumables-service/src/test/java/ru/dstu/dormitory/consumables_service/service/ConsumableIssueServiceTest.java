package ru.dstu.dormitory.consumables_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.consumables_service.client.ResidentsFacade;
import ru.dstu.dormitory.consumables_service.client.dto.ResidentDto;
import ru.dstu.dormitory.consumables_service.domain.enums.ConsumableUnit;
import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.enums.ReturnCondition;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.domain.repo.ConsumableIssueRepository;
import ru.dstu.dormitory.consumables_service.exception.ConsumableIssueNotFoundException;
import ru.dstu.dormitory.consumables_service.exception.IssueAlreadyReturnedException;
import ru.dstu.dormitory.consumables_service.service.Impl.ConsumableIssueServiceImpl;
import ru.dstu.dormitory.consumables_service.service.event.ConsumableIssuedEvent;
import ru.dstu.dormitory.consumables_service.service.event.ConsumableReturnedEvent;
import ru.dstu.dormitory.consumables_service.service.event.EventPublisher;
import ru.dstu.dormitory.consumables_service.service.event.EventTypes;
import ru.dstu.dormitory.consumables_service.web.dto.request.IssueRequest;
import ru.dstu.dormitory.consumables_service.web.dto.request.ReturnRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumableIssueServiceTest {

    @Mock
    private ConsumableIssueRepository issueRepository;
    @Mock
    private StockService stockService;
    @Mock
    private ResidentsFacade residentsFacade;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ConsumableIssueServiceImpl service;

    private UUID typeId;
    private UUID residentId;
    private UUID userId;
    private UUID actorId;
    private ConsumableType type;
    private ResidentDto resident;

    @BeforeEach
    void setUp() {
        typeId = UUID.randomUUID();
        residentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        type = ConsumableType.builder()
                .id(typeId).name("Комплект постельного белья")
                .unit(ConsumableUnit.SET).stock(20).lowStockThreshold(5).build();
        resident = new ResidentDto(residentId, userId, "STUDENT", null, null, null,
                null, null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Выдача: уменьшает stock через StockService и публикует ConsumableIssued")
    void issue_publishesEvent() {
        IssueRequest request = new IssueRequest(residentId, typeId, 2, "выдача");
        when(residentsFacade.requireResident(residentId)).thenReturn(resident);
        when(stockService.adjustStock(eq(typeId), eq(-2), any(), eq(actorId))).thenReturn(type);
        when(issueRepository.save(any(ConsumableIssue.class))).thenAnswer(inv -> {
            ConsumableIssue arg = inv.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(UUID.randomUUID());
            }
            return arg;
        });

        ConsumableIssue issue = service.issue(request, actorId);

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ISSUED);
        assertThat(issue.getQuantity()).isEqualTo(2);
        assertThat(issue.getResidentId()).isEqualTo(residentId);
        assertThat(issue.getUserId()).isEqualTo(userId);
        verify(eventPublisher).publish(eq(EventTypes.AGGREGATE_ISSUE), any(),
                eq(EventTypes.CONSUMABLE_ISSUED), any(ConsumableIssuedEvent.class));
    }

    @Test
    @DisplayName("Возврат OK — увеличивает stock и публикует ConsumableReturned")
    void returnOk_increasesStock() {
        UUID issueId = UUID.randomUUID();
        ConsumableIssue existing = ConsumableIssue.builder()
                .id(issueId).residentId(residentId).userId(userId)
                .type(type).quantity(2).status(IssueStatus.ISSUED).build();
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(existing));
        when(issueRepository.save(any(ConsumableIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsumableIssue result = service.returnIssue(issueId, new ReturnRequest(ReturnCondition.OK, null), actorId);

        assertThat(result.getStatus()).isEqualTo(IssueStatus.RETURNED);
        assertThat(result.getReturnCondition()).isEqualTo(ReturnCondition.OK);
        verify(stockService).adjustStock(eq(typeId), eq(2), any(), eq(actorId));
        verify(eventPublisher).publish(any(), any(),
                eq(EventTypes.CONSUMABLE_RETURNED), any(ConsumableReturnedEvent.class));
    }

    @Test
    @DisplayName("Возврат DAMAGED — также увеличивает stock")
    void returnDamaged_increasesStock() {
        UUID issueId = UUID.randomUUID();
        ConsumableIssue existing = ConsumableIssue.builder()
                .id(issueId).residentId(residentId).userId(userId)
                .type(type).quantity(1).status(IssueStatus.ISSUED).build();
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(existing));
        when(issueRepository.save(any(ConsumableIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        service.returnIssue(issueId, new ReturnRequest(ReturnCondition.DAMAGED, null), actorId);

        verify(stockService).adjustStock(eq(typeId), eq(1), any(), eq(actorId));
    }

    @Test
    @DisplayName("Возврат LOST — НЕ увеличивает stock")
    void returnLost_noStockChange() {
        UUID issueId = UUID.randomUUID();
        ConsumableIssue existing = ConsumableIssue.builder()
                .id(issueId).residentId(residentId).userId(userId)
                .type(type).quantity(1).status(IssueStatus.ISSUED).build();
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(existing));
        when(issueRepository.save(any(ConsumableIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsumableIssue result = service.returnIssue(issueId, new ReturnRequest(ReturnCondition.LOST, null), actorId);

        assertThat(result.getStatus()).isEqualTo(IssueStatus.RETURNED);
        assertThat(result.getReturnCondition()).isEqualTo(ReturnCondition.LOST);
        verify(stockService, never()).adjustStock(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Возврат уже возвращённой выдачи → IssueAlreadyReturnedException")
    void returnAlreadyReturned() {
        UUID issueId = UUID.randomUUID();
        ConsumableIssue existing = ConsumableIssue.builder()
                .id(issueId).residentId(residentId).userId(userId)
                .type(type).quantity(1).status(IssueStatus.RETURNED).build();
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.returnIssue(issueId,
                new ReturnRequest(ReturnCondition.OK, null), actorId))
                .isInstanceOf(IssueAlreadyReturnedException.class);
        verify(stockService, never()).adjustStock(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Возврат несуществующей выдачи → ConsumableIssueNotFoundException")
    void returnNotFound() {
        UUID issueId = UUID.randomUUID();
        when(issueRepository.findById(issueId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.returnIssue(issueId,
                new ReturnRequest(ReturnCondition.OK, null), actorId))
                .isInstanceOf(ConsumableIssueNotFoundException.class);
    }
}

package ru.dstu.dormitory.consumables_service.service.event;

public final class EventTypes {

    public static final String AGGREGATE_ISSUE = "consumable_issue";
    public static final String AGGREGATE_TYPE = "consumable_type";

    public static final String CONSUMABLE_ISSUED = "ConsumableIssued";
    public static final String CONSUMABLE_RETURNED = "ConsumableReturned";
    public static final String CONSUMABLE_STOCK_LOW = "ConsumableStockLow";
    public static final String CONSUMABLE_STOCK_OUT = "ConsumableStockOut";
    public static final String CONSUMABLE_RETURN_REQUIRED = "ConsumableReturnRequired";

    private EventTypes() {
    }
}

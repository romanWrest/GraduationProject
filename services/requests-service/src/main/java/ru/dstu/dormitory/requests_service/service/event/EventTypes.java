package ru.dstu.dormitory.requests_service.service.event;

public final class EventTypes {

    public static final String AGGREGATE_REQUEST = "request";

    public static final String REQUEST_CREATED = "RequestCreated";
    public static final String REQUEST_STATUS_CHANGED = "RequestStatusChanged";
    public static final String REQUEST_ASSIGNED = "RequestAssigned";
    public static final String REQUEST_CLOSED = "RequestClosed";

    private EventTypes() {
    }
}

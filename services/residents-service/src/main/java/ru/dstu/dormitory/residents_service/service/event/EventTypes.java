package ru.dstu.dormitory.residents_service.service.event;

public final class EventTypes {

    public static final String AGGREGATE_RESIDENT = "resident";

    public static final String RESIDENT_ENROLLED = "ResidentEnrolled";
    public static final String RESIDENT_EVICTED = "ResidentEvicted";
    public static final String RESIDENT_MOVED = "ResidentMoved";

    private EventTypes() {
    }
}

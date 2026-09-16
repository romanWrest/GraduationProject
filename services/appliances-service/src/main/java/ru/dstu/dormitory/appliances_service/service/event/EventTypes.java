package ru.dstu.dormitory.appliances_service.service.event;

public final class EventTypes {

    public static final String AGGREGATE_APPLIANCE = "appliance";

    public static final String APPLIANCE_REGISTERED = "ApplianceRegistered";
    public static final String APPLIANCE_APPROVED = "ApplianceApproved";
    public static final String APPLIANCE_REJECTED = "ApplianceRejected";
    public static final String APPLIANCE_REVOKED = "ApplianceRevoked";

    private EventTypes() {
    }
}

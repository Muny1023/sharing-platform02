package org.example.cloudcommon;

/**
 * Payload used only by the Seata AT demonstration flow.
 */
public record SeataDemoRequest(String demoKey, String accountNote, String contentNote) {
}

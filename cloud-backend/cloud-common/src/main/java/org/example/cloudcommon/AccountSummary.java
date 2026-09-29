package org.example.cloudcommon;

/**
 * Minimal account projection used by other services for batched lookups.
 */
public record AccountSummary(Integer id, String username, String nickname, String avatar, String role) {
}

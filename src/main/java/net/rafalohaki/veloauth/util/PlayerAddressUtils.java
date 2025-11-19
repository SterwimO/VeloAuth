package net.rafalohaki.veloauth.util;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Utility class for extracting IP addresses from players and connection events.
 * Provides consistent address extraction across different event types.
 * <p>
 * Thread-safe: stateless utility methods.
 */
public final class PlayerAddressUtils {

    private PlayerAddressUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts IP address from player as string.
     * Handles null addresses gracefully by returning "unknown".
     *
     * @param player Player to extract IP from
     * @return IP address string or "unknown" if unavailable
     */
    @javax.annotation.Nonnull
    public static String getPlayerIp(@javax.annotation.Nullable Player player) {
        if (player == null) {
            return StringConstants.UNKNOWN;
        }
        
        var address = player.getRemoteAddress();
        if (address == null) {
            org.slf4j.LoggerFactory.getLogger(PlayerAddressUtils.class)
                .warn("Player {} has null remote address", player.getUsername());
            return StringConstants.UNKNOWN;
        }
        
        if (address instanceof InetSocketAddress inetAddress) {
            String hostAddress = inetAddress.getAddress().getHostAddress();
            return hostAddress != null ? hostAddress : StringConstants.UNKNOWN;
        }
        return StringConstants.UNKNOWN;
    }

    /**
     * Extracts InetAddress from player.
     * Handles null addresses gracefully by returning null.
     *
     * @param player Player to extract InetAddress from
     * @return InetAddress or null if unavailable
     */
    @javax.annotation.Nullable
    public static InetAddress getPlayerAddress(@javax.annotation.Nullable Player player) {
        if (player == null) {
            return null;
        }
        
        var address = player.getRemoteAddress();
        if (address == null) {
            org.slf4j.LoggerFactory.getLogger(PlayerAddressUtils.class)
                .warn("Player {} has null remote address", player.getUsername());
            return null;
        }
        
        if (address instanceof InetSocketAddress inetAddress) {
            return inetAddress.getAddress();
        }
        return null;
    }

    /**
     * Extracts InetAddress from PreLoginEvent.
     * PreLoginEvent doesn't have a Player object yet, so we extract from connection data.
     * Handles null addresses gracefully by returning null.
     *
     * @param event PreLoginEvent to extract address from
     * @return InetAddress or null if unavailable
     */
    @javax.annotation.Nullable
    public static InetAddress getAddressFromPreLogin(@javax.annotation.Nullable PreLoginEvent event) {
        if (event == null) {
            return null;
        }
        
        try {
            var connection = event.getConnection();
            if (connection == null) {
                org.slf4j.LoggerFactory.getLogger(PlayerAddressUtils.class)
                    .debug("PreLoginEvent has null connection");
                return null;
            }
            
            var address = connection.getRemoteAddress();
            if (address == null) {
                org.slf4j.LoggerFactory.getLogger(PlayerAddressUtils.class)
                    .warn("PreLoginEvent connection has null remote address for user: {}", 
                        event.getUsername());
                return null;
            }
            
            if (address instanceof InetSocketAddress inetAddress) {
                return inetAddress.getAddress();
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PlayerAddressUtils.class)
                .error("Error extracting address from PreLoginEvent", e);
        }
        return null;
    }

    /**
     * Checks if player has a valid remote address.
     * Handles null player and null address gracefully.
     *
     * @param player Player to check
     * @return true if player has valid address, false otherwise
     */
    public static boolean hasValidAddress(@javax.annotation.Nullable Player player) {
        if (player == null) {
            return false;
        }
        
        var address = player.getRemoteAddress();
        return address instanceof InetSocketAddress;
    }
}

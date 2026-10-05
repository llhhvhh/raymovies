package com.yourname.dxdweapons.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlessingFlightHandler {
    private static final Map<UUID, Long> flightEndTime = new HashMap<>();

    public static void enableFlight(ServerPlayer player, int level) {
        double durationSeconds = 5.0 + Math.floor((level - 10) / 10.0) * 0.1;
        int durationTicks = Math.max(1, (int) (durationSeconds * 20));
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        flightEndTime.put(player.getUUID(), player.serverLevel().getGameTime() + durationTicks);
    }

    public static void disableFlight(ServerPlayer player) {
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        flightEndTime.remove(player.getUUID());
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Long endTime = flightEndTime.get(uuid);
        if (endTime != null && player.serverLevel().getGameTime() >= endTime) {
            disableFlight(player);
        }
    }

    public static boolean hasFlight(UUID uuid) {
        return flightEndTime.containsKey(uuid);
    }
}

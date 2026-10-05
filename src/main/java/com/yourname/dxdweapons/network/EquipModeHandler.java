package com.yourname.dxdweapons.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipModeHandler {
    private static final Map<UUID, Boolean> equipMode = new HashMap<>();

    public static boolean isEquipMode(UUID uuid) {
        return equipMode.getOrDefault(uuid, false);
    }

    public static void setEquipMode(UUID uuid, boolean mode) {
        if (mode) {
            equipMode.put(uuid, true);
        } else {
            equipMode.remove(uuid);
        }
    }
}

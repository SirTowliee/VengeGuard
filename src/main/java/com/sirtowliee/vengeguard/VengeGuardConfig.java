package com.sirtowliee.vengeguard;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("vengeguard")
public interface VengeGuardConfig extends Config
{
    @ConfigItem(
        keyName = "debugLogging",
        name = "Debug logging",
        description = "Logs Vengeance Other menu-entry details to the RuneLite log."
    )
    default boolean debugLogging()
    {
        return false;
    }

    @ConfigItem(
        keyName = "testRemoval",
        name = "Test removing Vengeance options",
        description = "Temporarily removes all Vengeance Other player options instead of sorting them. For diagnosis only."
    )
    default boolean testRemoval()
    {
        return false;
    }
}

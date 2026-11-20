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
        description = "Log Vengeance Other menu detection and target classifications."
    )
    default boolean debugLogging()
    {
        return true;
    }

    @ConfigItem(
        keyName = "testMenuRemoval",
        name = "Test menu removal",
        description = "Temporarily hide Vengeance Other entries for venged party members and Ironmen instead of leaving them at the bottom."
    )
    default boolean testMenuRemoval()
    {
        return false;
    }
}

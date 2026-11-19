package com.sirtowliee.vengeguard;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VengeGuardPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(VengeGuardPlugin.class);
        RuneLite.main(args);
    }
}

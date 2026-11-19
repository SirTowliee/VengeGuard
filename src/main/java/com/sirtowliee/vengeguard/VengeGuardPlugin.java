package com.sirtowliee.vengeguard;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.IconID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.party.PartyPlugin;
import net.runelite.client.util.Text;

@PluginDependency(PartyPlugin.class)
@PluginDescriptor(
    name = "VengeGuard",
    description = "Hides the Vengeance Other cast option for Ironman players and party members who already have Vengeance active.",
    tags = {"vengeance", "magic", "party", "ironman"}
)
public class VengeGuardPlugin extends Plugin
{
    private static final String VENGEANCE_OTHER = "Vengeance Other";

    private static final IconID[] IRONMAN_ICONS = {
        IconID.IRONMAN,
        IconID.ULTIMATE_IRONMAN,
        IconID.HARDCORE_IRONMAN,
        IconID.GROUP_IRONMAN,
        IconID.HARDCORE_GROUP_IRONMAN,
        IconID.UNRANKED_GROUP_IRONMAN
    };

    @Inject
    private Client client;

    @Inject
    private PartyService partyService;

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        MenuEntry entry = event.getMenuEntry();

        if (entry == null || !isVengeanceOtherEntry(event))
        {
            return;
        }

        // RuneLite includes account-type icons in the player target as Jagex image tags.
        if (isIronmanTarget(event.getTarget()))
        {
            client.getMenu().removeMenuEntry(entry);
            return;
        }

        if (entry.getType() != MenuAction.WIDGET_TARGET_ON_PLAYER)
        {
            return;
        }

        Player target = entry.getPlayer();
        if (target == null || target.getName() == null || !partyService.isInParty())
        {
            return;
        }

        PartyMember member = partyService.getMemberByDisplayName(target.getName());
        if (member != null && member.isVengeanceActive())
        {
            client.getMenu().removeMenuEntry(entry);
        }
    }

    private boolean isVengeanceOtherEntry(MenuEntryAdded event)
    {
        String optionAndTarget = Text.removeTags(
            String.valueOf(event.getOption()) + " " + String.valueOf(event.getTarget())
        );

        return optionAndTarget.toLowerCase(java.util.Locale.ROOT)
            .contains(VENGEANCE_OTHER.toLowerCase(java.util.Locale.ROOT));
    }

    private boolean isIronmanTarget(String menuTarget)
    {
        if (menuTarget == null || menuTarget.isEmpty())
        {
            return false;
        }

        for (IconID icon : IRONMAN_ICONS)
        {
            if (menuTarget.contains(icon.toString()))
            {
                return true;
            }
        }

        return false;
    }
}

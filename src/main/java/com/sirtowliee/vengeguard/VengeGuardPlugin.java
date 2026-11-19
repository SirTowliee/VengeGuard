package com.sirtowliee.vengeguard;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.IconID;
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
    description = "Hides Cast Vengeance Other on Ironman players and party members with Vengeance active.",
    tags = {"vengeance", "magic", "party", "ironman"}
)
public class VengeGuardPlugin extends Plugin
{
    private static final String VENGEANCE_OTHER_OPTION = "Cast Vengeance Other";

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
        if (!VENGEANCE_OTHER_OPTION.equalsIgnoreCase(event.getOption()))
        {
            return;
        }

        // Ironman account icons are included in the menu target as Jagex image tags.
        if (isIronmanTarget(event.getTarget()))
        {
            removeEntry(event.getMenuEntry());
            return;
        }

        Player target = event.getMenuEntry().getPlayer();
        if (target == null || !partyService.isInParty())
        {
            return;
        }

        String targetName = Text.removeTags(Text.toJagexName(target.getName()));
        PartyMember member = partyService.getMemberByDisplayName(targetName);
        if (member != null && member.isVengeanceActive())
        {
            removeEntry(event.getMenuEntry());
        }
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

    private void removeEntry(MenuEntry entry)
    {
        if (entry != null)
        {
            client.getMenu().removeMenuEntry(entry);
        }
    }
}

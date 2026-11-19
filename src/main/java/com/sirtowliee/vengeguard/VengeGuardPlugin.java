package com.sirtowliee.vengeguard;

import javax.inject.Inject;
import net.runelite.api.Client;
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
    description = "Hides the Vengeance Other cast option for party members who already have Vengeance active.",
    tags = {"vengeance", "magic", "party"}
)
public class VengeGuardPlugin extends Plugin
{
    private static final String VENGEANCE_OTHER = "Vengeance Other";

    @Inject
    private Client client;

    @Inject
    private PartyService partyService;

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        MenuEntry entry = event.getMenuEntry();

        // When a spell is selected, the menu option is normally "Cast";
        // the spell name is part of the target (for example, "Vengeance Other -> player").
        if (entry == null
            || entry.getType() != MenuAction.WIDGET_TARGET_ON_PLAYER
            || !isVengeanceOtherEntry(event))
        {
            return;
        }

        Player target = entry.getPlayer();
        if (target == null || !partyService.isInParty() || target.getName() == null)
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

        return optionAndTarget.toLowerCase(java.util.Locale.ROOT).contains(VENGEANCE_OTHER.toLowerCase(java.util.Locale.ROOT));
    }
}

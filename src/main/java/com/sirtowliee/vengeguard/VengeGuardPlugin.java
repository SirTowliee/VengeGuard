package com.sirtowliee.vengeguard;

import com.google.inject.Provides;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.IconID;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.vars.Varbits;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.PartyDataService;
import net.runelite.client.party.PartyDataType;
import net.runelite.client.party.messages.PartyData;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.util.Text;

@PluginDescriptor(
    name = "VengeGuard",
    description = "Hides Cast Vengeance Other when the target cannot or should not receive it.",
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

    @Inject
    private PartyDataService partyDataService;

    private final Map<Long, Boolean> vengeanceByMemberId = new ConcurrentHashMap<>();

    @Override
    protected void startUp()
    {
        partyDataService.subscribe(this, PartyDataType.VENGEANCE);
    }

    @Override
    protected void shutDown()
    {
        partyDataService.unsubscribe(this, PartyDataType.VENGEANCE);
        vengeanceByMemberId.clear();
    }

    @Subscribe
    public void onPartyData(PartyData data)
    {
        Boolean vengeanceActive = data.getVengeanceActive();
        if (vengeanceActive != null)
        {
            vengeanceByMemberId.put(data.getMemberId(), vengeanceActive);
        }
    }

    @Subscribe
    public void onPartyChanged(PartyChanged event)
    {
        vengeanceByMemberId.clear();
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!VENGEANCE_OTHER_OPTION.equalsIgnoreCase(event.getOption()))
        {
            return;
        }

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
        net.runelite.client.party.PartyMember member =
            partyService.getMemberByDisplayName(targetName);

        if (member == null)
        {
            return;
        }

        if (Boolean.TRUE.equals(vengeanceByMemberId.get(member.getMemberId())))
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

    @Provides
    VengeGuardConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(VengeGuardConfig.class);
    }
}

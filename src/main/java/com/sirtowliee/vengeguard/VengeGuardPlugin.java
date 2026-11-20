package com.sirtowliee.vengeguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.IconID;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.config.ConfigManager;
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
    description = "Prioritises Vengeance Other targets by party status, active Vengeance and Ironman status.",
    tags = {"vengeance", "magic", "party", "ironman"}
)
@Slf4j
public class VengeGuardPlugin extends Plugin
{
    private static final String VENGEANCE_OTHER = "vengeance other";

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
    private VengeGuardConfig config;

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!config.debugLogging())
        {
            return;
        }

        String option = clean(event.getOption());
        if (option.toLowerCase(java.util.Locale.ROOT).contains(VENGEANCE_OTHER))
        {
            MenuEntry entry = event.getMenuEntry();
            log.info("VengeGuard detected menu entry: option='{}', target='{}', type='{}', identifier={}, param0={}, param1={}, player='{}'",
                event.getOption(), event.getTarget(),
                entry == null ? null : entry.getType(),
                entry == null ? -1 : entry.getIdentifier(),
                entry == null ? -1 : entry.getParam0(),
                entry == null ? -1 : entry.getParam1(),
                entry == null || entry.getPlayer() == null ? null : entry.getPlayer().getName());
        }
    }

    @Subscribe
    public void onPostMenuSort(PostMenuSort event)
    {
        if (client.isMenuOpen())
        {
            return;
        }

        Menu menu = client.getMenu();
        MenuEntry[] entries = menu.getMenuEntries();
        List<Integer> vengeanceIndexes = new ArrayList<>();
        List<MenuEntry> vengeanceEntries = new ArrayList<>();

        for (int i = 0; i < entries.length; i++)
        {
            MenuEntry entry = entries[i];
            if (isVengeanceOtherEntry(entry))
            {
                vengeanceIndexes.add(i);
                vengeanceEntries.add(entry);
            }
        }

        if (config.debugLogging() && !vengeanceEntries.isEmpty())
        {
            log.info("VengeGuard menu sort: found {} Vengeance Other entries; partyActive={}, removalTest={}",
                vengeanceEntries.size(), partyService.isInParty(), config.testRemoval());
        }

        if (config.testRemoval())
        {
            if (!vengeanceEntries.isEmpty())
            {
                MenuEntry[] filtered = Arrays.stream(entries)
                    .filter(entry -> !isVengeanceOtherEntry(entry))
                    .toArray(MenuEntry[]::new);
                menu.setMenuEntries(filtered);

                if (config.debugLogging())
                {
                    log.info("VengeGuard removal test: removed {} of {} Vengeance Other entries; {} menu entries remain.",
                        vengeanceEntries.size(), entries.length, filtered.length);
                }
            }
            return;
        }

        if (vengeanceEntries.size() < 2)
        {
            return;
        }

        // Menu arrays are ordered bottom-to-top, so ascending priority puts
        // the highest-priority entries at the top of the visible menu.
        vengeanceEntries.sort(Comparator.comparingInt(this::getPriority));

        for (int i = 0; i < vengeanceIndexes.size(); i++)
        {
            entries[vengeanceIndexes.get(i)] = vengeanceEntries.get(i);
        }

        menu.setMenuEntries(entries);
    }

    private boolean isVengeanceOtherEntry(MenuEntry entry)
    {
        if (entry == null || entry.getType() != MenuAction.WIDGET_TARGET_ON_PLAYER)
        {
            return false;
        }

        return clean(entry.getOption()).toLowerCase(java.util.Locale.ROOT).contains(VENGEANCE_OTHER);
    }

    private int getPriority(MenuEntry entry)
    {
        // 3 = Ironman (bottom), 2 = venged party member,
        // 1 = unvenged non-party player, 0 = unvenged party member (top).
        if (isIronmanTarget(entry.getTarget()))
        {
            return 3;
        }

        Player target = entry.getPlayer();
        if (target == null || target.getName() == null || !partyService.isInParty())
        {
            return 1;
        }

        PartyMember member = partyService.getMemberByDisplayName(target.getName());
        if (member == null)
        {
            return 1;
        }

        return member.isVengeanceActive() ? 2 : 0;
    }

    private boolean isIronmanTarget(String menuTarget)
    {
        if (menuTarget == null || menuTarget.isEmpty())
        {
            return false;
        }

        return Arrays.stream(IRONMAN_ICONS).anyMatch(icon -> menuTarget.contains(icon.toString()));
    }

    private String clean(String value)
    {
        return Text.removeTags(String.valueOf(value));
    }
}

package com.sirtowliee.vengeguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.IconID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.party.PartyPlugin;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDependency(PartyPlugin.class)
@PluginDescriptor(
    name = "VengeGuard",
    description = "Prioritises Vengeance Other targets and can test hiding unavailable targets.",
    tags = {"vengeance", "magic", "party", "ironman"},
    configName = "vengeguard"
)
public class VengeGuardPlugin extends Plugin
{
    private static final Logger log = LoggerFactory.getLogger(VengeGuardPlugin.class);
    private static final String VENGEANCE_OTHER = "Vengeance Other";

    private static final int PRIORITY_UNVENGED_PARTY = 0;
    private static final int PRIORITY_UNVENGED_NON_PARTY = 1;
    private static final int PRIORITY_VENGED_PARTY = 2;
    private static final int PRIORITY_IRONMAN = 3;

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
        if (!config.debugLogging() || !isVengeanceOtherEntry(event.getOption(), event.getTarget()))
        {
            return;
        }

        MenuEntry entry = event.getMenuEntry();
        log.info("VengeGuard detected Vengeance Other entry: option='{}', target='{}', type={}, player={}",
            event.getOption(),
            event.getTarget(),
            entry == null ? "null" : entry.getType(),
            entry == null || entry.getPlayer() == null ? "null" : entry.getPlayer().getName());
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event)
    {
        MenuEntry[] originalEntries = event.getMenuEntries();
        if (originalEntries == null || originalEntries.length == 0)
        {
            return;
        }

        List<MenuEntry> vengeanceEntries = new ArrayList<>();
        List<Integer> vengeanceIndexes = new ArrayList<>();

        for (int i = 0; i < originalEntries.length; i++)
        {
            MenuEntry entry = originalEntries[i];
            if (entry != null && isVengeanceOtherEntry(entry.getOption(), entry.getTarget()))
            {
                int priority = getPriority(entry);
                String playerName = entry.getPlayer() == null ? "unknown" : entry.getPlayer().getName();

                if (config.debugLogging())
                {
                    log.info("VengeGuard menu classification: player='{}', priority={}, category={}",
                        playerName, priority, priorityName(priority));
                }

                if (config.testMenuRemoval()
                    && (priority == PRIORITY_VENGED_PARTY || priority == PRIORITY_IRONMAN))
                {
                    if (config.debugLogging())
                    {
                        log.info("VengeGuard removal test: removing '{}' ({}).", playerName, priorityName(priority));
                    }
                    continue;
                }

                vengeanceEntries.add(entry);
                vengeanceIndexes.add(i);
            }
        }

        if (vengeanceEntries.isEmpty())
        {
            if (config.debugLogging() && config.testMenuRemoval())
            {
                log.info("VengeGuard removal test: no eligible Vengeance Other entries were present.");
            }
            if (config.testMenuRemoval())
            {
                event.setMenuEntries(removeBlockedEntries(originalEntries));
            }
            return;
        }

        // MenuOpened arrays are ordered bottom-to-top: the last entry is displayed at the top.
        // Sorting priority descending therefore places the highest-priority targets at the top.
        vengeanceEntries.sort(Comparator.comparingInt(this::getPriority).reversed());

        MenuEntry[] reorderedEntries = originalEntries.clone();
        for (int i = 0; i < vengeanceIndexes.size(); i++)
        {
            reorderedEntries[vengeanceIndexes.get(i)] = vengeanceEntries.get(i);
        }

        if (config.testMenuRemoval())
        {
            reorderedEntries = removeBlockedEntries(reorderedEntries);
        }

        if (config.debugLogging())
        {
            log.info("VengeGuard reordered {} Vengeance Other entries. Removal test is {}.",
                vengeanceEntries.size(), config.testMenuRemoval() ? "ON" : "OFF");
        }

        event.setMenuEntries(reorderedEntries);
    }

    private MenuEntry[] removeBlockedEntries(MenuEntry[] entries)
    {
        return Arrays.stream(entries)
            .filter(entry -> entry == null
                || !isVengeanceOtherEntry(entry.getOption(), entry.getTarget())
                || (getPriority(entry) != PRIORITY_VENGED_PARTY && getPriority(entry) != PRIORITY_IRONMAN))
            .toArray(MenuEntry[]::new);
    }

    private int getPriority(MenuEntry entry)
    {
        if (isIronmanTarget(entry.getTarget()))
        {
            return PRIORITY_IRONMAN;
        }

        if (entry.getType() != MenuAction.WIDGET_TARGET_ON_PLAYER)
        {
            return PRIORITY_UNVENGED_NON_PARTY;
        }

        Player target = entry.getPlayer();
        if (target == null || target.getName() == null || !partyService.isInParty())
        {
            return PRIORITY_UNVENGED_NON_PARTY;
        }

        PartyMember member = partyService.getMemberByDisplayName(target.getName());
        if (member == null)
        {
            return PRIORITY_UNVENGED_NON_PARTY;
        }

        return member.isVengeanceActive()
            ? PRIORITY_VENGED_PARTY
            : PRIORITY_UNVENGED_PARTY;
    }

    private String priorityName(int priority)
    {
        switch (priority)
        {
            case PRIORITY_UNVENGED_PARTY:
                return "unvenged party member";
            case PRIORITY_UNVENGED_NON_PARTY:
                return "unvenged non-party player";
            case PRIORITY_VENGED_PARTY:
                return "venged party member";
            case PRIORITY_IRONMAN:
                return "ironman";
            default:
                return "unknown";
        }
    }

    private boolean isVengeanceOtherEntry(String option, String target)
    {
        String optionAndTarget = Text.removeTags(
            String.valueOf(option) + " " + String.valueOf(target)
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

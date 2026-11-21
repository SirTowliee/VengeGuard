# VengeGuard

VengeGuard improves the **Cast Vengeance Other** player-menu order and displays party members who do not currently have Vengeance active.

## Target priority

When multiple **Cast Vengeance Other** entries are present, VengeGuard orders them so the top of the menu prioritises:

1. Party members who do not currently have Vengeance active.
2. Players who are not in your RuneLite Party.
3. Party members who already have Vengeance active.
4. Ironman account types (Ironman, Ultimate Ironman, Hardcore Ironman, Group Ironman, Hardcore Group Ironman, and Unranked Group Ironman).

Only Vengeance Other entries are reordered; other menu entries are left in place.

## Unvenged party overlay

VengeGuard can display a movable overlay on the game screen listing party members who do not currently have Vengeance active. It updates using the RuneLite Party plugin's shared Vengeance status and displays **Everyone venged!** when all other logged-in party members are venged.

The overlay can be enabled or disabled with **Show unvenged party overlay** in VengeGuard's configuration. It is enabled by default.

## Optional removal test

Open VengeGuard's configuration to enable **Test menu removal**. When enabled, VengeGuard attempts to remove Vengeance Other entries for party members with active Vengeance and Ironman accounts instead of leaving them at the bottom.

This is a test setting and should be validated in-game before relying on it.

## Party requirement

Enable RuneLite's built-in **Party** plugin and be in the same party as other players for VengeGuard to use their shared Vengeance status. The Party plugin must be able to share the Vengeance data type.

## Debugging

**Debug logging** is enabled by default. It logs detected Vengeance Other entries, their classifications, and whether the removal test is enabled. These messages can help diagnose menu detection and ordering issues.

## Important notes

- VengeGuard only changes the local right-click menu; it does not cast spells or interact with the game automatically.
- Ironman detection relies on the account-type icon being present in the target text supplied by RuneLite for that menu entry.
- Party Vengeance status is only available for players in your current RuneLite Party and may be briefly stale between status updates.
- The overlay excludes your own character and lists other logged-in party members only.
- A successful build confirms that the code compiles; actual menu and overlay behaviour still requires in-game testing.

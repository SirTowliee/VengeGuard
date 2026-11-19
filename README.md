# VengeGuard

VengeGuard is a RuneLite plugin that removes the **Cast Vengeance Other** player-menu option in two situations:

- The target's player-menu name contains a RuneLite/Jagex Ironman account icon (Ironman, Ultimate Ironman, Hardcore Ironman, Group Ironman, Hardcore Group Ironman, or Unranked Group Ironman).
- The target is a member of your built-in RuneLite Party and RuneLite has reported that their Vengeance effect is active.

The option is left alone for other players.

## Party requirement

Enable RuneLite's built-in **Party** plugin and be in the same party as the other player for VengeGuard to use their shared Vengeance status. The Party plugin must be able to share the Vengeance data type.

## Important notes

- VengeGuard only changes the local right-click menu; it does not cast spells or interact with the game automatically.
- Ironman detection relies on the account-type icon being present in the target text supplied by RuneLite for that player-menu entry.
- Party Vengeance status is only available for players in your current RuneLite Party and may be briefly stale between status updates.

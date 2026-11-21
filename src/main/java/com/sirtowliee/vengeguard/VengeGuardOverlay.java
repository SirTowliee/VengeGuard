package com.sirtowliee.vengeguard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class VengeGuardOverlay extends Overlay
{
    private static final int PANEL_WIDTH = 190;

    private final PartyService partyService;
    private final VengeGuardConfig config;
    private final PanelComponent panel = new PanelComponent();

    @Inject
    public VengeGuardOverlay(PartyService partyService, VengeGuardConfig config)
    {
        this.partyService = partyService;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setMovable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showUnvengedOverlay() || !partyService.isInParty())
        {
            return null;
        }

        List<String> unvengedNames = new ArrayList<>();
        PartyMember localMember = partyService.getLocalMember();
        long localMemberId = localMember == null ? -1L : localMember.getMemberId();

        for (PartyMember member : partyService.getMembers())
        {
            if (!member.isLoggedIn() || member.getMemberId() == localMemberId)
            {
                continue;
            }

            if (!member.isVengeanceActive())
            {
                unvengedNames.add(member.getDisplayName());
            }
        }

        panel.getChildren().clear();
        panel.setPreferredLocation(new Point(10, 10));
        panel.setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        panel.setBorder(new Rectangle(6, 6, 6, 6));
        panel.setGap(new Point(0, 3));
        panel.setBackgroundColor(new Color(20, 20, 20, 210));

        panel.getChildren().add(TitleComponent.builder()
            .text("VengeGuard")
            .color(Color.WHITE)
            .preferredSize(new Dimension(PANEL_WIDTH - 12, 0))
            .build());

        if (unvengedNames.isEmpty())
        {
            panel.getChildren().add(LineComponent.builder()
                .left("Everyone venged!")
                .leftColor(new Color(80, 220, 110))
                .preferredSize(new Dimension(PANEL_WIDTH - 12, 0))
                .build());
        }
        else
        {
            panel.getChildren().add(LineComponent.builder()
                .left("Not venged (" + unvengedNames.size() + ")")
                .leftColor(new Color(255, 100, 100))
                .preferredSize(new Dimension(PANEL_WIDTH - 12, 0))
                .build());

            for (String name : unvengedNames)
            {
                panel.getChildren().add(LineComponent.builder()
                    .left(name)
                    .leftColor(Color.WHITE)
                    .preferredSize(new Dimension(PANEL_WIDTH - 12, 0))
                    .build());
            }
        }

        return panel.render(graphics);
    }
}

package ru.kelcuprum.waterplayer.frontend.gui.screens;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.frontend.localization.Music;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class LoadMusicScreen extends Screen {
    private final Screen parent;
    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;

    public LoadMusicScreen(Screen parent) {
        super(Localization.getText("waterplayer.name"));
        this.parent = parent;
    }

    @Override
    public void init() {
        initPanel();
        initList();
    }

    public void initPanel() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, Localization.getText("waterplayer.load"), true));
        EditBoxString request = new EditBoxString(x, 60, size, 20, designType, Localization.getText("waterplayer.load.url"));
        request.setMaxLength(Integer.MAX_VALUE);
        addRenderableWidget(request);

        addRenderableWidget(new Button(x, height - 80, size, 20, designType, Localization.getText("waterplayer.load.load"), (OnPress) -> {
            WaterPlayer.player.loadMusic(request.getValue(), true);
            onClose();
        }));
        addRenderableWidget(new Button(x, height - 55, size, 20, designType, Localization.getText("waterplayer.load.url.copy"), (OnPress) -> request.setValue(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", ""))));
        addRenderableWidget(new Button(x, height - 30, size, 20, designType, CommonComponents.GUI_CANCEL, (OnPress) -> onClose()));
    }

    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();

    public void initList() {
        widgets = new ArrayList<>();
        int x = 195;
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 8, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for (AbstractWidget widget : widgets) {
                if (widget.visible) {
                    widget.setY((int) (scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        addRenderableWidget(scroller);
        Queue<AudioTrack> queue = WaterPlayer.player.getTrackScheduler().queue;
        widgets.add(new TextBox(x, 5, width - 200, 20, Component.translatable(queue.isEmpty() ? "waterplayer.command.queue.blank" : "waterplayer.command.queue"), true));
        int pos = 1;
        if (!queue.isEmpty()) {
            for (AudioTrack track : WaterPlayer.player.getTrackScheduler().queue) {
                StringBuilder builder = new StringBuilder().append(pos).append(". ");
                if (!Music.isAuthorNull(track)) builder.append("«").append(Music.getAuthor(track)).append("» ");
                builder.append(Music.getTitle(track)).append(" ");
                builder.append(Music.getIsLive(track) ? WaterPlayer.localization.getLocalization("format.live") : StarScript.getTimestamp(Music.getDuration(track)));
                widgets.add(new TextBox(x, -10, width - 200, 10, Component.literal(builder.toString()), false, (s) -> {
                    if (track.getInfo().uri != null) Util.getPlatform().openUri(track.getInfo().uri);
                }));
                pos++;
            }
        }
        addRenderableWidgets(widgets);
    }

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        InterfaceUtils.renderLeftPanel(guiGraphics, 190, height);
    }

    @Override
    public void tick() {
        if (scroller != null) scroller.onScroll.accept(scroller);
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}

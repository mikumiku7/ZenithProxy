package com.zenith.feature.gui;

import com.zenith.event.client.ClientDisconnectEvent;
import com.zenith.event.client.ClientTickEvent;
import com.zenith.event.player.PlayerConnectionRemovedEvent;
import com.zenith.network.codec.PacketCodecRegistries;
import com.zenith.network.codec.PacketHandlerCodec;
import com.zenith.network.codec.PacketHandlerStateCodec;
import com.zenith.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundStartConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.rfresh2.EventConsumer.of;
import static com.zenith.Globals.EVENT_BUS;
import static com.zenith.Globals.SERVER_LOG;

public class GuiManager {
    public static final GuiManager INSTANCE = new GuiManager();
    private final Map<ServerSession, Gui> openGuiMap = new ConcurrentHashMap<>();

    private GuiManager() {
        EVENT_BUS.subscribe(this,
            of(ClientDisconnectEvent.class, e -> openGuiMap.clear()),
            of(PlayerConnectionRemovedEvent.class, e -> openGuiMap.remove(e.serverConnection())),
            of(ClientTickEvent.class, e -> openGuiMap.values().forEach(Gui::tick))
        );
        var codec = PacketHandlerCodec.serverBuilder()
            .setId("gui")
            .setPriority(5)
            .state(ProtocolState.GAME, PacketHandlerStateCodec.serverBuilder()
                .inbound(ServerboundContainerClickPacket.class, (p, s) -> {
                    var gui = openGuiMap.get(s);
                    if (gui == null) {
                        return p;
                    }
                    var containerClick = new ContainerClick(p.getSlot(), p.getActionType(), p.getActionParam());
                    gui.onClick(containerClick);
                    return null;
                })
                .inbound(ServerboundContainerClosePacket.class, (p, s) -> {
                    var gui = openGuiMap.get(s);
                    if (gui == null) {
                        return p;
                    }
                    gui.onClose();
                    openGuiMap.remove(s);
                    SERVER_LOG.info("Closed GUI {} for: {}", gui.hashCode(), s.getUsername());
                    return null;
                })
                .outbound(ClientboundLoginPacket.class, (p, s) -> {
                    openGuiMap.remove(s);
                    return p;
                })
                .outbound(ClientboundStartConfigurationPacket.class, (p, s) -> {
                    openGuiMap.remove(s);
                    return p;
                })
                .outbound(ClientboundRespawnPacket.class, (p, s) -> {
                    openGuiMap.remove(s);
                    return p;
                })
                .build())
            .build();
        PacketCodecRegistries.SERVER_REGISTRY.register(codec);
    }

    public void open(Gui gui) {
        SERVER_LOG.info("Opening GUI {} for: {}", gui.hashCode(), gui.session().getUsername());
        if (openGuiMap.containsKey(gui.session())) {
            close(gui.session());
        }
        openGuiMap.put(gui.session(), gui);
        gui.open();
    }

    public void close(Gui gui) {
        close(gui.session());
    }

    public void close(ServerSession session) {
        var g = openGuiMap.remove(session);
        if (g != null) {
            g.onClose();
        }
    }
}

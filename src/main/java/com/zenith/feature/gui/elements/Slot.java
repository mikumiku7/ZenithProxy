package com.zenith.feature.gui.elements;

import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface Slot {
    @Nullable ItemStack item();
}

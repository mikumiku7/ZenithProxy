package com.zenith.feature.gui.elements;

import com.zenith.mc.item.ItemRegistry;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@EqualsAndHashCode
@ToString
@NullMarked
public class ItemSlot implements Slot {
    private final @Nullable ItemStack item;

    public ItemSlot(@Nullable ItemStack item) {
        this.item = item;
    }

    public @Nullable ItemStack item() {
        return item;
    }

    public static ItemSlot empty() {
        return new ItemSlot(new ItemStack(ItemRegistry.AIR.id(), 1));
    }
}

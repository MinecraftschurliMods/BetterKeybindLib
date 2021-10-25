package com.github.minecraftschurlimods.betterkeybindlib;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public record ItemInHandKeyConflictContext(Either<Item, Tag<Item>> item) implements IKeyConflictContext {
    private static final Map<Either<Item, Tag<Item>>, ItemInHandKeyConflictContext> CONTEXTS = new HashMap<>();

    public static IKeyConflictContext from(Item item) {
        return CONTEXTS.computeIfAbsent(Either.left(item), ItemInHandKeyConflictContext::new);
    }

    public static IKeyConflictContext from(Tag<Item> item) {
        return CONTEXTS.computeIfAbsent(Either.right(item), ItemInHandKeyConflictContext::new);
    }

    @Override
    public boolean isActive() {
        var player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) {
            return false;
        }
        var item = player.getMainHandItem();
        if (item.isEmpty() || !this.item.map(item::is, item::is)) {
            item = player.getOffhandItem();
        }
        return this.item.map(item::is, item::is);
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this.equals(other);
    }
}

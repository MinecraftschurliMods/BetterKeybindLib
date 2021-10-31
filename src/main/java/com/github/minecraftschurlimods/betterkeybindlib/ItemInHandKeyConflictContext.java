package com.github.minecraftschurlimods.betterkeybindlib;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 */
public class ItemInHandKeyConflictContext extends WorldKeyConflictContext {
    private static final Map<Either<Item, Tag<Item>>, ItemInHandKeyConflictContext> CONTEXTS = new HashMap<>();

    private final @NotNull  Either<Item, Tag<Item>> item;
    private final @Nullable InteractionHand         hand;

    protected ItemInHandKeyConflictContext(Either<Item, Tag<Item>> item, @Nullable InteractionHand hand) {
        this.item = item;
        this.hand = hand;
    }

    public static IKeyConflictContext from(Item item, @Nullable InteractionHand hand) {
        return CONTEXTS.computeIfAbsent(Either.left(item), item1 -> new ItemInHandKeyConflictContext(item1, null));
    }

    public static IKeyConflictContext from(Tag<Item> item, @Nullable InteractionHand hand) {
        return CONTEXTS.computeIfAbsent(Either.right(item), item1 -> new ItemInHandKeyConflictContext(item1, null));
    }

    public static IKeyConflictContext from(Item item) {
        return from(item, null);
    }

    public static IKeyConflictContext from(Tag<Item> item) {
        return from(item, null);
    }

    @Override
    public boolean isActive() {
        Player player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) {
            return false;
        }
        ItemStack item = getMatchingStack(player);
        return this.item.map(item::is, item::is);
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this.equals(other);
    }

    @Override
    public Map<String, Function<Callback.Context,?>> context() {
        return Map.of("stack", (ctx) -> getMatchingStack(ctx.get("player")));
    }

    private ItemStack getMatchingStack(Player player) {
        ItemStack item;
        if (this.hand != null) {
            item = player.getItemInHand(this.hand);
        } else {
            item = player.getMainHandItem();
            if (item.isEmpty() || !this.item.map(item::is, item::is)) {
                item = player.getOffhandItem();
            }
        }
        return item;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemInHandKeyConflictContext) obj;
        return Objects.equals(this.item, that.item) && Objects.equals(this.hand, that.hand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, hand);
    }

    @Override
    public String toString() {
        return "ItemInHandKeyConflictContext[" + "item=" + item + ", " + "hand=" + hand + ']';
    }

}

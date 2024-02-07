package com.github.minecraftschurlimods.betterkeybindlib;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class ItemInHandKeyConflictContext extends WorldKeyConflictContext {
    private static final Map<Predicate<ItemStack>, ItemInHandKeyConflictContext> CONTEXTS = new HashMap<>();

    private final @NotNull  Predicate<ItemStack> item;
    private final @Nullable InteractionHand      hand;

    protected ItemInHandKeyConflictContext(Predicate<ItemStack> item, @Nullable InteractionHand hand) {
        this.item = item;
        this.hand = hand;
    }

    public static IKeyConflictContext from(ResourceLocation item, @Nullable InteractionHand hand) {
        return CONTEXTS.computeIfAbsent(new ItemWrapper(item), item1 -> new ItemInHandKeyConflictContext(item1, hand));
    }

    public static IKeyConflictContext from(Item item, @Nullable InteractionHand hand) {
        return from(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)), hand);
    }

    public static IKeyConflictContext from(TagKey<Item> item, @Nullable InteractionHand hand) {
        return CONTEXTS.computeIfAbsent(new TagWrapper(item), item1 -> new ItemInHandKeyConflictContext(item1, hand));
    }

    public static IKeyConflictContext from(Item item) {
        return from(item, null);
    }

    public static IKeyConflictContext from(TagKey<Item> item) {
        return from(item, null);
    }

    public static IKeyConflictContext from(ResourceLocation item) {
        return from(item, null);
    }

    @Override
    public boolean isActive() {
        Player player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) {
            return false;
        }
        ItemStack item = getMatchingStack(player);
        return this.item.test(item);
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this.equals(other);
    }

    @Override
    public Map<String, Function<Callback.Context,?>> context() {
        var map = new HashMap<>(super.context());
        map.put("stack", (ctx) -> getMatchingStack(ctx.get("player")));
        map.put("hand", (ctx) -> {
            Player player = ctx.get("player");
            return this.hand != null
                    ? this.hand
                    : getMatchingStack(player) == player.getMainHandItem()
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND;
        });
        return map;
    }

    private ItemStack getMatchingStack(Player player) {
        ItemStack item;
        if (this.hand != null) {
            item = player.getItemInHand(this.hand);
        } else {
            item = player.getMainHandItem();
            if (item.isEmpty() || !this.item.test(item)) {
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

    private record TagWrapper(TagKey<Item> tag) implements Predicate<ItemStack> {
        @Override
        public boolean test(ItemStack stack) {
            return stack.is(tag);
        }
    }

    private record ItemWrapper(ResourceLocation item) implements Predicate<ItemStack> {
        @Override
        public boolean test(ItemStack stack) {
            return item.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
    }
}

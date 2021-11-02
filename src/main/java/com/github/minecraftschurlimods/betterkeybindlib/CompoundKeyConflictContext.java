package com.github.minecraftschurlimods.betterkeybindlib;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record CompoundKeyConflictContext(List<IKeyConflictContext> contexts) implements IKeyConflictContext, IContextProvider {
    private static final Map<List<IKeyConflictContext>, CompoundKeyConflictContext> CONTEXTS = new HashMap<>();

    public static CompoundKeyConflictContext from(IKeyConflictContext context1, IKeyConflictContext context2, IKeyConflictContext... contexts) {
        List<IKeyConflictContext> list = ImmutableList.<IKeyConflictContext>builder().add(context1, context2).add(contexts).build();
        return CONTEXTS.computeIfAbsent(list, CompoundKeyConflictContext::new);
    }

    @Override
    public boolean isActive() {
        return contexts.stream().allMatch(IKeyConflictContext::isActive);
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this.equals(other) || contexts.stream().anyMatch(context -> context.conflicts(other));
    }

    @NotNull
    @Override
    public Map<String, Function<Callback.Context,?>> context() {
        Map<String, Function<Callback.Context,?>> map = new HashMap<>();
        for (IKeyConflictContext context : this.contexts()) {
            if (context == KeyConflictContext.GUI) {
                map.put("screen", (ctx) -> Minecraft.getInstance().screen);
            } else if (context instanceof IContextProvider contextProvider) {
                map.putAll(contextProvider.context());
            }
        }
        return map;
    }
}

package com.github.minecraftschurlimods.betterkeybindlib;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CompoundKeyConflictContext(List<IKeyConflictContext> contexts) implements IKeyConflictContext {
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
}

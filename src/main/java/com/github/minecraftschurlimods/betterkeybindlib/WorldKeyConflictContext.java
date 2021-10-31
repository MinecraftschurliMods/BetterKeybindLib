package com.github.minecraftschurlimods.betterkeybindlib;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.Map;
import java.util.function.Function;

public class WorldKeyConflictContext implements IKeyConflictContext, IContextProvider {
    private static final WorldKeyConflictContext INSTANCE = new WorldKeyConflictContext();

    protected WorldKeyConflictContext() {}

    @Override
    public Map<String, Function<Callback.Context,?>> context() {
        return Map.of("level", (ctx) -> Minecraft.getInstance().level,
                      "player", (ctx) -> Minecraft.getInstance().player);
    }

    @Override
    public boolean isActive() {
        return Minecraft.getInstance().level != null;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return WorldKeyConflictContext.class.equals(other.getClass());
    }
}

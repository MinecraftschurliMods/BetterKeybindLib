package com.github.minecraftschurlimods.betterkeybindlib;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ToggleableKeybind extends AbstractKeybind {
    private boolean state = false;

    ToggleableKeybind(ResourceLocation id,
                      IKeyConflictContext keyConflictContext,
                      KeyModifier keyModifier,
                      InputConstants.Type inputType,
                      int keyCode,
                      String category,
                      Callback callback) {
        super(id, () -> new KeyMapping(Util.makeDescriptionId("key", id), keyConflictContext, keyModifier, inputType, keyCode, "key.category."+category), callback);
    }

    @Contract("_, _, _ -> new")
    public static ToggleableKeybind.@NotNull Builder builder(ResourceLocation id, @MagicConstant(valuesFromClass = InputConstants.class) int key, String category) {
        return new ToggleableKeybind.Builder(id, category, key);
    }

    @Contract("_, _, _, _ -> new")
    public static ToggleableKeybind.@NotNull Builder builder(ResourceLocation id, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode, String category) {
        return new ToggleableKeybind.Builder(id, category, inputType, keyCode);
    }

    public boolean isActive() {
        return this.state;
    }

    @Override
    public boolean click() {
        this.state = !this.state;
        return super.click();
    }

    @Override
    protected Callback.Context makeContext() {
        return new ContextWrapperWithState(super.makeContext(), ToggleableKeybind.this.isActive());
    }

    public static class Builder extends AbstractBuilder<ToggleableKeybind.Builder, ToggleableKeybind> {

        private Builder(ResourceLocation id,
                        String category,
                        @MagicConstant(valuesFromClass = InputConstants.class) int key) {
            this(id, category, InputConstants.Type.KEYSYM, key);
        }

        private Builder(ResourceLocation id,
                        String category,
                        InputConstants.Type inputType,
                        @MagicConstant(valuesFromClass = InputConstants.class) int keyCode) {
            super(id, category, inputType, keyCode, KeyModifier.NONE);
        }

        public ToggleableKeybind build(IKeyConflictContext context) {
            return new ToggleableKeybind(this.id,
                                         context,
                                         this.keyModifier,
                                         this.inputType,
                                         this.keyCode,
                                         this.category,
                                         this.callback);
        }
    }

    private record ContextWrapperWithState(Callback.Context ctx, boolean state) implements Callback.Context {
        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(String name) {
            if ("state".equals(name)) {
                return (T) Boolean.valueOf(state());
            }
            return ctx().get(name);
        }

        @Override
        public boolean provides(String... names) {
            List<String> list = Arrays.asList(names);
            list.remove("state");
            return ctx().provides(list.toArray(String[]::new));
        }
    }
}

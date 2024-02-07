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

public class Keybind extends AbstractKeybind {

    Keybind(ResourceLocation id,
            IKeyConflictContext keyConflictContext,
            KeyModifier keyModifier,
            InputConstants.Type inputType,
            int keyCode,
            String category,
            Callback callback) {
        super(id, () -> new KeyMapping(Util.makeDescriptionId("key", id), keyConflictContext, keyModifier, inputType, keyCode, "key.category."+category), callback);
    }

    @Contract("_, _, _ -> new")
    public static Keybind.@NotNull Builder builder(ResourceLocation id, @MagicConstant(valuesFromClass = InputConstants.class) int key, String category) {
        return new Builder(id, category, key);
    }

    @Contract("_, _, _, _ -> new")
    public static Keybind.@NotNull Builder builder(ResourceLocation id, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode, String category) {
        return new Builder(id, category, inputType, keyCode);
    }

    public static class Builder extends AbstractBuilder<Keybind.Builder, Keybind> {

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

        public Keybind build(IKeyConflictContext context) {
            return new Keybind(this.id,
                               context,
                               this.keyModifier,
                               this.inputType,
                               this.keyCode,
                               this.category,
                               this.callback);
        }
    }
}

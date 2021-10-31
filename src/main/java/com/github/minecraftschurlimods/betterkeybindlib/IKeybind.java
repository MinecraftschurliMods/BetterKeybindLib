package com.github.minecraftschurlimods.betterkeybindlib;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

public interface IKeybind {
    ResourceLocation getId();
    boolean click();
    KeyMapping getMapping();
}

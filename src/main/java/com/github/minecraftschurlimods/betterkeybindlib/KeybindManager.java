package com.github.minecraftschurlimods.betterkeybindlib;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public final class KeybindManager {
    private static final Map<String, KeybindManager> MANAGER = new HashMap<>();
    private final Map<ResourceLocation, IKeybind> keybindings = new HashMap<>();
    private final String modid;
    private boolean busRegistered;

    public static KeybindManager get(String modid) {
        return MANAGER.computeIfAbsent(modid, KeybindManager::new);
    }

    private KeybindManager(String modid) {
        this.modid = modid;
    }

    public synchronized KeybindManager register(IEventBus bus) {
        if (this.busRegistered) return this;
        this.busRegistered = true;
        bus.addListener(EventPriority.LOWEST, this::init);
        MinecraftForge.EVENT_BUS.addListener(this::onKeyboardInput);
        MinecraftForge.EVENT_BUS.addListener(this::onMouseInput);
        return this;
    }

    private void onKeyboardInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            onInput(InputConstants.Type.KEYSYM.getOrCreate(event.getKey()));
        }
    }

    private void onMouseInput(InputEvent.MouseButton.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            onInput(InputConstants.Type.MOUSE.getOrCreate(event.getButton()));
        }
    }

    private void onInput(InputConstants.Key key) {
        for (IKeybind entry : this.keybindings.values()) {
            if (entry.getMapping().isActiveAndMatches(key)) {
                if (entry.click()) {
                    break;
                }
            }
        }
    }

    @Contract("_ -> param1")
    public <T extends IKeybind> @NotNull T register(T keybind) {
        this.keybindings.put(keybind.getId(), keybind);
        return keybind;
    }

    public Keybind.Builder keybind(String name, @MagicConstant(valuesFromClass = InputConstants.class) int key) {
        return Keybind.builder(new ResourceLocation(modid, name), key, modid).setRegistrator(this::register);
    }

    public Keybind.Builder keybind(String name, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode) {
        return Keybind.builder(new ResourceLocation(modid, name), inputType, keyCode, modid).setRegistrator(this::register);
    }

    public Keybind.Builder keybind(String name, @MagicConstant(valuesFromClass = InputConstants.class) int key, String category) {
        return Keybind.builder(new ResourceLocation(modid, name), key, category).setRegistrator(this::register);
    }

    public Keybind.Builder keybind(String name, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode, String category) {
        return Keybind.builder(new ResourceLocation(modid, name), inputType, keyCode, category).setRegistrator(this::register);
    }

    public ToggleableKeybind.Builder toggleableKeybind(String name, @MagicConstant(valuesFromClass = InputConstants.class) int key) {
        return ToggleableKeybind.builder(new ResourceLocation(modid, name), key, modid).setRegistrator(this::register);
    }

    public ToggleableKeybind.Builder toggleableKeybind(String name, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode) {
        return ToggleableKeybind.builder(new ResourceLocation(modid, name), inputType, keyCode, modid).setRegistrator(this::register);
    }

    public ToggleableKeybind.Builder toggleableKeybind(String name, @MagicConstant(valuesFromClass = InputConstants.class) int key, String category) {
        return ToggleableKeybind.builder(new ResourceLocation(modid, name), key, category).setRegistrator(this::register);
    }

    public ToggleableKeybind.Builder toggleableKeybind(String name, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode, String category) {
        return ToggleableKeybind.builder(new ResourceLocation(modid, name), inputType, keyCode, category).setRegistrator(this::register);
    }

    private void init(RegisterKeyMappingsEvent event) {
        keybindings.values().stream().map(IKeybind::getMapping).forEach(event::register);
    }
}

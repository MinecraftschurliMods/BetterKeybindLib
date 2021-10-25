package com.github.minecraftschurlimods.betterkeybindlib;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public final class KeybindManager {
    private static final Map<String, KeybindManager> MANAGER = new HashMap<>();
    private final Map<ResourceLocation, KeyMapping> mappings = new HashMap<>();
    private final Map<KeyMapping, Callback> callbacks = new HashMap<>();
    private final String modid;

    public static KeybindManager get(String modid) {
        return MANAGER.computeIfAbsent(modid, KeybindManager::new);
    }

    private KeybindManager(String modid) {
        this.modid = modid;
        MinecraftForge.EVENT_BUS.addListener(this::onKeyboardInput);
        MinecraftForge.EVENT_BUS.addListener(this::onMouseInput);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOWEST, this::init);
    }

    public KeybindBuilder keybind(String name, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode) {
        return new KeybindBuilder(new ResourceLocation(this.modid, name), keyCode);
    }

    public KeybindBuilder keybind(String name, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode) {
        return new KeybindBuilder(new ResourceLocation(this.modid, name), inputType, keyCode);
    }

    private void onKeyboardInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            onInput(InputConstants.Type.KEYSYM.getOrCreate(event.getKey()));
        }
    }

    private void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            onInput(InputConstants.Type.MOUSE.getOrCreate(event.getButton()));
        }
    }

    private void onInput(InputConstants.Key key) {
        for (Map.Entry<KeyMapping, Callback> entry : callbacks.entrySet()) {
            if (entry.getKey().isActiveAndMatches(key)) {
                if (entry.getValue().call()) {
                    break;
                }
            }
        }
    }

    private void addKeybind(KeybindBuilder builder) {
        var mapping = mappings.computeIfAbsent(builder.id, rl -> builder.createMapping());
        if (builder.callback != null) {
            this.callbacks.putIfAbsent(mapping, builder.callback);
        }
    }

    private void init(FMLClientSetupEvent event) {
        mappings.values().forEach(ClientRegistry::registerKeyBinding);
    }

    public class KeybindBuilder {
        private final ResourceLocation    id;
        private final InputConstants.Type inputType;
        private final int                 keyCode;
        private       IKeyConflictContext keyConflictContext;
        private       KeyModifier         keyModifier;
        private       Callback            callback;

        private KeybindBuilder(ResourceLocation id, @MagicConstant(valuesFromClass = InputConstants.class) int key) {
            this(id, InputConstants.Type.KEYSYM, key);
        }

        public KeybindBuilder(ResourceLocation id, InputConstants.Type inputType, @MagicConstant(valuesFromClass = InputConstants.class) int keyCode) {
            this.id = id;
            this.inputType = inputType;
            this.keyCode = keyCode;
            this.keyConflictContext = KeyConflictContext.UNIVERSAL;
            this.keyModifier = KeyModifier.NONE;
        }

        public KeybindBuilder withContext(IKeyConflictContext context) {
            this.keyConflictContext = context;
            return this;
        }

        public KeybindBuilder withModifier(KeyModifier modifier) {
            this.keyModifier = modifier;
            return this;
        }

        public KeybindBuilder withCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        private KeyMapping createMapping() {
            return new KeyMapping("key."+this.id.getNamespace()+"."+this.id.getPath(), this.keyConflictContext, this.keyModifier, this.inputType, this.keyCode, "key.category."+this.id.getNamespace());
        }

        public void build() {
            KeybindManager.this.addKeybind(this);
        }
    }

    @FunctionalInterface
    public interface Callback {
        boolean call();
    }
}

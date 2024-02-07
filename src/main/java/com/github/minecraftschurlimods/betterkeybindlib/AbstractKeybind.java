package com.github.minecraftschurlimods.betterkeybindlib;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Contract;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class AbstractKeybind implements IKeybind {
    private final ResourceLocation id;
    private final Lazy<KeyMapping> mapping;
    private final Callback callback;

    public AbstractKeybind(ResourceLocation id, Supplier<KeyMapping> mappingSupplier, Callback callback) {
        this.id = id;
        this.mapping = Lazy.of(mappingSupplier);
        this.callback = callback;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public KeyMapping getMapping() {
        return this.mapping.get();
    }

    @Override
    public boolean click() {
        return this.callback == null || this.callback.call(this.makeContext());
    }

    protected Callback.Context makeContext() {
        Map<String, Function<Callback.Context,?>> map = new HashMap<>();
        var conflictContext = this.getMapping().getKeyConflictContext();
        if (conflictContext instanceof KeyConflictContext kcc && kcc == KeyConflictContext.GUI) {
            map.put("screen", (ctx) -> Minecraft.getInstance().screen);
        } else if (conflictContext instanceof IContextProvider cp) {
            map.putAll(cp.context());
        }
        return new MapContext(map);
    }

    public static abstract class AbstractBuilder<T extends AbstractBuilder<T,B>, B extends AbstractKeybind> {
        protected final ResourceLocation          id;
        protected final String                    category;
        protected final InputConstants.Type       inputType;
        protected final int                       keyCode;
        protected final List<IKeyConflictContext> keyConflictContext;
        protected       KeyModifier               keyModifier;
        protected       Callback                  callback;
        private         UnaryOperator<B>          registrator;

        protected AbstractBuilder(ResourceLocation id,
                                  String category,
                                  InputConstants.Type inputType,
                                  int keyCode,
                                  KeyModifier keyModifier) {
            this.id = id;
            this.category = category;
            this.inputType = inputType;
            this.keyCode = keyCode;
            this.keyModifier = keyModifier;
            this.keyConflictContext = new ArrayList<>();
        }

        @Contract("_ -> this")
        public T withContext(IKeyConflictContext context) {
            this.keyConflictContext.add(context);
            return self();
        }

        @Contract("_ -> this")
        public T withModifier(KeyModifier modifier) {
            this.keyModifier = modifier;
            return self();
        }

        @Contract("_ -> this")
        public T withCallback(Callback callback) {
            this.callback = callback;
            return self();
        }

        @Contract("_ -> this")
        T setRegistrator(UnaryOperator<B> registrator) {
            this.registrator = registrator;
            return self();
        }

        public final B build() {
            IKeyConflictContext context;
            if (this.keyConflictContext.isEmpty()) {
                context = KeyConflictContext.UNIVERSAL;
            } else if (this.keyConflictContext.size() == 1) {
                context = this.keyConflictContext.get(0);
            } else {
                context = CompoundKeyConflictContext.from(
                        this.keyConflictContext.get(0),
                        this.keyConflictContext.get(1),
                        this.keyConflictContext.subList(2, this.keyConflictContext.size())
                                               .toArray(IKeyConflictContext[]::new));
            }
            return this.registrator.apply(this.build(context));
        }

        protected abstract B build(IKeyConflictContext context);

        @SuppressWarnings("unchecked")
        @Contract("-> this")
        private T self() {
            return (T) this;
        }
    }

    protected record MapContext(Map<String, Function<Callback.Context, ?>> map) implements Callback.Context {

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(String name) {
            return (T) map().getOrDefault(name, ctx -> null).apply(this);
        }

        @Override
        public boolean provides(String... names) {
            return Arrays.stream(names).allMatch(map()::containsKey);
        }

        @Override
        public Map<String, Function<Callback.Context, ?>> map() {
            return Collections.unmodifiableMap(map);
        }
    }
}

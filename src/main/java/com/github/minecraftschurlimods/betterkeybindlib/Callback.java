package com.github.minecraftschurlimods.betterkeybindlib;

@FunctionalInterface
public interface Callback {
    boolean call(Context context);

    interface Context {
        <T> T get(String name);
        boolean provides(String... names);
    }
}

package com.github.minecraftschurlimods.betterkeybindlib;

import java.util.Map;
import java.util.function.Function;

public interface IContextProvider {
    Map<String, Function<Callback.Context,?>> context();
}

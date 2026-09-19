package com.mortylovelly.skylimitless.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegistryEntry.Reference.class)
public interface RegistryEntryReferenceAccessor<T> {
    @Invoker("setValue")
    void skylimitless$setValue(T value);
}

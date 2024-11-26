package com.matyrobbrt.mekanisticrouters.mixin;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Filter.class)
public interface FilterAccess {
    @Accessor("matchers")
    List<IItemMatcher> mekrouters$getMatchers();

    @Accessor("flags")
    ModuleFlags mekrouters$getFlags();
}

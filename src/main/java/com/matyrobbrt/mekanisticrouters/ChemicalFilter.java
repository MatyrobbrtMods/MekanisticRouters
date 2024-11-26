package com.matyrobbrt.mekanisticrouters;

import com.google.common.collect.Sets;
import com.matyrobbrt.mekanisticrouters.mixin.FilterAccess;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import me.desht.modularrouters.logic.filter.Filter;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;

public record ChemicalFilter(Chemical chemical) implements IItemMatcher {
    public ChemicalFilter(ItemStack stack) {
        this(MekRouters.getStoredChemical(stack).map(ChemicalStack::getChemical).orElse(ChemicalStack.EMPTY.getChemical()));
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags) {
        return MekRouters.getStoredChemical(stack)
                .map(ch -> matchChemical(ch.getChemical(), flags))
                .orElse(false);
    }

    public boolean matchChemical(Chemical chemical, IModuleFlags flags) {
        return chemical == this.chemical || flags.matchItemTags() && !Sets.intersection(chemicalTags(chemical), chemicalTags(this.chemical)).isEmpty();
    }

    public static Set<TagKey<Chemical>> chemicalTags(Chemical chemical) {
        return MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical).tags().collect(Collectors.toSet());
    }

    public static boolean testFilter(Filter filter, ChemicalStack stack) {
        var access = ((FilterAccess) filter);
        for (IItemMatcher matcher : access.mekrouters$getMatchers()) {
            if (matcher instanceof ChemicalFilter cf && cf.matchChemical(stack.getChemical(), access.mekrouters$getFlags())) {
                return access.mekrouters$getFlags().whiteList();
            }
        }
        return !access.mekrouters$getFlags().whiteList();
    }
}

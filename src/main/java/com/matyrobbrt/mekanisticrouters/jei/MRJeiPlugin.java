package com.matyrobbrt.mekanisticrouters.jei;

import com.matyrobbrt.mekanisticrouters.MRConfig;
import com.matyrobbrt.mekanisticrouters.MekRouters;
import me.desht.modularrouters.integration.jei.JEIModularRoutersPlugin;
import mekanism.api.Action;
import mekanism.api.IMekanismAccess;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class MRJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekRouters.MOD_ID, "jei");

    public MRJeiPlugin() {
        JEIModularRoutersPlugin.registerGhostStackCreator(IMekanismAccess.INSTANCE.jeiHelper().getChemicalStackHelper().getIngredientType(),
                chem -> {
                    var stack = new ItemStack(MekanismBlocks.BASIC_CHEMICAL_TANK);
                    var cap = stack.getCapability(MekRouters.ITEM_CHEMICAL);
                    if (cap != null) {
                        cap.insertChemical(chem.copy(), Action.EXECUTE);
                    }
                    return stack;
                });
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAlias(VanillaTypes.ITEM_STACK, MekRouters.CHEMICAL_UPGRADE.toStack(), "Gas Upgrade");
        registration.addAlias(VanillaTypes.ITEM_STACK, MekRouters.CHEMICAL_MODULE_1.toStack(), "Gas Module Mk1");
        registration.addAlias(VanillaTypes.ITEM_STACK, MekRouters.CHEMICAL_MODULE_2.toStack(), "Gas Module Mk2");
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}

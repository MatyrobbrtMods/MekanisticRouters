package com.matyrobbrt.mekanisticrouters.item;

import com.matyrobbrt.mekanisticrouters.MRConfig;
import com.matyrobbrt.mekanisticrouters.MekRouters;
import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ITargetedModule;
import me.desht.modularrouters.logic.ModuleTargetList;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class ChemicalModule2 extends ChemicalModule1 implements ITargetedModule, IPositionProvider, IRangedModule {
    private static final TintColor TINT_COLOR = new TintColor(237, 204, 251);

    public ChemicalModule2() {
        super(ModItems.moduleProps().component(
                ModDataComponents.MODULE_TARGET_LIST, ModuleTargetList.EMPTY
        ));
    }

    @Override
    public boolean isValidTarget(UseOnContext ctx) {
        return ctx.getLevel().getCapability(MekRouters.BLOCK_CHEMICAL, ctx.getClickedPos(), ctx.getClickedFace()) != null;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return MRConfig.CHEMICAL_MODULE_2FE.getAsInt();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getRenderColor(int index) {
        return FastColor.ARGB32.opaque(TINT_COLOR.getRGB());
    }

    @Override
    public int getBaseRange() {
        return MRConfig.CHEMICAL_MODULE_2_BASE_RANGE.getAsInt();
    }

    @Override
    public int getHardMaxRange() {
        return MRConfig.CHEMICAL_MODULE_2_MAX_RAMGE.getAsInt();
    }
}

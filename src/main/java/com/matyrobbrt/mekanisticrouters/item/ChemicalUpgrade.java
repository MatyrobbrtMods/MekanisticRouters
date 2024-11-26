package com.matyrobbrt.mekanisticrouters.item;

import com.matyrobbrt.mekanisticrouters.MRConfig;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ChemicalUpgrade extends UpgradeItem {
    private static final TintColor TINT_COLOR = new TintColor(229, 182, 249);

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] {MRConfig.CHEMICAL_UPGRADE_MB.getAsInt()};
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<Component> list) {
        list.add(xlate("mekanisticrouters.itemText.usage.item." + BuiltInRegistries.ITEM.getKey(itemstack.getItem()).getPath(), getExtraUsageParams()));
        ClientUtil.getOpenItemRouter()
                .ifPresent(router -> list.add(ClientUtil.xlate("modularrouters.itemText.usage.item.fluidUpgradeRouter", ChemicalModule1.getRouterMaxTransfer(router))));
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getStackLimit(int slot) {
        return 35;
    }
}

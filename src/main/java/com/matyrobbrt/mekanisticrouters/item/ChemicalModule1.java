package com.matyrobbrt.mekanisticrouters.item;

import com.matyrobbrt.mekanisticrouters.ChemicalFilter;
import com.matyrobbrt.mekanisticrouters.MRConfig;
import com.matyrobbrt.mekanisticrouters.MekRouters;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.settings.TransferDirection;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.desht.modularrouters.client.util.ClientUtil.colorText;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ChemicalModule1 extends ModuleItem {
    private static final TintColor TINT_COLOR = new TintColor(219, 149, 249);

    public ChemicalModule1(Item.Properties properties) {
        super(properties, Compiled::new);
    }

    @Override
    public String getRegulatorTranslationKey(ItemStack stack) {
        boolean isAbsolute = stack.getOrDefault(MekRouters.CHEMICAL_SETTINGS, ChemicalSettings.DEFAULT).regulateAbsolute();
        return "modularrouters.guiText.tooltip.regulator." + (isAbsolute ? "labelFluidmB" : "labelFluidPct");
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return MekRouters.CHEMICAL_MODULE_MENU.get();
    }

    @Override
    protected Component getFilterItemDisplayName(ItemStack stack) {
        return MekRouters.getStoredChemical(stack).map(ChemicalStack::getTextComponent).orElse(stack.getHoverName());
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);

        var set = stack.getOrDefault(MekRouters.CHEMICAL_SETTINGS, ChemicalSettings.DEFAULT);
        list.add(xlate("modularrouters.itemText.transfer_direction",
                xlate(set.direction().getTranslationKey()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));

        list.add(xlate("mekanisticrouters.itemText.chemical.maxTransfer",
                colorText(set.maxTransfer(), ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return MRConfig.CHEMICAL_MODULE_1FE.getAsInt();
    }

    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        // only chemical-holding items or a smart filter item can go into a fluid module's filter
        if (stack.isEmpty() || stack.getItem() instanceof SmartFilterItem) return true;
        if (stack.getCount() > 1) return false;

        return MekRouters.getStoredChemical(stack).map(cs -> !cs.isEmpty()).orElse(false);
    }

    @Override
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new ChemicalFilter(stack);
    }

    @Override
    public boolean isFluidModule() {
        return true;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    public static class Compiled extends CompiledModule {
        private final ChemicalSettings settings;
        public Compiled(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
            super(router, stack);
            this.settings = stack.getOrDefault(MekRouters.CHEMICAL_SETTINGS, ChemicalSettings.DEFAULT);
        }

        @Override
        public boolean execute(@Nonnull ModularRouterBlockEntity router) {
            if (getTarget() == null) return false;

            var routerHandler = router.getBufferCapability(MekRouters.ITEM_CHEMICAL);
            if (routerHandler == null) return false;

            Level world = Objects.requireNonNull(router.getLevel());
            var targetChemicalHandler = Optional.ofNullable(world.getCapability(MekRouters.BLOCK_CHEMICAL, getTarget().gPos.pos(), getTarget().face));

            if (targetChemicalHandler.isPresent()) {
                // there's a block entity with a fluid capability; try to interact with that
                return switch (getTransferDirection()) {
                    case TO_ROUTER -> targetChemicalHandler.map(worldHandler -> doTransfer(router, worldHandler, routerHandler, TransferDirection.TO_ROUTER))
                            .orElse(false);
                    case FROM_ROUTER -> targetChemicalHandler.map(worldHandler -> doTransfer(router, routerHandler, worldHandler, TransferDirection.FROM_ROUTER))
                            .orElse(false);
                };
            }

            return false;
        }

        private boolean doTransfer(ModularRouterBlockEntity router, IChemicalHandler src, IChemicalHandler dest, TransferDirection direction) {
            if (getRegulationAmount() > 0) {
                if (direction == TransferDirection.TO_ROUTER && checkChemicalInTank(src) <= getRegulationAmount()) {
                    return false;
                } else if (direction == TransferDirection.FROM_ROUTER && checkChemicalInTank(dest) >= getRegulationAmount()) {
                    return false;
                }
            }
            int amount = getMaxTransfer(router);
            var newStack = MekRouters.tryChemicalTransfer(src, dest, amount, false);
            if (newStack.isRadioactive() && !MRConfig.RADIATION_TRANSFER.getAsBoolean()) {
                return false;
            }

            if (!newStack.isEmpty() && ChemicalFilter.testFilter(getFilter(), newStack)) {
                newStack = MekRouters.tryChemicalTransfer(src, dest, newStack.getAmount(), true);
                return !newStack.isEmpty();
            }
            return false;
        }

        public int getMaxTransfer(ModularRouterBlockEntity router) {
            return settings.getEffectiveMaxTransfer(getRouterMaxTransfer(router));
        }

        public boolean isRegulateAbsolute() {
            return settings.regulateAbsolute();
        }

        public TransferDirection getTransferDirection() {
            return settings.direction();
        }

        private long checkChemicalInTank(IChemicalHandler handler) {
            // note: total amount of all chemicals in all tanks... not ideal for inventories with multiple tanks
            long total = 0, max = 0;
            if (isRegulateAbsolute()) {
                for (int idx = 0; idx < handler.getChemicalTanks(); idx++) {
                    total += handler.getChemicalInTank(idx).getAmount();
                }
                return total;
            } else {
                for (int idx = 0; idx < handler.getChemicalTanks(); idx++) {
                    max += handler.getChemicalTankCapacity(idx);
                    total += handler.getChemicalInTank(idx).getAmount();
                }
                return max == 0 ? 0 : (total * 100) / max;
            }
        }
    }

    public static int getRouterMaxTransfer(ModularRouterBlockEntity router) {
        return (MRConfig.BASE_CHEMICAL_RATE.getAsInt() + router.getUpgradeCount(MekRouters.CHEMICAL_UPGRADE.get()) * MRConfig.CHEMICAL_UPGRADE_MB.getAsInt());
    }
}

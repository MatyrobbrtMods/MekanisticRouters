package com.matyrobbrt.mekanisticrouters;

import com.matyrobbrt.mekanisticrouters.data.ModItemModelProvider;
import com.matyrobbrt.mekanisticrouters.item.ChemicalModule1;
import com.matyrobbrt.mekanisticrouters.item.ChemicalModule2;
import com.matyrobbrt.mekanisticrouters.item.ChemicalSettings;
import com.matyrobbrt.mekanisticrouters.item.ChemicalUpgrade;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModCreativeModeTabs;
import me.desht.modularrouters.core.ModItems;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

@Mod(MekRouters.MOD_ID)
public final class MekRouters {
    public static final String MOD_ID = "mekanisticrouters";

    public static final ItemCapability<IChemicalHandler, Void> ITEM_CHEMICAL = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_handler"), IChemicalHandler.class);
    public static final BlockCapability<IChemicalHandler, Direction> BLOCK_CHEMICAL = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_handler"), IChemicalHandler.class);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChemicalSettings>> CHEMICAL_SETTINGS = COMPONENTS.registerComponentType("chemical_settings", b ->
            b.persistent(ChemicalSettings.CODEC).networkSynchronized(ChemicalSettings.STREAM_CODEC));
    public static final DeferredHolder<MenuType<?>, MenuType<ModuleMenu>> CHEMICAL_MODULE_MENU = MENU_TYPES.register("chemical_module", () -> IMenuTypeExtension.create(MekRouters::createChemicalMenu));

    public static final DeferredItem<ChemicalModule1> CHEMICAL_MODULE_1 = ITEMS.register("chemical_module_mk1", () -> new ChemicalModule1(ModItems.moduleProps()));
    public static final DeferredItem<ChemicalModule2> CHEMICAL_MODULE_2 = ITEMS.register("chemical_module_mk2", ChemicalModule2::new);
    public static final DeferredItem<ChemicalUpgrade> CHEMICAL_UPGRADE = ITEMS.register("chemical_upgrade", ChemicalUpgrade::new);

    public MekRouters(final IEventBus bus, final ModContainer container) {
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        MENU_TYPES.register(bus);

        bus.addListener((final BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTab() == ModCreativeModeTabs.DEFAULT.get()) {
                event.accept(CHEMICAL_UPGRADE);
                event.accept(CHEMICAL_MODULE_1);
                event.accept(CHEMICAL_MODULE_2);
            }
        });

        bus.addListener((final GatherDataEvent event) -> {
            event.getGenerator().addProvider(
                    event.includeClient(), new ModItemModelProvider(event.getGenerator(), event.getExistingFileHelper())
            );
        });

        bus.addListener((final RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(BLOCK_CHEMICAL, ModBlockEntities.MODULAR_ROUTER.get(), (object, context) -> object.getBufferCapability(MekRouters.ITEM_CHEMICAL));
        });

        container.registerConfig(ModConfig.Type.SERVER, MRConfig.SPEC);
    }

    public static Optional<ChemicalStack> getStoredChemical(ItemStack stack) {
        return Optional.ofNullable(stack.getCapability(ITEM_CHEMICAL))
                .filter(h -> h.getChemicalTanks() > 0)
                .map(h -> h.getChemicalInTank(0));
    }

    private static ModuleMenu createChemicalMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(CHEMICAL_MODULE_MENU.get(), windowId, inv, extra);
    }

    public static ChemicalStack tryChemicalTransfer(IChemicalHandler chemSource, IChemicalHandler chemDest, long maxAmount, boolean doTransfer) {
        var drainable = chemSource.extractChemical(maxAmount, Action.SIMULATE);
        if (!drainable.isEmpty()) {
            long fillableAmount = drainable.getAmount() - chemDest.insertChemical(drainable, Action.SIMULATE).getAmount();
            if (fillableAmount > 0) {
                drainable.setAmount(fillableAmount);
                if (doTransfer) {
                    var drained = chemSource.extractChemical(drainable, Action.EXECUTE);
                    if (!drained.isEmpty()) {
                        drained.shrink(chemDest.insertChemical(drained, Action.EXECUTE).getAmount());
                        return drained;
                    }
                } else {
                    return drainable;
                }
            }
            return ChemicalStack.EMPTY;
        }
        return ChemicalStack.EMPTY;
    }
}

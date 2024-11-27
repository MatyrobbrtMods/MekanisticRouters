package com.matyrobbrt.mekanisticrouters.client;

import com.matyrobbrt.mekanisticrouters.MekRouters;
import com.matyrobbrt.mekanisticrouters.item.ChemicalRefillModule;
import me.desht.modularrouters.client.gui.module.ModuleScreen;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class ChemicalRefillModuleScreen extends ModuleScreen {
    private static final ResourceLocation WIDGETS = MiscUtil.RL("textures/gui/widgets.png");
    private static final ItemStack MAIN_INV_STACK = new ItemStack(Blocks.CHEST);
    private static final ItemStack MAIN_NO_HOTBAR_INV_STACK = new ItemStack(Blocks.BARREL);
    private static final ItemStack ARMOUR_STACK = new ItemStack(Items.DIAMOND_CHESTPLATE);
    private static final ItemStack OFFHAND_STACK = new ItemStack(Items.SHIELD);
    private static final ItemStack ROUTER_STACK = new ItemStack(ModBlocks.MODULAR_ROUTER.get());

    private static final ItemStack[] STACKS = new ItemStack[] {
            MAIN_INV_STACK, MAIN_NO_HOTBAR_INV_STACK, ARMOUR_STACK, OFFHAND_STACK
    };

    private ItemStackCyclerButton<ChemicalRefillModule.Section> secButton;

    public ChemicalRefillModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        var settings = moduleItemStack.getOrDefault(MekRouters.REFILL_SETTINGS, ChemicalRefillModule.RefillSettings.DEFAULT);
        addRenderableWidget(secButton = new ItemStackCyclerButton<>(leftPos + 169, topPos + 32, 16, 16, true, STACKS, settings.section(), this));

        getMouseOverHelp().addHelpRegion(leftPos + 127, topPos + 29, leftPos + 187, topPos + 50, "mekanisticrouters.guiText.popup.chemical_refill.control");
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        graphics.blit(GUI_TEXTURE, leftPos + 168, topPos + 31, BUTTON_XY.x(), BUTTON_XY.y(), 18, 18);  // section "button" background
        graphics.blit(WIDGETS, leftPos + 148, topPos + 32, 160 + 16, 16, 16, 16);
        graphics.renderItem(ROUTER_STACK, leftPos + 128, topPos + 32);
    }

    @Override
    protected ItemStack buildModifiedItemStack() {
        return Util.make(super.buildModifiedItemStack(), stack ->
                stack.set(MekRouters.REFILL_SETTINGS, new ChemicalRefillModule.RefillSettings(
                        secButton.getState()
                ))
        );
    }
}

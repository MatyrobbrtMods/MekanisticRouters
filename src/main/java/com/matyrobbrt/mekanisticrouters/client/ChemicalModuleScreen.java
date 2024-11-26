package com.matyrobbrt.mekanisticrouters.client;

import com.matyrobbrt.mekanisticrouters.MekRouters;
import com.matyrobbrt.mekanisticrouters.item.ChemicalModule1;
import com.matyrobbrt.mekanisticrouters.item.ChemicalSettings;
import me.desht.modularrouters.client.gui.module.ModuleScreen;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.logic.settings.TransferDirection;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ChemicalModuleScreen extends ModuleScreen {
    static final XYPoint LARGE_TEXTFIELD_XY = new XYPoint(0, 212);

    private final ItemStack routerStack = new ItemStack(ModBlocks.MODULAR_ROUTER.get());
    private final ItemStack tankStack = new ItemStack(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem());
    private final ItemStack gaugeDropperStack = new ItemStack(MekanismItems.GAUGE_DROPPER.asItem());

    private IntegerTextField maxTransferField;
    private FluidDirectionButton fluidDir;
    private RegulateAbsoluteButton regulationTypeButton;

    public ChemicalModuleScreen(ModuleMenu container, Inventory inventory, Component displayName) {
        super(container, inventory, displayName);
    }

    @Override
    public void init() {
        super.init();

        var settings = moduleItemStack.getOrDefault(MekRouters.CHEMICAL_SETTINGS, ChemicalSettings.DEFAULT);

        maxTransferField = new IntegerTextField(font, leftPos + 152, topPos + 23, 34, 12, Range.of(0, Integer.MAX_VALUE));
        if (settings.maxTransfer() > 0) {
            maxTransferField.setValue(settings.maxTransfer());
        }
        maxTransferField.setResponder(str -> sendModuleSettingsDelayed(5));
        maxTransferField.setIncr(100, 10, 10);
        maxTransferField.useGuiTextBackground();
        addRenderableWidget(maxTransferField);

        addRenderableWidget(new TooltipButton(leftPos + 130, topPos + 19, 16, 16, gaugeDropperStack));
        addRenderableWidget(fluidDir = new FluidDirectionButton(leftPos + 148, topPos + 44, settings.direction()));
        addRenderableWidget(regulationTypeButton = new RegulateAbsoluteButton(regulatorTextField.getX() + regulatorTextField.getWidth() + 2, regulatorTextField.getY() - 1, 18, 14, b -> toggleRegulationType(), settings.regulateAbsolute()));

        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 17, leftPos + 183, topPos + 35, "mekanisticrouters.guiText.popup.chemical.maxTransfer");
        getMouseOverHelp().addHelpRegion(leftPos + 126, topPos + 42, leftPos + 185, topPos + 61, "mekanisticrouters.guiText.popup.chemical.direction");
    }

    @Override
    protected IntegerTextField buildRegulationTextField() {
        IntegerTextField tf = new IntegerTextField(font, leftPos + 128, topPos + 90, 40, 12, Range.of(0, Integer.MAX_VALUE));
        tf.setValue(getRegulatorAmount());
        tf.setResponder((str) -> {
            setRegulatorAmount(str.isEmpty() ? 0 : Integer.parseInt(str));
            sendModuleSettingsDelayed(5);
        });
        return tf;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // text entry field custom background - super has already bound the correct texture
        graphics.blit(GUI_TEXTURE, leftPos + 146, topPos + 20, LARGE_TEXTFIELD_XY.x(), LARGE_TEXTFIELD_XY.y(), 35, 14);

        graphics.renderItem(routerStack, leftPos + 128, topPos + 44);
        graphics.renderItem(tankStack, leftPos + 168, topPos + 44);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        regulationTypeButton.visible = regulatorTextField.visible;
        regulationTypeButton.setText();
        regulatorTextField.setRange(Range.of(0, regulationTypeButton.regulateAbsolute ? Integer.MAX_VALUE : 100));
    }

    @Override
    protected ItemStack buildModifiedItemStack() {
        return Util.make(super.buildModifiedItemStack(), stack ->
                stack.set(MekRouters.CHEMICAL_SETTINGS, new ChemicalSettings(
                        fluidDir.getState(),
                        maxTransferField.getIntValue(),
                        regulationTypeButton.regulateAbsolute
                ))
        );
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y, int width, int height, ItemStack renderStack) {
            super(x, y, width, height, renderStack, true, p -> {});
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(xlate("mekanisticrouters.guiText.tooltip.chemicalTransferTooltip"));
            tooltip.add(Component.empty());
            tooltip.add(xlate("modularrouters.guiText.tooltip.numberFieldTooltip"));
            ClientUtil.setMultilineTooltip(this, tooltip);
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
            // no sound
        }
    }

    private void toggleRegulationType() {
        regulationTypeButton.toggle();
        regulatorTextField.setRange(regulationTypeButton.regulateAbsolute ? Range.of(0, Integer.MAX_VALUE) : Range.of(0, 100));
        sendToServer();
    }

    private class FluidDirectionButton extends TexturedCyclerButton<TransferDirection> {
        FluidDirectionButton(int x, int y, TransferDirection initialVal) {
            super(x, y, 16, 16, initialVal, ChemicalModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(160 + getState().ordinal() * 16, 16);
        }
    }

    private static class RegulateAbsoluteButton extends ExtendedButton {
        private boolean regulateAbsolute;

        public RegulateAbsoluteButton(int xPos, int yPos, int width, int height, OnPress pressable, boolean regulateAbsolute) {
            super(xPos, yPos, width, height, Component.empty(), pressable);
            this.regulateAbsolute = regulateAbsolute;
        }

        private void toggle() {
            regulateAbsolute = !regulateAbsolute;
        }

        void setText() {
            setMessage(Component.literal(regulateAbsolute ? "mB" : "%"));
        }
    }
}

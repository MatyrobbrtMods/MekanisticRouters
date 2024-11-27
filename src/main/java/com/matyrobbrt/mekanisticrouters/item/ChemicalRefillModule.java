package com.matyrobbrt.mekanisticrouters.item;

import com.matyrobbrt.mekanisticrouters.MRConfig;
import com.matyrobbrt.mekanisticrouters.MekRouters;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.module.TargetValidation;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.logic.settings.TransferDirection;
import me.desht.modularrouters.util.TranslatableEnum;
import me.desht.modularrouters.util.WildcardedRLMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static me.desht.modularrouters.client.util.ClientUtil.colorText;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ChemicalRefillModule extends ModuleItem implements IPlayerOwned {
    private static final TintColor TINT_COLOR = new TintColor(175, 114, 249);

    public ChemicalRefillModule() {
        super(ModItems.moduleProps()
                        .component(ModDataComponents.PLAYER_SETTINGS, CompiledPlayerModule.PlayerSettings.DEFAULT),
                Compiled::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, List<Component> list) {
        super.addSettingsInformation(stack, list);

        var settings = stack.getOrDefault(MekRouters.REFILL_SETTINGS, RefillSettings.DEFAULT);
        ResolvableProfile profile = stack.get(ModDataComponents.OWNER);

        String owner = profile == null ? "-" : profile.gameProfile().getName();
        list.add(xlate("modularrouters.itemText.security.owner", colorText(owner, ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));

        Component c = xlate("modularrouters.itemText.misc.operation").withStyle(ChatFormatting.YELLOW)
                .append(": ")
                .append(xlate("block.modularrouters.modular_router")
                        .append(" ")
                        .append(TransferDirection.FROM_ROUTER.getSymbol())
                        .append(" ")
                        .append(xlate(settings.section().getTranslationKey()))
                        .withStyle(ChatFormatting.AQUA)
                );
        list.add(c);
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return MekRouters.CHEMICAL_REFILL_MODULE_MENU.get();
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (ctx.getPlayer() != null && ctx.getPlayer().isSteppingCarefully()) {
            setOwner(ctx.getItemInHand(), ctx.getPlayer());
            ctx.getPlayer().displayClientMessage(Component.translatable("modularrouters.itemText.security.owner", ctx.getPlayer().getDisplayName()), false);
            return InteractionResult.SUCCESS;
        } else {
            return super.useOn(ctx);
        }
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return MRConfig.CHEMICAL_REFILL_MODULE_FE.getAsInt();
    }

    @Override
    public void doModuleValidation(ItemStack stack, ServerPlayer player) {
        TargetValidation v = ModularRouters.getDimensionBlacklist().test(player.level().dimension().location()) ?
                TargetValidation.BAD_DIMENSION :
                TargetValidation.OK;
        MutableComponent msg = Component.translatable(v.translationKey()).withStyle(v.getColor());
        player.displayClientMessage(msg, false);
    }

    public static class Compiled extends CompiledModule {
        private final RefillSettings settings;
        private final GameProfile playerProfile;

        protected Compiled(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
            super(router, stack);
            settings = stack.getOrDefault(MekRouters.REFILL_SETTINGS, RefillSettings.DEFAULT);
            playerProfile = ((IPlayerOwned) stack.getItem()).getOwnerProfile(stack).orElse(null);
        }

        @Override
        public boolean execute(@NotNull ModularRouterBlockEntity router) {
            if (playerProfile == null) return false;
            var player = router.nonNullLevel().getServer().getPlayerList().getPlayer(playerProfile.getId());
            if (player == null || isDimensionBlacklisted(router, player)) return false;

            var chemicalHandler = router.getBufferCapability(MekRouters.ITEM_CHEMICAL);
            if (chemicalHandler == null) return false;
            long[] rate = {ChemicalModule1.getRouterMaxTransfer(router)};

            forEachItem(player, settings.section, stack -> {
                if (!getFilter().test(stack)) return false;
                var targetHandler = stack.getCapability(MekRouters.ITEM_CHEMICAL);
                if (targetHandler == null) return false;

                var newStack = MekRouters.tryChemicalTransfer(chemicalHandler, targetHandler, rate[0], false);
                if (newStack.isEmpty() || (newStack.isRadioactive() && !MRConfig.RADIATION_TRANSFER.getAsBoolean())) {
                    return false;
                }

                var remainder = MekRouters.tryChemicalTransfer(chemicalHandler, targetHandler, newStack.getAmount(), true);
                rate[0] -= (newStack.getAmount() - remainder.getAmount());

                return rate[0] <= 0;
            });

            return false;
        }

        private static void forEachItem(Player player, Section section, Predicate<ItemStack> pred) {
            switch (section) {
                case MAIN -> {
                    for (ItemStack item : player.getInventory().items) {
                        if (!item.isEmpty() && pred.test(item)) break;
                    }
                }
                case MAIN_NO_HOTBAR -> {
                    for (int i = Inventory.getSelectionSize(); i < player.getInventory().items.size(); i++) {
                        var item = player.getInventory().items.get(i);
                        if (!item.isEmpty() && pred.test(item)) break;
                    }
                }
                case ARMOR -> {
                    for (ItemStack item : player.getInventory().armor) {
                        if (!item.isEmpty() && pred.test(item)) break;
                    }
                }
                case OFFHAND -> {
                    var item = player.getOffhandItem();
                    if (!item.isEmpty()) {
                        pred.test(item);
                    }
                }
            }
        }

        private boolean isDimensionBlacklisted(ModularRouterBlockEntity router, Player player) {
            WildcardedRLMatcher matcher = ModularRouters.getDimensionBlacklist();
            return matcher.test(router.nonNullLevel().dimension().location()) || matcher.test(player.level().dimension().location());
        }
    }

    public record RefillSettings(Section section) {
        public static final RefillSettings DEFAULT = new RefillSettings(Section.ARMOR);
        public static final Codec<RefillSettings> CODEC = StringRepresentable.fromEnum(Section::values)
                .optionalFieldOf("section", Section.ARMOR)
                .xmap(RefillSettings::new, RefillSettings::section)
                .codec();
        public static final StreamCodec<FriendlyByteBuf, RefillSettings> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Section.class)
                .map(RefillSettings::new, RefillSettings::section);
    }

    public enum Section implements TranslatableEnum, StringRepresentable {
        MAIN("main"),
        MAIN_NO_HOTBAR("main_no_hotbar"),
        ARMOR("armor"),
        OFFHAND("offhand");

        private final String name;

        Section(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.playerSect." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}


package com.matyrobbrt.mekanisticrouters.data;

import com.matyrobbrt.mekanisticrouters.MekRouters;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MRItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED = ResourceLocation.parse("item/generated");

    public MRItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), MekRouters.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var registryObject : MekRouters.ITEMS.getEntries()) {
            String name = registryObject.getId().getPath();
            switch (registryObject.get()) {
                case ModuleItem moduleItem -> {
                    simpleItem(registryObject,
                            modidM("item/module/module_layer0"),
                            modidM("item/module/module_layer1"),
                            modid("item/module/" + name));
                }
                case UpgradeItem upgradeItem -> simpleItem(registryObject,
                        modidM("item/upgrade/upgrade_layer0"),
                        modidM("item/upgrade/upgrade_layer1"),
                        modid("item/upgrade/" + name));
                case AugmentItem augmentItem -> simpleItem(registryObject,
                        modidM("item/augment/augment_layer0"),
                        modid("item/augment/" + name));
                default -> {
                }
            }
        }
    }

    private ItemModelBuilder simpleItem(DeferredHolder<Item, ? extends Item> item, String... textures) {
        return simpleItem(item.getId(), textures);
    }

    private ItemModelBuilder simpleItem(ResourceLocation itemKey, String... textures) {
        ItemModelBuilder builder = withExistingParent(itemKey.getPath(), GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }

    private String modidM(String path) {
        return ModularRouters.MODID + ":" + path;
    }

    private String modid(String path) {
        return this.modid + ":" + path;
    }
}

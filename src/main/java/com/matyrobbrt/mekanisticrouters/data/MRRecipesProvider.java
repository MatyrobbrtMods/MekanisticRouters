package com.matyrobbrt.mekanisticrouters.data;

import com.matyrobbrt.mekanisticrouters.MekRouters;
import me.desht.modularrouters.core.ModItems;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class MRRecipesProvider extends RecipeProvider {
    public MRRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(
                RecipeCategory.MISC, MekRouters.CHEMICAL_MODULE_1.toStack()
        ).pattern(" O ").pattern("GMG").pattern(" O ")
                .unlockedBy("has_bm", has(ModItems.BLANK_MODULE))
                .define('O', ItemTags.create(ResourceLocation.parse("c:ingots/osmium")))
                .define('M', ModItems.BLANK_MODULE)
                .define('G', Tags.Items.GLASS_BLOCKS_CHEAP)
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(
                RecipeCategory.MISC, MekRouters.CHEMICAL_MODULE_2.toStack()
        ).unlockedBy("has_bm", has(ModItems.BLANK_MODULE)).requires(MekRouters.CHEMICAL_MODULE_1).requires(Items.PRISMARINE_SHARD).save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(
                RecipeCategory.MISC, MekRouters.CHEMICAL_MODULE_2.toStack(4)
        ).unlockedBy("has_bm", has(ModItems.BLANK_MODULE)).requires(MekRouters.CHEMICAL_MODULE_1, 4).requires(Items.PRISMARINE_SHARD)
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(MekRouters.MOD_ID, "chemical_module_mk2_x4"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MekRouters.CHEMICAL_UPGRADE)
                .pattern(" O ").pattern("GMG").pattern(" O ")
                .unlockedBy("has_bm", has(ModItems.BLANK_MODULE))
                .define('O', ItemTags.create(ResourceLocation.parse("c:ingots/osmium")))
                .define('M', ModItems.BLANK_UPGRADE)
                .define('G', Tags.Items.GLASS_BLOCKS_CHEAP)
                .save(recipeOutput);
    }
}

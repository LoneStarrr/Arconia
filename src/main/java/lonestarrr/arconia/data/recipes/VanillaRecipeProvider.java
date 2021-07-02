package lonestarrr.arconia.data.recipes;

import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;

import java.util.function.Consumer;

public class VanillaRecipeProvider extends RecipeProvider {

    public VanillaRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        //addCriterion - makes recipe visible only after you have attained a specific (set of)item(s)
        registerArconiumBlocks(consumer);
        registerArconiumIngots(consumer);
        registerTreeRootBlocks(consumer);
    }

    private void registerTreeRootBlocks(Consumer<IFinishedRecipe> consumer) {
        for (RainbowColor tier: RainbowColor.values()) {
            Item treeRoot = ModItems.getColoredRoot(tier);
            Item treeRootBlock = ModBlocks.getResourceTreeRootBlock(tier).asItem();
            ShapedRecipeBuilder.shapedRecipe(treeRootBlock)
                    .key('R', treeRoot)
                    .patternLine("RRR")
                    .patternLine("RRR")
                    .patternLine("RRR")
                    .addCriterion("has_item", hasItem(treeRoot))
                    .build(consumer);
        }
    }

    private void registerArconiumIngots(Consumer<IFinishedRecipe> consumer) {
        for (RainbowColor tier: RainbowColor.values()) {
            Item ingot = ModItems.getArconiumIngot(tier);
            Item essence = ModItems.getArconiumEssence(tier);

            ShapedRecipeBuilder.shapedRecipe(ingot)
                    .key('E', essence)
                    .patternLine("EEE")
                    .patternLine("EEE")
                    .patternLine("EEE")
                    .addCriterion("has_item", hasItem(essence))
                    .build(consumer);
        }
    }

    private void registerArconiumBlocks(Consumer<IFinishedRecipe> consumer) {
        for (RainbowColor tier: RainbowColor.values()) {
            Block block = ModBlocks.getArconiumBlock(tier);
            Item ingot = ModItems.getArconiumIngot(tier);
            ShapedRecipeBuilder.shapedRecipe(block)
                    .key('I', ingot)
                    .patternLine("III")
                    .patternLine("III")
                    .patternLine("III")
                    .addCriterion("has_item", hasItem(ingot))
                    .build(consumer);
        }
    }
}

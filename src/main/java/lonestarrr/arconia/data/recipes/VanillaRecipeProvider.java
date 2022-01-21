package lonestarrr.arconia.data.recipes;

import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

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
        registerCrates(consumer);
        registerMisc(consumer);
    }

    private void registerMisc(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(ModBlocks.hat)
                .key('W', Items.GREEN_WOOL)
                .key('H', Items.GOLDEN_HELMET)
                .patternLine("WWW")
                .patternLine("WHW")
                .patternLine("   ")
                .addCriterion("has_item", hasItem(Items.GOLDEN_HELMET))
                .build(consumer);
    }

    private void registerTreeRootBlocks(Consumer<IFinishedRecipe> consumer) {
        for (RainbowColor tier : RainbowColor.values()) {
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
        for (RainbowColor tier : RainbowColor.values()) {
            Item ingot = ModItems.getArconiumIngot(tier);
            Item arconiumBlock = ModBlocks.getArconiumBlock(tier).asItem();

            ShapelessRecipeBuilder.shapelessRecipe(ingot, 9)
                    .addIngredient(arconiumBlock)
                    .addCriterion("has_item", hasItem(arconiumBlock))
                    .build(consumer);
        }
    }

    private void registerArconiumBlocks(Consumer<IFinishedRecipe> consumer) {
        for (RainbowColor tier : RainbowColor.values()) {
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

    private void registerCrates(Consumer<IFinishedRecipe> consumer) {
        for (RainbowColor tier : RainbowColor.values()) {
            Block block = ModBlocks.getRainbowCrateBlock(tier);
            ShapedRecipeBuilder.shapedRecipe(block)
                    .key('I', ModItems.getArconiumIngot(tier))
                    .key('C', Tags.Items.CHESTS)
                    .key('P', ItemTags.PLANKS)
                    .patternLine("IPI")
                    .patternLine("PCP")
                    .patternLine("IPI")
                    .addCriterion("has_item", hasItem(ModItems.getArconiumIngot(tier)))
                    .build(consumer);
        }
    }

}

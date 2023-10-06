package lonestarrr.arconia.data.recipes;

import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class VanillaRecipeProvider extends RecipeProvider {

    public VanillaRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        //addCriterion - makes recipe visible only after you have attained a specific (set of)item(s)
        registerArconiumBlocks(consumer);
        registerArconiumIngots(consumer);
        registerArconiumSickles(consumer);
        registerMisc(consumer);
    }

    private void registerMisc(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(ModBlocks.hat.get())
                .define('W', Items.GREEN_WOOL)
                .define('H', Items.GOLDEN_HELMET)
                .pattern("WWW")
                .pattern("WHW")
                .pattern("   ")
                .unlockedBy("has_item", has(Items.GOLDEN_HELMET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(ModBlocks.worldBuilder.get())
                .define('D', Items.DIRT)
                .define('P', Items.OAK_PLANKS)
                .pattern("PPP")
                .pattern("PDP")
                .pattern("PPP")
                .unlockedBy("has_item", has(Items.DIRT))
                .save(consumer);
    }

    private void registerArconiumIngots(Consumer<FinishedRecipe> consumer) {
        for (RainbowColor tier : RainbowColor.values()) {
            Item ingot = ModItems.getArconiumIngot(tier).get();
            Item arconiumBlock = ModBlocks.getArconiumBlock(tier).get().asItem();

            ShapelessRecipeBuilder.shapeless(ingot, 9)
                    .requires(arconiumBlock)
                    .unlockedBy("has_item", has(arconiumBlock))
                    .save(consumer);
        }
    }

    private void registerArconiumSickles(Consumer<FinishedRecipe> consumer) {
        for (RainbowColor tier : RainbowColor.values()) {
            Item ingot = ModItems.getArconiumIngot(tier).get();
            Item sickle = ModItems.getArconiumSickle(tier).get();

            ShapedRecipeBuilder.shaped(sickle)
                    .define('S', Items.STICK)
                    .define('I', ingot)
                    .pattern("III")
                    .pattern("I I")
                    .pattern("  S")
                    .unlockedBy("has_item", has(ingot))
                    .save(consumer);
        }
    }


    private void registerArconiumBlocks(Consumer<FinishedRecipe> consumer) {
        for (RainbowColor tier : RainbowColor.values()) {
            Block block = ModBlocks.getArconiumBlock(tier).get();
            Item ingot = ModItems.getArconiumIngot(tier).get();
            ShapedRecipeBuilder.shaped(block)
                    .define('I', ingot)
                    .pattern("III")
                    .pattern("III")
                    .pattern("III")
                    .unlockedBy("has_item", has(ingot))
                    .save(consumer);
        }
    }
}

package lonestarrr.arconia.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lonestarrr.arconia.common.block.GoldArconiumBlock;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.BlockNames;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.ModItems;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Registers recipes specific for the pedestal crafting ritual
 */
public class PedestalProvider extends RecipeProvider {

    public PedestalProvider(DataGenerator gen) {
        super(gen);
    }

    @Override
    public String getName() {
        return "ResourceTrees pedestal recipes";
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        // Recipes for enchanted colored tree roots - these are used to activate a resource tree of the same color and makes them start
        // generating the resource associated with them
        consumer.accept(makeEnchantedRedColoredRoot(ModItems.getArconiumEssence(RainbowColor.RED), Items.RED_WOOL));
        consumer.accept(makeEnchantedRedColoredRoot(Items.RED_DYE, Items.RED_DYE));
        consumer.accept(makeEnchantedRedColoredRoot(Items.OAK_LOG, Items.OAK_LOG));
        consumer.accept(makeEnchantedRedColoredRoot(Items.DARK_OAK_LOG, Items.DARK_OAK_LOG));
        consumer.accept(makeEnchantedRedColoredRoot(Items.BIRCH_LOG, Items.BIRCH_LOG));
        consumer.accept(makeEnchantedRedColoredRoot(Items.JUNGLE_LOG, Items.JUNGLE_LOG));
        consumer.accept(makeEnchantedRedColoredRoot(Items.ACACIA_LOG, Items.ACACIA_LOG));
        consumer.accept(makeEnchantedRedColoredRoot(Items.SPRUCE_LOG, Items.SPRUCE_LOG));
        consumer.accept(makeEnchantedRedColoredRoot(Items.SAND, Items.SAND));
        consumer.accept(makeEnchantedRedColoredRoot(Items.GRAVEL, Items.GRAVEL));
        consumer.accept(makeEnchantedRedColoredRoot(Items.DIRT, Items.DIRT));
        consumer.accept(makeEnchantedRedColoredRoot(Items.KELP, Items.KELP));

        consumer.accept(makeEnchantedOrangeColoredRoot(ModItems.getArconiumEssence(RainbowColor.ORANGE), Items.ORANGE_WOOL));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.ORANGE_DYE, Items.ORANGE_DYE));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.COAL, Items.COAL));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.STONE, Items.STONE));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.DIORITE, Items.DIORITE));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.GRANITE, Items.GRANITE));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.ANDESITE, Items.ANDESITE));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.STRING, Items.SPIDER_EYE));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.COOKED_BEEF, Items.BEEF));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.SUGAR_CANE, Items.SUGAR_CANE));

        consumer.accept(makeEnchantedYellowColoredRoot(ModItems.getArconiumEssence(RainbowColor.YELLOW), Items.YELLOW_WOOL));
        consumer.accept(makeEnchantedYellowColoredRoot(Items.YELLOW_DYE, Items.YELLOW_DYE));
        consumer.accept(makeEnchantedYellowColoredRoot(Items.IRON_INGOT, Items.IRON_BLOCK));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.ARROW, Items.BONE_BLOCK));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.LEATHER, Items.LEATHER));
        consumer.accept(makeEnchantedOrangeColoredRoot(Items.COOKED_PORKCHOP, Items.PORKCHOP));

        consumer.accept(makeEnchantedGreenColoredRoot(ModItems.getArconiumEssence(RainbowColor.GREEN), Items.GREEN_WOOL));
        consumer.accept(makeEnchantedGreenColoredRoot(Items.GREEN_DYE, Items.GREEN_DYE));
        consumer.accept(makeEnchantedGreenColoredRoot(Items.GOLD_INGOT, Items.GOLD_BLOCK));
        consumer.accept(makeEnchantedGreenColoredRoot(Items.LAPIS_LAZULI, Items.LAPIS_BLOCK));
        consumer.accept(makeEnchantedGreenColoredRoot(Items.REDSTONE, Items.REDSTONE_BLOCK));
        consumer.accept(makeEnchantedGreenColoredRoot(Items.GUNPOWDER, Items.GUNPOWDER));

        consumer.accept(makeEnchantedLightBlueColoredRoot(ModItems.getArconiumEssence(RainbowColor.LIGHT_BLUE), Items.LIGHT_BLUE_WOOL));
        consumer.accept(makeEnchantedLightBlueColoredRoot(Items.LIGHT_BLUE_DYE, Items.LIGHT_BLUE_DYE));
        consumer.accept(makeEnchantedLightBlueColoredRoot(Items.DIAMOND, Items.DIAMOND_BLOCK));
        consumer.accept(makeEnchantedLightBlueColoredRoot(Items.OBSIDIAN, Items.CRYING_OBSIDIAN));
        consumer.accept(makeEnchantedLightBlueColoredRoot(Items.ENDER_PEARL, Items.ENDER_PEARL));

        consumer.accept(makeEnchantedBlueColoredRoot(ModItems.getArconiumEssence(RainbowColor.BLUE), Items.BLUE_WOOL));
        consumer.accept(makeEnchantedBlueColoredRoot(Items.BLUE_DYE, Items.BLUE_DYE));
        consumer.accept(makeEnchantedBlueColoredRoot(Items.NETHERRACK, Items.NETHER_BRICK));
        consumer.accept(makeEnchantedBlueColoredRoot(Items.SOUL_SAND, Items.SOUL_SAND));
        consumer.accept(makeEnchantedBlueColoredRoot(Items.SOUL_SOIL, Items.SOUL_SOIL));
        consumer.accept(makeEnchantedBlueColoredRoot(Items.BLAZE_ROD, Items.BLAZE_ROD));
        consumer.accept(makeEnchantedBlueColoredRoot(Items.WARPED_STEM, Items.WARPED_STEM, Items.WARPED_NYLIUM));
        consumer.accept(makeEnchantedLightBlueColoredRoot(Items.EMERALD, Items.EMERALD_BLOCK));
        consumer.accept(makeEnchantedLightBlueColoredRoot(Items.NETHER_WART, Items.NETHER_WART));

        consumer.accept(makeEnchantedPurpleColoredRoot(ModItems.getArconiumEssence(RainbowColor.PURPLE), Items.PURPLE_WOOL));
        consumer.accept(makeEnchantedPurpleColoredRoot(Items.PURPLE_DYE, Items.PURPLE_DYE));
        consumer.accept(makeEnchantedPurpleColoredRoot(Items.END_STONE, Items.END_STONE_BRICKS));
        consumer.accept(makeEnchantedPurpleColoredRoot(Items.CHORUS_FRUIT, Items.CHORUS_FLOWER));
        consumer.accept(makeEnchantedPurpleColoredRoot(Items.GHAST_TEAR, Items.GHAST_TEAR));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.ZOMBIE_SPAWN_EGG, Items.ZOMBIE_HEAD));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.CREEPER_SPAWN_EGG, Items.CREEPER_HEAD));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.SKELETON_SPAWN_EGG, Items.SKELETON_SKULL));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.SPIDER_SPAWN_EGG, Items.FERMENTED_SPIDER_EYE));

        consumer.accept(makeEnchantedMagentaColoredRoot(ModItems.getArconiumEssence(RainbowColor.MAGENTA), Items.MAGENTA_WOOL));
        consumer.accept(makeEnchantedMagentaColoredRoot(Items.MAGENTA_DYE, Items.MAGENTA_DYE));
        consumer.accept(makeEnchantedMagentaColoredRoot(Items.SHULKER_SHELL, Items.SHULKER_BOX));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.WITHER_SKELETON_SPAWN_EGG, Items.WITHER_SKELETON_SKULL));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.ZOMBIFIED_PIGLIN_SPAWN_EGG, Items.MUSIC_DISC_PIGSTEP));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.GHAST_SPAWN_EGG, Items.NETHERITE_HOE));

        consumer.accept(makeEnchantedPinkColoredRoot(ModItems.getArconiumEssence(RainbowColor.PINK), Items.PINK_WOOL));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.PINK_DYE, Items.PINK_DYE));
        consumer.accept(makeEnchantedPinkColoredRoot(Items.ENDERMAN_SPAWN_EGG, Items.ENDER_CHEST));

        RainbowColor.stream().forEach(color -> consumer.accept(makeGoldArconiumBlock(color)));
    }

    private static FinishedRecipe makeGoldArconiumBlock(RainbowColor color) {
        ItemStack output = new ItemStack(ModBlocks.getGoldArconiumBlock(color).asItem());
        Ingredient goldBlock = Ingredient.fromItems(Blocks.GOLD_BLOCK.asItem());
        Ingredient essence = Ingredient.fromItems(ModItems.getArconiumEssence(color));
        ResourceLocation recipeId = id(color.getTierName() + BlockNames.GOLD_ARCONIUM_BLOCK_SUFFIX);
        final int durationTicks = 100 + (color.getTier() * 100);
        return new FinishedRecipe(recipeId, output, durationTicks, goldBlock, essence, essence, essence, essence, essence, essence);
    }

    /**
     * Craft an enchanted colored root, which will be used to make a resource tree grow a specific resource
     *
     * @param item Colored root item to craft, which will be 'enchanted' through the altar ritual
     * @param resourceItem The item to be produced by the tree
     * @param durationTicks time the crafting recipe will take to make the root
     * @param ingredients
     * @return
     */
    private static FinishedRecipe makeEnchantedColoredRoot(ColoredRoot item, IItemProvider resourceItem, int durationTicks, Ingredient... ingredients) {
        ItemStack coloredRoot = new ItemStack(item);
        ColoredRoot.setResourceItem(coloredRoot, resourceItem);
        ResourceLocation rootId = Registry.ITEM.getKey(item);
        ResourceLocation itemId = Registry.ITEM.getKey(resourceItem.asItem());
        ResourceLocation recipeId = id(rootId.getPath() + "/" + itemId.getNamespace() + "_" + itemId.getPath());
        Arconia.logger.info("***** Recipe ID: " + recipeId);
        return new FinishedRecipe(recipeId, coloredRoot, durationTicks, ingredients);
    }

    /**
     * Generic recipe for enchanted red colored roots
     * @param resourceItem Resource to be generated by the resource tree, once activated with the resulting enchanted root
     * @param ingredient Required extra ingredient for crafting this enchanted root
     * @return
     */
    private static FinishedRecipe makeEnchantedRedColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.RED));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.RED), resourceItem, 200, root, clover,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedOrangeColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.ORANGE));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Ingredient arconium = Ingredient.fromItems(ModItems.getArconiumIngot(RainbowColor.RED));

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.ORANGE), resourceItem, 200, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedYellowColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.YELLOW));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Ingredient arconium = Ingredient.fromItems(ModItems.getArconiumIngot(RainbowColor.ORANGE));

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.YELLOW), resourceItem, 200, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedGreenColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.GREEN));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Ingredient arconium = Ingredient.fromItems(ModBlocks.getArconiumBlock(RainbowColor.YELLOW).asItem());

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.GREEN), resourceItem, 400, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedLightBlueColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.LIGHT_BLUE));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Ingredient arconium = Ingredient.fromItems(ModBlocks.getArconiumBlock(RainbowColor.GREEN).asItem());

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.LIGHT_BLUE), resourceItem, 400, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedBlueColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.BLUE));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Ingredient arconium = Ingredient.fromItems(ModBlocks.getArconiumBlock(RainbowColor.LIGHT_BLUE).asItem());

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.BLUE), resourceItem, 400, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedPurpleColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.PURPLE));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Item arconiumItem = ModBlocks.getArconiumBlock(RainbowColor.BLUE).asItem();
        final Ingredient arconium = Ingredient.fromItems(arconiumItem, arconiumItem, arconiumItem);

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.PURPLE), resourceItem, 800, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedMagentaColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.MAGENTA));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Item arconiumItem = ModBlocks.getArconiumBlock(RainbowColor.PURPLE).asItem();
        final Ingredient arconium = Ingredient.fromItems(arconiumItem, arconiumItem, arconiumItem);

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.MAGENTA), resourceItem, 800, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedPinkColoredRoot(IItemProvider resourceItem, Item... ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.PINK));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);
        final Item arconiumItem = ModBlocks.getArconiumBlock(RainbowColor.PINK).asItem();
        final Ingredient arconium = Ingredient.fromItems(arconiumItem, arconiumItem, arconiumItem);

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.PINK), resourceItem, 800, root, clover, arconium,
                Ingredient.fromItems(ingredient));
    }

    private static ResourceLocation id(String s) {
        return new ResourceLocation(Arconia.MOD_ID, "pedestal/" + s);
    }


    private static class FinishedRecipe implements IFinishedRecipe {
        private final ResourceLocation id;
        private final ItemStack output;
        private final Ingredient[] inputs;
        private final int durationTicks;

        private FinishedRecipe(ResourceLocation id, ItemStack output, int durationTicks, Ingredient... inputs) {
            this.id = id;
            this.output = output;
            this.durationTicks = durationTicks;
            this.inputs = inputs;
        }

        @Override
        public void serialize(JsonObject json) {
            json.add("output", serializeStack(output));
            json.add("durationTicks", new JsonPrimitive(durationTicks));
            JsonArray ingredients = new JsonArray();
            for (Ingredient ingredient : inputs) {
                ingredients.add(ingredient.serialize());
            }
            json.add("ingredients", ingredients);
        }

        /**
         * Serializes the given stack such that {@link net.minecraft.item.crafting.ShapedRecipe#deserializeItem}
         * would be able to read the result back
         */
        private static JsonObject serializeStack(ItemStack stack) {
            CompoundNBT nbt = stack.write(new CompoundNBT());
            byte c = nbt.getByte("Count");
            if (c != 1) {
                nbt.putByte("count", c);
            }
            nbt.remove("Count");
            renameTag(nbt, "id", "item");
            renameTag(nbt, "tag", "nbt");
            Dynamic<INBT> dyn = new Dynamic<>(NBTDynamicOps.INSTANCE, nbt);
            return dyn.convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
        }

        private static void renameTag(CompoundNBT nbt, String oldName, String newName) {
            INBT tag = nbt.get(oldName);
            if (tag != null) {
                nbt.remove(oldName);
                nbt.put(newName, tag);
            }
        }

        @Override
        public ResourceLocation getID() {
            return id;
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return ModRecipeTypes.PEDESTAL_SERIALIZER;
        }

        @Nullable
        @Override
        public JsonObject getAdvancementJson() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementID() {
            return null;
        }

    }
}
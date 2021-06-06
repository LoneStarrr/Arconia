package lonestarrr.arconia.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
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
        // All pedestal recipes additionally include time (in ticks) for the ritual to run.
        Ingredient fourLeafClover = Ingredient.fromItems(ModItems.fourLeafClover);
        Ingredient redColoredTreeRoot = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.RED));

        // First tier (red) colored roots - resource to be generated, ingredient needed for that
        consumer.accept(makeEnchantedRedColoredRoot(Items.COAL, Items.COAL));
        consumer.accept(makeEnchantedRedColoredRoot(Items.OAK_LOG, Items.OAK_LOG));
        consumer.accept(makeEnchantedRedColoredRoot(Items.SAND, Items.SAND));
        consumer.accept(makeEnchantedRedColoredRoot(Items.GRAVEL, Items.GRAVEL));
        consumer.accept(makeEnchantedRedColoredRoot(Items.STONE, Items.STONE));
        consumer.accept(makeEnchantedRedColoredRoot(Items.COBBLESTONE, Items.COBBLESTONE));
        consumer.accept(makeEnchantedRedColoredRoot(Items.DIRT, Items.DIRT));
        consumer.accept(makeEnchantedRedColoredRoot(ModItems.getArconiumEssence(RainbowColor.RED), Items.RED_DYE));
        consumer.accept(makeEnchantedOrangeColoredRoot(ModItems.getArconiumEssence(RainbowColor.ORANGE), Items.ORANGE_DYE));
    }

    private static FinishedRecipe make(IItemProvider item, int durationTicks, Ingredient... ingredients) {
        return new FinishedRecipe(idFor(Registry.ITEM.getKey(item.asItem())), new ItemStack(item), durationTicks, ingredients);
    }

    /**
     * Craft an enchanted colored root, which will be used to make a resource tree grow a specific resource
     *
     * @param item
     * @param resourceItem The item to be produced by the tree
     * @param durationTicks time the crafting recipe will take to make the root
     * @param ingredients
     * @return
     */
    private static FinishedRecipe makeEnchantedColoredRoot(ColoredRoot item, IItemProvider resourceItem, int durationTicks, Ingredient... ingredients) {
        ItemStack output = new ItemStack(item);
        ColoredRoot.setResourceItem(output, resourceItem);
        ResourceLocation rootID = Registry.ITEM.getKey(item);
        ResourceLocation itemID = Registry.ITEM.getKey(resourceItem.asItem());
        ResourceLocation recipeID = new ResourceLocation(rootID.getNamespace(), "pedestal/" + rootID.getPath() + "/" + itemID.getNamespace() + "_" + itemID.getPath());
        Arconia.logger.info("***** Recipe ID: " + recipeID);
        return new FinishedRecipe(recipeID, output, durationTicks, ingredients);
    }

    private static FinishedRecipe makeEnchantedRedColoredRoot(IItemProvider resourceItem, Item ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.RED));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.RED), resourceItem, 200, root, clover,
                Ingredient.fromItems(ingredient));
    }

    private static FinishedRecipe makeEnchantedOrangeColoredRoot(IItemProvider resourceItem, Item ingredient) {
        final Ingredient root = Ingredient.fromItems(ModItems.getColoredRoot(RainbowColor.ORANGE));
        final Ingredient clover = Ingredient.fromItems(ModItems.fourLeafClover);

        return makeEnchantedColoredRoot(ModItems.getColoredRoot(RainbowColor.ORANGE), resourceItem, 200, root, clover,
                Ingredient.fromItems(ingredient));
    }

    private static ResourceLocation idFor(ResourceLocation name) {
        return new ResourceLocation(name.getNamespace(), "pedestal/" + name.getPath());
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
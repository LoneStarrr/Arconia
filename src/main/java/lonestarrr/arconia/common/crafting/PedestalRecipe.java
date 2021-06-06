package lonestarrr.arconia.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PedestalRecipe implements IPedestalRecipe {
    private final ResourceLocation id;
    private final ItemStack output;
    private int durationTicks; // How long the ritual runs for
    private final NonNullList<Ingredient> inputs;

    public PedestalRecipe(ResourceLocation id, ItemStack output, int durationTicks, Ingredient... inputs) {
        this.id = id;
        this.output = output;
        this.durationTicks = durationTicks;
        this.inputs = NonNullList.from(Ingredient.EMPTY, inputs);
    }


    @Override
    public boolean matches(IInventory inv, World world) {
        List<Ingredient> missing = new ArrayList<>(inputs);

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack input = inv.getStackInSlot(i);
            if (input.isEmpty()) {
                continue;
            }

            int stackIndex = -1;

            for (int j = 0; j < missing.size(); j++) {
                Ingredient ingr = missing.get(j);
                if (ingr.test(input)) {
                    stackIndex = j;
                    break;
                }
            }

            if (stackIndex != -1) {
                missing.remove(stackIndex);
            } else {
                return false;
            }
        }

        return missing.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(IInventory iInventory) {
        return getRecipeOutput().copy();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public int getDurationTicks() {
        return durationTicks;
    }


    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<PedestalRecipe> {

        @Override
        public PedestalRecipe read(ResourceLocation id, JsonObject json) {
            // Serializer is in PedestalProvider which is part of data generation
            ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
            int durationTicks = JSONUtils.getInt(json, "durationTicks");
            JsonArray ingrs = JSONUtils.getJsonArray(json, "ingredients");
            List<Ingredient> inputs = new ArrayList<>();
            for (JsonElement e : ingrs) {
                inputs.add(Ingredient.deserialize(e));
            }
            return new PedestalRecipe(id, output, durationTicks, inputs.toArray(new Ingredient[0]));
        }

        @Nullable
        @Override
        public PedestalRecipe read(ResourceLocation id, PacketBuffer buf) {
            Ingredient[] inputs = new Ingredient[buf.readVarInt()];
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = Ingredient.read(buf);
            }
            ItemStack output = buf.readItemStack();
            int durationTicks = buf.readInt();
            return new PedestalRecipe(id, output, durationTicks, inputs);

        }

        @Override
        public void write(PacketBuffer buf, PedestalRecipe recipe) {
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient input : recipe.getIngredients()) {
                input.write(buf);
            }
            buf.writeItemStack(recipe.getRecipeOutput(), false);
            buf.writeInt(recipe.durationTicks);
        }
    }
}

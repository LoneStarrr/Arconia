package lonestarrr.arconia.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
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
        this.inputs = NonNullList.of(Ingredient.EMPTY, inputs);
    }


    @Override
    public boolean matches(Container inv, Level world) {
        List<Ingredient> missing = new ArrayList<>(inputs);

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack input = inv.getItem(i);
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
    public ItemStack assemble(Container iInventory) {
        return getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem() {
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
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<PedestalRecipe> {

        @Override
        public PedestalRecipe fromJson(ResourceLocation id, JsonObject json) {
            // Serializer is in PedestalProvider which is part of data generation
            ItemStack output = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int durationTicks = GsonHelper.getAsInt(json, "durationTicks");
            JsonArray ingrs = GsonHelper.getAsJsonArray(json, "ingredients");
            List<Ingredient> inputs = new ArrayList<>();
            for (JsonElement e : ingrs) {
                inputs.add(Ingredient.fromJson(e));
            }
            return new PedestalRecipe(id, output, durationTicks, inputs.toArray(new Ingredient[0]));
        }

        @Nullable
        @Override
        public PedestalRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient[] inputs = new Ingredient[buf.readVarInt()];
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = Ingredient.fromNetwork(buf);
            }
            ItemStack output = buf.readItem();
            int durationTicks = buf.readInt();
            return new PedestalRecipe(id, output, durationTicks, inputs);

        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PedestalRecipe recipe) {
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient input : recipe.getIngredients()) {
                input.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
            buf.writeInt(recipe.durationTicks);
        }
    }
}

package lonestarrr.arconia.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class
PedestalRecipe implements Recipe<PedestalInput> {
    private final ItemStack output;
    private final int durationTicks; // How long the ritual runs for
    private final List<Ingredient> ingredients;

    // Constructor used by codec
    public PedestalRecipe(ItemStack output, int durationTicks, List<Ingredient> ingredients) {
        this.output = output;
        this.durationTicks = durationTicks;
        this.ingredients = ingredients;
    }

    // Convenience constructor using varargs
    public PedestalRecipe(ItemStack output, int durationTicks, Ingredient... ingredients) {
        this(output, durationTicks, List.of(ingredients));
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.PEDESTAL_TYPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return output;
    }

    public ItemStack getOutput() { return output; }

    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.copyOf(ingredients);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(PedestalInput inv, Level world) {
        List<Ingredient> missing = new ArrayList<>(ingredients);

        for (int i = 0; i < inv.size(); i++) {
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
    public @NotNull ItemStack assemble(@NotNull PedestalInput pCraftingContainer, HolderLookup.@NotNull Provider pRegistries) {
        return getResultItem(pRegistries).copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.PEDESTAL_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<PedestalRecipe> {
        public static final StreamCodec<RegistryFriendlyByteBuf, PedestalRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );
        private MapCodec<PedestalRecipe> codec;

        @NotNull
        @Override
        public MapCodec<PedestalRecipe> codec() {
            if (codec == null) {
                codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("output").forGetter(PedestalRecipe::getOutput),
                        Codec.INT.fieldOf("durationTicks").forGetter(PedestalRecipe::getDurationTicks),
                        Ingredient.LIST_CODEC.fieldOf("ingredients").forGetter(PedestalRecipe::getIngredients)
                ).apply(instance, PedestalRecipe::new));
            }
            return codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, PedestalRecipe> streamCodec() {
            return STREAM_CODEC;
        }


        private static @NotNull PedestalRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
            Ingredient[] inputs = new Ingredient[buf.readVarInt()];
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            int durationTicks = buf.readInt();
            return new PedestalRecipe(output, durationTicks, inputs);

        }

        private static void toNetwork(@NotNull RegistryFriendlyByteBuf buf, @NotNull PedestalRecipe recipe) {
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient input : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, input);
            }
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.output);
            buf.writeInt(recipe.durationTicks);
        }
    }
}
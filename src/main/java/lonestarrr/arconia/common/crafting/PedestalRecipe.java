package lonestarrr.arconia.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PedestalRecipe implements Recipe<Container> {
    public static final RecipeSerializer<PedestalRecipe> SERIALIZER = new Serializer();
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
        return PedestalRecipe.Type.INSTANCE;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
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
    public boolean matches(Container inv, Level world) {
        List<Ingredient> missing = new ArrayList<>(ingredients);

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
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Type implements RecipeType<PedestalRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "pedestal";
    }

    public static class Serializer implements RecipeSerializer<PedestalRecipe> {
        private Codec<PedestalRecipe> codec;

        @NotNull
        @Override
        public Codec<PedestalRecipe> codec() {
            if (codec == null) {
                codec = RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("output").forGetter(PedestalRecipe::getOutput),
                        Codec.INT.fieldOf("durationTicks").forGetter(PedestalRecipe::getDurationTicks),
                        Ingredient.LIST_CODEC.fieldOf("ingredients").forGetter(PedestalRecipe::getIngredients)
                ).apply(instance, PedestalRecipe::new));
            }
            return codec;
        }


        @Override
        public @NotNull PedestalRecipe fromNetwork(@NotNull FriendlyByteBuf buf) {
            Ingredient[] inputs = new Ingredient[buf.readVarInt()];
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = Ingredient.fromNetwork(buf);
            }
            ItemStack output = buf.readItem();
            int durationTicks = buf.readInt();
            return new PedestalRecipe(output, durationTicks, inputs);

        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull PedestalRecipe recipe) {
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient input : recipe.getIngredients()) {
                input.toNetwork(buf);
            }
            buf.writeItem(recipe.getOutput());
            buf.writeInt(recipe.durationTicks);
        }
    }
}

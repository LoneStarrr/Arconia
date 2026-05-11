package lonestarrr.arconia.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PedestalRecipe implements Recipe<PedestalInput> {
  private final ItemStackTemplate output;
  private final int durationTicks;
  private final List<Ingredient> ingredients;
  @Nullable private final Item resourceItem;

  public static final MapCodec<PedestalRecipe> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      ItemStackTemplate.CODEC
                          .fieldOf("output")
                          .forGetter(PedestalRecipe::getOutputTemplate),
                      Codec.INT
                          .fieldOf("durationTicks")
                          .forGetter(PedestalRecipe::getDurationTicks),
                      Ingredient.CODEC
                          .listOf()
                          .fieldOf("ingredients")
                          .forGetter(PedestalRecipe::getIngredients),
                      Item.CODEC
                          .optionalFieldOf("resourceItem")
                          .forGetter(
                              r ->
                                  r.resourceItem != null
                                      ? java.util.Optional.of(
                                          r.resourceItem.builtInRegistryHolder())
                                      : java.util.Optional.empty()))
                  .apply(instance, PedestalRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, PedestalRecipe> STREAM_CODEC =
      StreamCodec.of(PedestalRecipe::toNetwork, PedestalRecipe::fromNetwork);

  public static final RecipeSerializer<PedestalRecipe> SERIALIZER =
      new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

  public PedestalRecipe(
      ItemStackTemplate output,
      int durationTicks,
      List<Ingredient> ingredients,
      @Nullable Item resourceItem) {
    this.output = output;
    this.durationTicks = durationTicks;
    this.ingredients = ingredients;
    this.resourceItem = resourceItem;
  }

  public PedestalRecipe(
      ItemStackTemplate output,
      int durationTicks,
      List<Ingredient> ingredients,
      Optional<Holder<Item>> resourceItemHolder) {
    this(output, durationTicks, ingredients, resourceItemHolder.map(Holder::value).orElse(null));
  }

  public PedestalRecipe(ItemStackTemplate output, int durationTicks, List<Ingredient> ingredients) {
    this(output, durationTicks, ingredients, (Item) null);
  }

  public PedestalRecipe(ItemStackTemplate output, int durationTicks, Ingredient... ingredients) {
    this(output, durationTicks, List.of(ingredients), (Item) null);
  }

  public PedestalRecipe(
      ItemStackTemplate output,
      int durationTicks,
      Ingredient[] ingredients,
      @Nullable Item resourceItem) {
    this(output, durationTicks, List.of(ingredients), resourceItem);
  }

  @Nonnull
  @Override
  public RecipeType<? extends Recipe<PedestalInput>> getType() {
    return ModRecipeTypes.PEDESTAL_TYPE.get();
  }

  @Override
  public PlacementInfo placementInfo() {
    return PlacementInfo.NOT_PLACEABLE;
  }

  @Override
  public RecipeBookCategory recipeBookCategory() {
    return RecipeBookCategories.CRAFTING_MISC;
  }

  public ItemStackTemplate getOutputTemplate() {
    return output;
  }

  public ItemStack getOutput() {
    return constructOutput();
  }

  public int getDurationTicks() {
    return durationTicks;
  }

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

  private ItemStack constructOutput() {
    if (resourceItem != null) {
      ItemStack resourceStack = new ItemStack(resourceItem);
      ItemContainerContents contents = ItemContainerContents.fromItems(List.of(resourceStack));
      DataComponentPatch patch =
          DataComponentPatch.builder().set(DataComponents.CONTAINER, contents).build();
      return output.apply(patch);
    }
    return output.create();
  }

  @Override
  public @NotNull ItemStack assemble(@NotNull PedestalInput input) {
    return constructOutput();
  }

  @Override
  public boolean showNotification() {
    return false;
  }

  @Override
  public String group() {
    return "";
  }

  @Override
  public @NotNull RecipeSerializer<? extends Recipe<PedestalInput>> getSerializer() {
    return ModRecipeTypes.PEDESTAL_SERIALIZER.get();
  }

  public static @NotNull PedestalRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
    Ingredient[] inputs = new Ingredient[buf.readVarInt()];
    for (int i = 0; i < inputs.length; i++) {
      inputs[i] = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
    }
    ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buf);
    int durationTicks = buf.readInt();
    boolean hasResource = buf.readBoolean();
    Item resourceItem =
        hasResource
            ? BuiltInRegistries.ITEM.get(buf.readIdentifier()).map(h -> h.value()).orElse(null)
            : null;
    return new PedestalRecipe(output, durationTicks, inputs, resourceItem);
  }

  public static void toNetwork(
      @NotNull RegistryFriendlyByteBuf buf, @NotNull PedestalRecipe recipe) {
    buf.writeVarInt(recipe.getIngredients().size());
    for (Ingredient input : recipe.getIngredients()) {
      Ingredient.CONTENTS_STREAM_CODEC.encode(buf, input);
    }
    ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.output);
    buf.writeInt(recipe.durationTicks);
    buf.writeBoolean(recipe.resourceItem != null);
    if (recipe.resourceItem != null) {
      buf.writeIdentifier(BuiltInRegistries.ITEM.getKey(recipe.resourceItem));
    }
  }
}

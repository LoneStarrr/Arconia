package lonestarrr.arconia.client.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.crafting.PedestalRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom altar recipe integration for JEI
 */
public class AltarRecipeCategory implements IRecipeCategory<PedestalRecipe> {
    public static final RecipeType<PedestalRecipe> TYPE =
            RecipeType.create(Arconia.MOD_ID, "pedestal", PedestalRecipe.class);
    private final IDrawable background;
    private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(ModBlocks.centerPedestal.get().asItem());

    public AltarRecipeCategory(@Nonnull IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(144, 81);
        this.overlay = guiHelper.createDrawable(new ResourceLocation(Arconia.MOD_ID, "textures/gui/jei/altar_overlay.png"),
                0, 0, 144, 81);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, renderStack.copy());
    }

    @Override
    public RecipeType<PedestalRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.arconia.recipe_category.altar");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(PedestalRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        overlay.draw(stack, 0, 0);
        RenderSystem.disableBlend();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PedestalRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> inputs = recipe.getIngredients();
        List<IRecipeSlotBuilder> inputSlots = new ArrayList<>(8);
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 7, 7));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 33, 3));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 59, 7));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 63, 33));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 59, 59));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 33, 62));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 7, 59));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 3, 33));
        for (int i = 0; i < inputs.size(); i++) {
            inputSlots.get(i).addIngredients(inputs.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 121, 33)
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }
}
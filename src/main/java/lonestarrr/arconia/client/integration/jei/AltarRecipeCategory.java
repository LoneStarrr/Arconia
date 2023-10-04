package lonestarrr.arconia.client.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.crafting.IPedestalRecipe;
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
public class AltarRecipeCategory implements IRecipeCategory<IPedestalRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Arconia.MOD_ID, "altar");
    public static final RecipeType<IPedestalRecipe> TYPE =
            RecipeType.create(Arconia.MOD_ID, "pedestal", IPedestalRecipe.class);
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
    public RecipeType<IPedestalRecipe> getRecipeType() {
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
    public void draw(IPedestalRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        overlay.draw(stack, 0, 0);
        RenderSystem.disableBlend();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IPedestalRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> inputs = recipe.getIngredients();
        List<IRecipeSlotBuilder> inputSlots = new ArrayList<>(8);
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 6, 6));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 32, 2));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 58, 6));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 62, 32));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 58, 58));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 32, 61));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 6, 58));
        inputSlots.add(builder.addSlot(RecipeIngredientRole.INPUT, 2, 32));
        for (int i= 0; i < inputs.size(); i++) {
            inputSlots.get(i).addIngredients(inputs.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 32)
                .addItemStack(recipe.getResultItem());
    }
}

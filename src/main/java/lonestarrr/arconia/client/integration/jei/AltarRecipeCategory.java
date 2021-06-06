package lonestarrr.arconia.client.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.crafting.IPedestalRecipe;
import lonestarrr.arconia.common.item.ModItems;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Custom altar recipe integration for JEI
 */
public class AltarRecipeCategory implements IRecipeCategory<IPedestalRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Arconia.MOD_ID, "altar");

    private final IDrawable background;
    private final String localizedName;
    private final IDrawable overlay;
    private final IDrawable icon;

    public AltarRecipeCategory(@Nonnull IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(144, 81);
        this.localizedName = I18n.format("jei.arconia.recipe_category.altar");
        this.overlay = guiHelper.createDrawable(new ResourceLocation(Arconia.MOD_ID, "textures/gui/jei/altar_overlay.png"),
                0, 0, 144, 81);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.centerPedestal.asItem()));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends IPedestalRecipe> getRecipeClass() {
        return IPedestalRecipe.class;
    }

    @Override
    public String getTitle() {
        return localizedName;
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
    public void draw(IPedestalRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        overlay.draw(matrixStack, 0, 0);
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
    }

    @Override
    public void setIngredients(IPedestalRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(recipe.getIngredients());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout layout, @Nonnull IPedestalRecipe recipe, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

        // Input slots, representing the surrounding pedestals
        stacks.init(0, true, 6, 6);
        stacks.init(1, true, 32, 2);
        stacks.init(2, true, 58, 6);
        stacks.init(3, true, 62, 32);
        stacks.init(4, true, 58, 58);
        stacks.init(5, true, 32, 61);
        stacks.init(6, true, 6, 58);
        stacks.init(7, true, 2, 32);

        // Output slot, representing the center pedestal
        stacks.init(8, false, 120, 32);

        for (int i = 0; i < inputs.size(); i++) {
            stacks.set(i, inputs.get(i));
        }

        stacks.set(8, outputs.get(0));
    }
}

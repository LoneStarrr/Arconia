package lonestarrr.arconia.client.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.ModItems;

@JeiPlugin
public class JEIArconiaPlugin implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(Arconia.MOD_ID, "main");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
            new AltarRecipeCategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        // The (main) blocks that process the recipes
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.centerPedestal.asItem()), AltarRecipeCategory.UID);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
//            registration.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.PEDESTAL_TYPE).values(), AltarRecipeCategory.UID);
            registration.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.PEDESTAL_TYPE).values(), AltarRecipeCategory.UID);
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Register items that are different because of NBT data, but share the same Item instance
        // Looks like you just give it some random unique string to differentiate them

        // Colored roots produced by the altar are differentiated on the resource they are associated with. Can be empty, but that should also serialize.
        for (RainbowColor tier: RainbowColor.values()) {
            ColoredRoot root = ModItems.getColoredRoot(tier);
            registration.registerSubtypeInterpreter(root, stack -> ColoredRoot.getResourceItem(stack).toString());
        }
    }
}

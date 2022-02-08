package lonestarrr.arconia.client.gui.crate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Items in crates are rendered like normal, but there is an additional bar under each item. Since crate slots can
 * store more than 64 items, this bar represents how full the slot actually is. The regular item renderer will still
 * display '64' for any slots with >= 64 items and I don't really want to mess with access modifiers to override that
 * behavior. Plus, I think it looks cool, and provides more opportunities for drawing rainbows!
 */
public class RainbowCrateItemRenderer extends ItemRenderer {
    public RainbowCrateItemRenderer(
            TextureManager textureManagerIn, ModelManager modelManagerIn,
            ItemColors itemColorsIn) {
        super(textureManagerIn, modelManagerIn, itemColorsIn);
    }

    @Override
    public void renderGuiItemDecorations(
            Font fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        super.renderGuiItemDecorations(fr, stack, xPosition, yPosition, text);
        // TODO Draw the bar here
    }
}

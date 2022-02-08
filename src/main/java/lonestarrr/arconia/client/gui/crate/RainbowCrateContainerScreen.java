package lonestarrr.arconia.client.gui.crate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.RainbowCrateBlock;
import lonestarrr.arconia.common.core.RainbowColor;

public class RainbowCrateContainerScreen extends AbstractContainerScreen<RainbowCrateContainer> {
    // Texture containing the background image and overlay sprites
    private static final ResourceLocation CRATE_UI_TEXTURE = new ResourceLocation(Arconia.MOD_ID, "textures/gui" +
            "/crate_ui.png");
    // offsets within the texture image
    private static final int BACKGROUND_OFFSET_X = 0;
    private static final int BACKGROUND_OFFSET_Y = 0;
    private static final int BACKGROUND_WIDTH = 242;
    private static final int BACKGROUND_HEIGHT = 256;
    private static final int CAPACITY_BAR_OFFSET_X = 0;
    private static final int CAPACITY_BAR_OFFSET_Y = 256;
    private static final int CAPACITY_BAR_WIDTH = 16;
    private static final int CAPACITY_BAR_HEIGHT = 3;
    // size of the actual image file
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 272;

    public RainbowCrateContainerScreen(RainbowCrateContainer container, Inventory playerInventory,
                                       Component title) {
        super(container, playerInventory, title);

        imageWidth = BACKGROUND_WIDTH;
        imageHeight = BACKGROUND_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        // Use our own item renderer to draw the little bars under each item in the GUI
        Minecraft mc = Minecraft.getInstance();
        this.itemRenderer = new RainbowCrateItemRenderer(mc.textureManager, mc.getModelManager(), mc.getItemColors());

        // Could add buttons here
        // this.addButton(new Button(this.guiLeft + this.xSize / 2 - 10, this.guiTop + 20, 50, 20, "1", null));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }

    /*
     * Each slot in the crate's inventory has a bar underneath displaying total capacity used, since a crate's slot
     * can actually contain more than 64 items.
     */
    private void drawSlotCapacityBars(PoseStack stack) {
        // coordinates to render at are slot.xPos and slot.yPos
        RainbowCrateContainer container = this.menu;
        // I think blit (invoked in the draw method) simply draws whatever texture is active.
        container.getCrateSlots().forEach(s -> drawCapacityBar(stack, s));
    }

    /**
     * Draw a capacity bar under each chest slot item indicating how many items the internal inventory has of the
     * same time.
     * @param s
     */
    private void drawCapacityBar(PoseStack stack, Slot s) {
        int maxCapacity = this.menu.getInternalSlotLimit();
        int itemCount = this.menu.getInternalSlotCount(s.index);

        final int SLOT_HEIGHT = 16;
        int barWidthPixels = (int)Math.floor((float)itemCount / maxCapacity * CAPACITY_BAR_WIDTH);
        if (itemCount > 0 && barWidthPixels == 0) {
            // Always display *something* if there are items in there, otherwise they appear 'missing'
            barWidthPixels = 1;
        }

        if (barWidthPixels == 0) {
            return;
        }

        int renderX = (this.width - this.imageWidth) / 2;
        int renderY = (this.height - this.imageHeight) / 2;
        // The slot has the location on the screen to draw to. The bar is positioned right under the slot.
        // blit: screen x, screen y, texture offset x, texture offset y, texture width, texture height
        blit(stack, renderX + s.x, renderY + s.y + SLOT_HEIGHT, CAPACITY_BAR_OFFSET_X,
                CAPACITY_BAR_OFFSET_Y, barWidthPixels, CAPACITY_BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     * Taken directly from ChestScreen
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        //TODO Remove me? I don't think I have anything interesting to draw here.
    }

    /**
     * Draws the background layer of this container (behind the items).
     * Taken directly from ChestScreen / BeaconScreen
     */
    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(CRATE_UI_TEXTURE);

        // Render background in the middle of the provided window (this.width/height)
        int edgeOffsetX = (this.width - this.imageWidth) / 2;
        int edgeOffsetY = (this.height - this.imageHeight) / 2;
        this.blit(stack, edgeOffsetX, edgeOffsetY, BACKGROUND_OFFSET_X, BACKGROUND_OFFSET_Y, this.imageWidth, this.imageHeight,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        drawSlotCapacityBars(stack);
    }

    /**
     * To be called while handling FMLClientSetupEvent
     */
    public static void registerContainerScreens() {
        for (RainbowColor tier : RainbowColor.values()) {
            MenuScreens.register(RainbowCrateBlock.getContainerTypeByTier(tier),
                    RainbowCrateContainerScreen::new);
        }
    }
}
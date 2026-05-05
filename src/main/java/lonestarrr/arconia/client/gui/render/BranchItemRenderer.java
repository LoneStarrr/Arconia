package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BranchItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation TREE_BRANCH_BASE_MODEL = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Arconia.MOD_ID, "item/tree_branch_base"));
    private final Minecraft minecraft = Minecraft.getInstance();

    public BranchItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        /* If this branch is imbued with an item, render a small version of the item behind the branch to differentiate it
         * from a plain branch, and also to quickly see what item it will have the pot of gold generate.
         */

        MultiBufferSource bufferSource;
        if (displayContext == ItemDisplayContext.GUI) {
            /* Getting (flat) items to render correctly in the UI with the right lighting was a pain. Of vital
             * importance that I missed is to create a fresh BufferSource. Not sure why, my render FU is weak.
             * Ended up learning this from Twilight Forest code, so thanks to the wonderful folks working on that!
             */
            bufferSource = minecraft.renderBuffers().bufferSource();
        } else {
            bufferSource = buffer;
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);

        int light = packedLight;

        if (displayContext == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
            light = LightTexture.FULL_BRIGHT;
        }

        // The branch texture is an item model, but it's not associated with an item, thus custom loaded.
        BakedModel bakedModel = minecraft.getModelManager().getModel(TREE_BRANCH_BASE_MODEL);
        // The (item) stack here is irrelevant, we're rendering the bakedModel.
        minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY, bakedModel.applyTransform(displayContext, poseStack, false));

        poseStack.popPose();

        // Render the imbued item as well
        var container = stack.get(DataComponents.CONTAINER);
        if (container != null && !container.getStackInSlot(0).isEmpty()) {
            ItemStack contained = container.getStackInSlot(0);
            poseStack.pushPose();

            poseStack.translate(0.8f, 0.65f, 0.6f);
            poseStack.scale(0.45f, 0.45f, 0.45f);

            minecraft.getItemRenderer().renderStatic(
                    contained,
                    displayContext,
                    light,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    minecraft.level,
                    0
            );

            poseStack.popPose();
        }

        if (displayContext == ItemDisplayContext.GUI) {
            ((MultiBufferSource.BufferSource)bufferSource).endBatch();
            Lighting.setupFor3DItems();
        }
    }
}

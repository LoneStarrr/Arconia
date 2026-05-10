package lonestarrr.arconia.client.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import org.joml.Vector3f;

/**
 * Renders the small "imbued" item perched on top of a colored tree branch — the per-stack overlay
 * that distinguishes a plain branch from an imbued one. The base branch model is rendered by the
 * surrounding {@code minecraft:composite} ClientItem definition; this renderer only handles the
 * dynamic overlay (the contained ItemStack from the {@link DataComponents#CONTAINER} component).
 */
public class BranchItemRenderer implements SpecialModelRenderer<ItemStack> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "branch_overlay");

    @Override
    public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> consumer) {
        // Not sure how to compute these sanely
        consumer.accept(new Vector3f(0.8f, 0.65f, 0.6f));
        consumer.accept(new Vector3f(0.8f + 0.45f, 0.65f + 0.45f, 0.6f + 0.45f));
    }

    @Nullable
    @Override
    public ItemStack extractArgument(ItemStack stack) {
        var container = stack.get(DataComponents.CONTAINER);
        if (container == null) {
            return null;
        }
        ItemStack contained = container.getStackInSlot(0);
        return contained.isEmpty() ? null : contained;
    }

    @Override
    public void submit(
            @Nullable ItemStack contained, ItemDisplayContext displayContext, PoseStack poseStack,
            SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
        if (contained == null || contained.isEmpty()) {
            return;
        }
        // Resolve the contained item's render state on every call. SpecialModelRenderer.submit is
        // invoked from inside another item's render pipeline, so re-using a cached ItemStackRenderState
        // across calls would clash with concurrent (or nested) renders. Reset/repopulate per call.
        Minecraft minecraft = Minecraft.getInstance();
        ItemStackRenderState state = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(state, contained, ItemDisplayContext.GUI, minecraft.level, null, 0);
        poseStack.pushPose();
        // The composite's first model already has applied the GUI/in-hand item transform for the
        // branch — we just position the contained-item overlay relative to that and let the inner
        // ItemStackRenderState.submit apply its own transforms again.
        poseStack.translate(0.7f, 0.60f, 0.6f);
        poseStack.scale(0.55f, 0.55f, 0.55f);
        state.submit(poseStack, nodeCollector, packedLight, packedOverlay, outlineColor);
        poseStack.popPose();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new BranchItemRenderer();
        }
    }
}

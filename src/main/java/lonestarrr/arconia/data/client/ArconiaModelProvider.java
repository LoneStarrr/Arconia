package lonestarrr.arconia.data.client;

import lonestarrr.arconia.client.gui.render.BranchItemRenderer;
import lonestarrr.arconia.client.item.MagicInABottleFilledProperty;
import lonestarrr.arconia.client.item.MagicInABottleTintSource;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

/**
 * Single 1.21.4 model provider replacing the old NeoForge BlockStateProvider + ItemModelProvider pair.
 *
 * <p>Block state JSONs are generated for every mod block, each pointing at the matching hand-authored
 * model under {@code src/main/resources/assets/arconia/models/block/}. Item models (the per-item layer
 * textures) likewise reference the hand-authored item-model templates. The new ClientItem JSONs
 * (under {@code assets/arconia/items/}) are emitted for every item, including the per-tier constant
 * tint sources, the {@code minecraft:range_dispatch} for {@link MagicInABottleFilledProperty}, and the
 * {@code minecraft:special} composite for the colored branches' {@link BranchItemRenderer}.
 */
public class ArconiaModelProvider extends ModelProvider {

    public ArconiaModelProvider(PackOutput output) {
        super(output, Arconia.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerSharedItemModels(itemModels);
        registerWorldBuilderBlockModel(blockModels);
        registerSingletonBlocks(blockModels);
        registerTieredBlocks(blockModels);
        registerStaticItems(itemModels);
        registerTieredItems(itemModels);
        registerMagicInABottle(itemModels);
        registerColoredBranches(itemModels);
    }

    private void registerSharedItemModels(ItemModelGenerators itemModels) {
        // Per-tier items reference these shared layered templates and apply a per-tier tint via the
        // ClientItem's tints array. These used to be authored by the legacy ItemModelProvider — emit
        // them here so the per-tier ClientItem JSONs find their parent model.
        ModelTemplates.FLAT_ITEM.create(prefix("item/arconium_essence"),
                TextureMapping.layer0(prefix("item/arconium_essence")),
                itemModels.modelOutput);
        ModelTemplates.FLAT_ITEM.create(prefix("item/arconium_ingot"),
                TextureMapping.layer0(prefix("item/arconium_ingot_white")),
                itemModels.modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(prefix("item/arconium_sickle"),
                TextureMapping.layered(prefix("item/sickle_handle"), prefix("item/sickle_head")),
                itemModels.modelOutput);
        ModelTemplates.FLAT_ITEM.create(prefix("item/tree_branch_base"),
                TextureMapping.layer0(prefix("item/colored_tree_branch")),
                itemModels.modelOutput);
        // Saplings use a flat item model (not the 3D cross block) — layer0 is the white sapling,
        // layer1 the un-tinted speckle overlay, both pulled from the existing block/ textures.
        ModelTemplates.TWO_LAYERED_ITEM.create(prefix("item/arconium_tree_sapling"),
                TextureMapping.layered(prefix("block/sapling_white"), prefix("block/sapling_speckles")),
                itemModels.modelOutput);
    }

    private void registerWorldBuilderBlockModel(BlockModelGenerators blockModels) {
        // Mirrors the legacy `models().cubeTop(name, oak_planks, arconia:block/world_builder)`:
        // a 3-textured cube whose top is the world_builder texture and whose sides+bottom are
        // vanilla oak planks. Emitted here because no hand-authored block/world_builder.json exists.
        TextureMapping mapping = new TextureMapping()
                .put(net.minecraft.client.data.models.model.TextureSlot.SIDE, ResourceLocation.withDefaultNamespace("block/oak_planks"))
                .put(net.minecraft.client.data.models.model.TextureSlot.TOP, prefix("block/world_builder"));
        ModelTemplates.CUBE_TOP.create(prefix("block/world_builder"), mapping, blockModels.modelOutput);
    }

    private void registerSingletonBlocks(BlockModelGenerators blockModels) {
        // Singleton blocks: blockstate references the hand-authored block model; the BlockItem gets
        // an auto-default ClientItem pointing to the same model.
        simpleBlockState(blockModels, ModBlocks.clover.get(), prefix("block/clover"));
        simpleBlockState(blockModels, ModBlocks.pedestal.get(), prefix("block/pedestal"));
        simpleBlockState(blockModels, ModBlocks.centerPedestal.get(), prefix("block/center_pedestal"));
        simpleBlockState(blockModels, ModBlocks.hat.get(), prefix("block/hat"));
        simpleBlockState(blockModels, ModBlocks.worldBuilder.get(), prefix("block/world_builder"));
        simpleBlockState(blockModels, ModBlocks.potMultiBlockPrimary.get(), prefix("block/pot_multiblock_primary"));
        // PotMultiBlockSecondary's PotPosition variants all render the empty model (the multiblock is
        // invisible — the primary block does the rendering), so a single blockstate variant suffices.
        simpleBlockState(blockModels, ModBlocks.potMultiBlockSecondary.get(), prefix("block/empty_model"));
    }

    private void registerTieredBlocks(BlockModelGenerators blockModels) {
        for (RainbowColor color : RainbowColor.values()) {
            ArconiumColorAssets assets = forColor(color);
            // Each tier-block re-uses one shared block model from src/main/resources, parameterised
            // by the per-tier tint that the in-world block color handler applies.
            simpleBlockState(blockModels, ModBlocks.getArconiumTreeLeaves(color).get(), prefix("block/arconium_tree_leaves"));
            simpleBlockState(blockModels, ModBlocks.getArconiumTreeSapling(color).get(), prefix("block/cross_sapling"));
            simpleBlockState(blockModels, ModBlocks.getArconiumBlock(color).get(), prefix("block/arconium_block"));
            simpleBlockState(blockModels, ModBlocks.getRainbowGrassBlock(color).get(), prefix("block/rainbow_grass_block"));

            // Tinted item models for the corresponding BlockItems — overrides the auto-default with a
            // ClientItem that carries the per-tier constant tint source.
            tintedFlatItem(blockModels.itemModelOutput, ModBlocks.getArconiumTreeLeaves(color).get().asItem(), assets.itemModelLeaves(), assets.tint());
            tintedFlatItem(blockModels.itemModelOutput, ModBlocks.getArconiumTreeSapling(color).get().asItem(), assets.itemModelSapling(), assets.tint(), saplingOverlayTint());
            tintedFlatItem(blockModels.itemModelOutput, ModBlocks.getArconiumBlock(color).get().asItem(), assets.itemModelArconiumBlock(), assets.tint());
            tintedFlatItem(blockModels.itemModelOutput, ModBlocks.getRainbowGrassBlock(color).get().asItem(), assets.itemModelGrassBlock(), assets.tint());
        }
    }

    private void registerStaticItems(ItemModelGenerators itemModels) {
        // Items with a hand-authored model template — emit a plain ClientItem pointing at it.
        itemModels.itemModelOutput.accept(ModItems.cloverStaff.get(), ItemModelUtils.plainModel(prefix("item/clover_staff")));
        itemModels.itemModelOutput.accept(ModItems.fourLeafClover.get(), ItemModelUtils.plainModel(prefix("item/four_leaf_clover")));
        itemModels.itemModelOutput.accept(ModItems.threeLeafClover.get(), ItemModelUtils.plainModel(prefix("item/three_leaf_clover")));
    }

    private void registerTieredItems(ItemModelGenerators itemModels) {
        // Per-tier items that share a single layered model — the per-tier color comes from the
        // constant tint source on the relevant layer(s).
        for (RainbowColor color : RainbowColor.values()) {
            ArconiumColorAssets assets = forColor(color);
            tintedFlatItem(itemModels.itemModelOutput, ModItems.getArconiumEssence(color).get(), prefix("item/arconium_essence"), assets.tint());
            tintedFlatItem(itemModels.itemModelOutput, ModItems.getArconiumIngot(color).get(), prefix("item/arconium_ingot"), assets.tint());
            tintedFlatItem(itemModels.itemModelOutput, ModItems.getArconiumSickle(color).get(), prefix("item/arconium_sickle"), sickleHandleTint(), assets.tint());
        }
    }

    private void registerMagicInABottle(ItemModelGenerators itemModels) {
        // Thresholds match the legacy 1.21.1 model overrides exactly: 8 sub-models across the
        // 0..1 filled ratio, fallback (empty bottle) below the lowest threshold. Each sub-model
        // (and the fallback) is a layered model whose layer1 is the "swirl" overlay — tinted by
        // MagicInABottleTintSource so the swirl colour matches the bottle's stored tier.
        ItemTintSource layer0Untinted = ItemModelUtils.constantTint(-1);
        ItemTintSource swirlTint = new MagicInABottleTintSource();
        ItemModel.Unbaked fallback = ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle"), layer0Untinted, swirlTint);
        List<RangeSelectItemModel.Entry> entries = List.of(
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled1"), layer0Untinted, swirlTint), 0.12f),
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled2"), layer0Untinted, swirlTint), 0.25f),
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled3"), layer0Untinted, swirlTint), 0.37f),
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled4"), layer0Untinted, swirlTint), 0.50f),
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled5"), layer0Untinted, swirlTint), 0.62f),
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled6"), layer0Untinted, swirlTint), 0.75f),
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled7"), layer0Untinted, swirlTint), 0.87f),
                ItemModelUtils.override(ItemModelUtils.tintedModel(prefix("item/magic_in_a_bottle_filled8"), layer0Untinted, swirlTint), 1.00f)
        );
        ItemModel.Unbaked rangeSelect = ItemModelUtils.rangeSelect(new MagicInABottleFilledProperty(), fallback, entries);
        itemModels.itemModelOutput.accept(ModItems.magicInABottle.get(), rangeSelect);
    }

    private void registerColoredBranches(ItemModelGenerators itemModels) {
        // The colored branch ClientItem is a composite: a tinted base item-model plus the special
        // renderer that draws the contained item overlay on top.
        ResourceLocation base = prefix("item/tree_branch_base");
        for (RainbowColor color : RainbowColor.values()) {
            ItemTintSource tint = ItemModelUtils.constantTint(color.getColorValue());
            ItemModel.Unbaked baseModel = ItemModelUtils.tintedModel(base, tint);
            ItemModel.Unbaked overlay = ItemModelUtils.specialModel(base, new BranchItemRenderer.Unbaked());
            ItemModel.Unbaked composite = ItemModelUtils.composite(baseModel, overlay);
            itemModels.itemModelOutput.accept(ModItems.getColoredBranch(color).get(), composite);
        }
    }

    // ---------- helpers ----------

    private void simpleBlockState(BlockModelGenerators blockModels, Block block, ResourceLocation modelLocation) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(modelLocation)));
    }

    /** Constant per-layer tint for a flat (sprite-based) item model living at {@code modelLocation}. */
    private void tintedFlatItem(ItemModelOutput output, Item item, ResourceLocation modelLocation, ItemTintSource... tints) {
        output.accept(item, ItemModelUtils.tintedModel(modelLocation, tints));
    }

    private static ItemTintSource saplingOverlayTint() {
        // Layer1 of the sapling model is the speckles overlay — kept un-tinted (white).
        return ItemModelUtils.constantTint(-1);
    }

    private static ItemTintSource sickleHandleTint() {
        // Layer0 of the sickle is the wooden handle — kept un-tinted (white).
        return ItemModelUtils.constantTint(-1);
    }

    private static ArconiumColorAssets forColor(RainbowColor color) {
        return new ArconiumColorAssets(color);
    }

    private record ArconiumColorAssets(RainbowColor color) {
        ItemTintSource tint() {
            return ItemModelUtils.constantTint(color.getColorValue());
        }

        // For block items the in-inventory icon is a flat 3D render of the block model itself,
        // tinted per-tier — no separate per-color item model needed.
        ResourceLocation itemModelLeaves() {
            return prefix("block/arconium_tree_leaves");
        }

        // Saplings are an exception — they render as flat sprites (not the 3D cross), so they
        // reference the shared flat item template emitted by registerSharedItemModels().
        ResourceLocation itemModelSapling() {
            return prefix("item/arconium_tree_sapling");
        }

        ResourceLocation itemModelArconiumBlock() {
            return prefix("block/arconium_block");
        }

        ResourceLocation itemModelGrassBlock() {
            return prefix("block/rainbow_grass_block");
        }
    }
}

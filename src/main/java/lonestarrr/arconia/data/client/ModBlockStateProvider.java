package lonestarrr.arconia.data.client;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.RainbowCropBlock;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import org.lwjgl.system.CallbackI;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

import javax.annotation.Nonnull;

/**
 * Dynamically generate blockstates, block models, and item models by invoking the runData gradle target.
 */
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Arconia.MOD_ID, exFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Arconia BlockStates";
    }

    @Override
    protected void registerStatesAndModels() {
        //TODO: default language too?
        registerLeaves();
        registerTreeRoots();
        registerSaplings();
        registerCrates();
        registerCrops();
        registerArconiumBlocks();
    }

    private void registerCrops() {
        // The first few stages of each crop are identical
        ModelFile modelStage0 = models().withExistingParent("block/rainbow_crop_stage0", "block/cross").texture("cross", prefix("block/rainbow_crop_stage0"));
        ModelFile modelStage1 = models().withExistingParent("block/rainbow_crop_stage1", "block/cross").texture("cross", prefix("block/rainbow_crop_stage1"));
        ModelFile modelStage2 = models().withExistingParent("block/rainbow_crop_stage2", "block/cross").texture("cross", prefix("block/rainbow_crop_stage2"));

        for (RainbowColor color: RainbowColor.values()) {
            Block block = ModBlocks.getRainbowCrop(color);
            String name = Registry.BLOCK.getKey(block).getPath();
            // TODO later stages should also be generic models using overlay with tints instead (doing that for tree roots already)
            ModelFile modelStage3 = models().withExistingParent("block/" + color.getTierName() + "_rainbow_crop_stage3", "block/cross")
                    .texture("cross", prefix("block/" + color.getTierName() + "_rainbow_crop_stage3"));
            ModelFile modelStage4 = models().withExistingParent("block/" + color.getTierName() + "_rainbow_crop_stage4", "block/cross")
                    .texture("cross", prefix("block/" + color.getTierName() + "_rainbow_crop_stage4"));
            getVariantBuilder(block)
                    .partialState().with(RainbowCropBlock.CROP_AGE, 0).addModels(new ConfiguredModel(modelStage0))
                    .partialState().with(RainbowCropBlock.CROP_AGE, 1).addModels(new ConfiguredModel(modelStage1))
                    .partialState().with(RainbowCropBlock.CROP_AGE, 2).addModels(new ConfiguredModel(modelStage2))
                    .partialState().with(RainbowCropBlock.CROP_AGE, 3).addModels(new ConfiguredModel(modelStage3))
                    .partialState().with(RainbowCropBlock.CROP_AGE, 4).addModels(new ConfiguredModel(modelStage4));
            // No item models for crops. They have seeds instead.
        }
    }

    private void registerCrates() {
        for (RainbowColor color: RainbowColor.values()) {
            Block block = ModBlocks.getRainbowCrateBlock(color);
            String name = Registry.BLOCK.getKey(block).getPath();
            ResourceLocation top = prefix("block/rainbow_crate_" + color.getTierName() + "_top");
            ResourceLocation sides = prefix("block/rainbow_crate_" + color.getTierName() + "_sides");
            ModelFile model = models().cubeTop("block/" + name, sides, top);
            horizontalBlock(block, model);
            itemModels().withExistingParent(name, prefix("block/" + name));
        }
    }

    private void registerSaplings() {
        for (RainbowColor color: RainbowColor.values()) {
            Block block = ModBlocks.getMoneyTreeSapling(color);
            String name = Registry.BLOCK.getKey(block).getPath();
            String texturePath = "block/" + color.getTierName() + BlockNames.SAPLING_SUFFIX;
            ModelFile model = models().withExistingParent(name, "block/cross")
                    .texture("cross", prefix(texturePath));
            simpleBlock(block, model);
            itemModels().withExistingParent(name, "item/generated")
                    .texture("layer0", prefix(texturePath));
        }
    }

    private void registerTreeRoots() {
        //Tree roots
        for (RainbowColor color: RainbowColor.values()) {
            Block rootBlock = ModBlocks.getResourceTreeRootBlock(color);
            String rootName = Registry.BLOCK.getKey(rootBlock).getPath();
            ModelFile rootModel = models().getExistingFile(prefix("block/tree_root_block"));
            horizontalBlock(rootBlock, rootModel);
            itemModels().withExistingParent(rootName, prefix("block/tree_root_block"));
        }

    }
    private void registerLeaves() {
        //Money tree leaves
        //TODO: single texture for all, use tints to dynamically color - like TreeRoots (probably use a manual model file)
        for (RainbowColor color: RainbowColor.values()) {
            Block leafBlock = ModBlocks.getMoneyTreeLeaves(color);
            String leafName = Registry.BLOCK.getKey(leafBlock).getPath();
            ModelFile leafModel = models().getExistingFile(prefix("block/resource_tree_leaves"));
            simpleBlock(leafBlock, leafModel);
            itemModels().withExistingParent(leafName, prefix("block/resource_tree_leaves"));
        }
    }

    private void registerArconiumBlocks() {
        for (RainbowColor color: RainbowColor.values()) {
            Block block = ModBlocks.getArconiumBlock(color);
            String name = Registry.BLOCK.getKey(block).getPath();
            ModelFile model = models().cubeAll(name, prefix("block/" + color.getTierName() + BlockNames.ARCONIUM_BLOCK_SUFFIX));
            simpleBlock(block, model);
            itemModels().withExistingParent(name, prefix("block/" + name));
        }
    }
}

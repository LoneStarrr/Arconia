package lonestarrr.arconia.data.client;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

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
        registerSaplings();
        registerArconiumBlocks();
        registerInfiniteGoldArconiumBlocks();
    }

    private void registerSaplings() {
        final String texturePath = "block/sapling_white";
        // Model is a copy of minecraft's block/cross, with added gold speckles overlay and tint index.
        ModelFile model = models().getExistingFile(prefix("block/cross_sapling"));

        for (RainbowColor color: RainbowColor.values()) {
            Block block = ModBlocks.getArconiumTreeSapling(color);
            String name = Registry.BLOCK.getKey(block).getPath();
            simpleBlock(block, model);
            // Item model here does not use the block model, but instead the flat texture(s)
            itemModels().withExistingParent(name, "item/generated")
                    .texture("layer0", prefix(texturePath))
                    .texture("layer1", prefix("block/sapling_speckles"));
        }
    }

    private void registerLeaves() {
        //Arconium tree leaves
        //TODO: single texture for all, use tints to dynamically color - like TreeRoots (probably use a manual model file)
        for (RainbowColor color: RainbowColor.values()) {
            Block leafBlock = ModBlocks.getArconiumTreeLeaves(color);
            String leafName = Registry.BLOCK.getKey(leafBlock).getPath();
            ModelFile leafModel = models().getExistingFile(prefix("block/arconium_tree_leaves"));
            simpleBlock(leafBlock, leafModel);
            itemModels().withExistingParent(leafName, prefix("block/arconium_tree_leaves"));
        }
    }

    private void registerArconiumBlocks() {
        for (RainbowColor color: RainbowColor.values()) {
            Block block = ModBlocks.getArconiumBlock(color);
            String name = Registry.BLOCK.getKey(block).getPath();
            //ModelFile model = models().cubeAll(name, prefix("block/arconium_block"));
            ModelFile model = models().getExistingFile(prefix("block/arconium_block"));
            simpleBlock(block, model);
            itemModels().withExistingParent(name, prefix("block/arconium_block"));
        }
    }

    private void registerInfiniteGoldArconiumBlocks() {
        for (RainbowColor color: RainbowColor.values()) {
            Block block = ModBlocks.getInfiniteGoldArconiumBlock(color);
            String name = Registry.BLOCK.getKey(block).getPath();
            ModelFile model = models().getExistingFile(prefix("block/infinite_gold_arconium_block"));
            simpleBlock(block, model);
            itemModels().withExistingParent(name, prefix("block/infinite_gold_arconium_block"));
        }
    }
}

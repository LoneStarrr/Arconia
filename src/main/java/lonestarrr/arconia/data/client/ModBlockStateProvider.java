package lonestarrr.arconia.data.client;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.*;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

/**
 * Dynamically generate blockstates, block models, and item models by invoking the runData gradle target.
 */
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Arconia.MOD_ID, exFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Arconia BlockStates";
    }

    @Override
    protected void registerStatesAndModels() {
        registerLeaves();
        registerRainbowGrassBlocks();
        registerSaplings();
        registerArconiumBlocks();
        registerInfiniteGoldArconiumBlocks();
        registerMisc();
    }

    private void registerSaplings() {
        final String texturePath = "block/sapling_white";
        // Model is a copy of minecraft's block/cross, with added gold speckles overlay and tint index.
        ModelFile model = models().getExistingFile(prefix("block/cross_sapling"));

        for (RainbowColor color: RainbowColor.values()) {
            RegistryObject<ArconiumTreeSapling> block = ModBlocks.getArconiumTreeSapling(color);
            String name = block.getId().getPath();
            simpleBlock(block.get(), model);
            // Item model here does not use the block model, but instead the flat texture(s)
            itemModels().withExistingParent(name, "item/generated")
                    .texture("layer0", prefix(texturePath))
                    .texture("layer1", prefix("block/sapling_speckles"));
        }
    }

    private void registerLeaves() {
        for (RainbowColor color: RainbowColor.values()) {
            RegistryObject<ArconiumTreeLeaves> leafBlock = ModBlocks.getArconiumTreeLeaves(color);
            String leafName = leafBlock.getId().getPath();
            ModelFile leafModel = models().getExistingFile(prefix("block/arconium_tree_leaves"));
            simpleBlock(leafBlock.get(), leafModel);
            itemModels().withExistingParent(leafName, prefix("block/arconium_tree_leaves"));
        }
    }

    private void registerRainbowGrassBlocks() {
        for (RainbowColor color: RainbowColor.values()) {
            RegistryObject<RainbowGrassBlock> grassBlock = ModBlocks.getRainbowGrassBlock(color);
            String grassName = grassBlock.getId().getPath();
            ModelFile grasssModel = models().getExistingFile(prefix("block/rainbow_grass_block"));
            simpleBlock(grassBlock.get(), grasssModel);
            itemModels().withExistingParent(grassName, prefix("block/rainbow_grass_block"));
        }
    }

    private void registerArconiumBlocks() {
        for (RainbowColor color: RainbowColor.values()) {
            RegistryObject<ArconiumBlock> block = ModBlocks.getArconiumBlock(color);
            String name = block.getId().getPath();
            //ModelFile model = models().cubeAll(name, prefix("block/arconium_block"));
            ModelFile model = models().getExistingFile(prefix("block/arconium_block"));
            simpleBlock(block.get(), model);
            itemModels().withExistingParent(name, prefix("block/arconium_block"));
        }
    }

    private void registerInfiniteGoldArconiumBlocks() {
        for (RainbowColor color: RainbowColor.values()) {
            RegistryObject<InfiniteGoldArconiumBlock> block = ModBlocks.getInfiniteGoldArconiumBlock(color);
            String name = block.getId().getPath();
            ModelFile model = models().getExistingFile(prefix("block/infinite_gold_arconium_block"));
            simpleBlock(block.get(), model);
            itemModels().withExistingParent(name, prefix("block/infinite_gold_arconium_block"));
        }
    }

    private void registerMisc() {
        //World Builder
        RegistryObject<WorldBuilder> block = ModBlocks.worldBuilder;
        String name = block.getId().getPath();
        ModelFile model = models().cubeTop(name, new ResourceLocation("block/oak_planks"), prefix("block/world_builder"));
        simpleBlock(block.get(), model);
        itemModels().withExistingParent(name, prefix("block/world_builder"));
    }
}

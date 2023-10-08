package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, Arconia.MOD_ID, helper);
    }

    @Override
    protected void addTags() {
        for (RainbowColor tier: RainbowColor.values()) {
            tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.getArconiumBlock(tier).get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.getArconiumBlock(tier).get());

            tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.getInfiniteGoldArconiumBlock(tier).get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.getInfiniteGoldArconiumBlock(tier).get());

            tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.getArconiumTreeLeaves(tier).get());

            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.worldBuilder.get());
            tag(BlockTags.LEAVES).add(ModBlocks.getArconiumTreeLeaves(tier).get());
            tag(BlockTags.SAPLINGS).add(ModBlocks.getArconiumTreeSapling(tier).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.hat.get());

    }

    @Override
    public String getName() {
        return "Arconia Block Tags";
    }
}

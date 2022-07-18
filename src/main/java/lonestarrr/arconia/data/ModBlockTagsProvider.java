package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, Arconia.MOD_ID, helper);
    }

    @Override
    protected void addTags() {
        for (RainbowColor tier: RainbowColor.values()) {
            tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.getArconiumBlock(tier));
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.getArconiumBlock(tier));

            tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.getGoldArconiumBlock(tier));
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.getGoldArconiumBlock(tier));

            tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.getInfiniteGoldArconiumBlock(tier));
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.getInfiniteGoldArconiumBlock(tier));

            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.getArconiumTreeRootBlocks(tier));
        }

    }

    @Override
    public String getName() {
        return "Arconia Block Tags";
    }
}

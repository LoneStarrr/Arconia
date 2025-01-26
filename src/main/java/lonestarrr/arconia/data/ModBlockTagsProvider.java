package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider,  ExistingFileHelper helper) {
        super(output, provider, Arconia.MOD_ID, helper);
    }

    @Override
    public @NotNull String getName() {
        return "Arconia Block Tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (RainbowColor tier: RainbowColor.values()) {
            tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.getArconiumBlock(tier).get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.getArconiumBlock(tier).get());

            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.centerPedestal.get());

            tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.getArconiumTreeLeaves(tier).get());

            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.worldBuilder.get());
            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.pedestal.get());

            tag(BlockTags.LEAVES).add(ModBlocks.getArconiumTreeLeaves(tier).get());
            tag(BlockTags.SAPLINGS).add(ModBlocks.getArconiumTreeSapling(tier).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.hat.get());
    }
}

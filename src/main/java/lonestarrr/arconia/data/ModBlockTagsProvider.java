package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(
            DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Arconia.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        // Register leaves - required as otherwise placing a new tree won't work as it does not recognize the leaves as such.
        RainbowColor.stream().forEach(color -> this.tag(BlockTags.LEAVES).add(ModBlocks.getArconiumTreeLeaves(color)));
    }
}

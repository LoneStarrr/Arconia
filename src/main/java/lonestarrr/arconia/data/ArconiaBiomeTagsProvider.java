package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.Tags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ArconiaBiomeTagsProvider extends BiomeTagsProvider {

    public ArconiaBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, Arconia.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider lookupProvider) {
        tag(Tags.Biomes.HAS_CLOVER)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_DECIDUOUS_TREE)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_CONIFEROUS_TREE)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_JUNGLE_TREE)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_FLORAL)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_PLAINS)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_HILL)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_OLD_GROWTH)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_SAVANNA)
        ;
        tag(Tags.Biomes.HAS_ARCONIUM_TREES)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_DECIDUOUS_TREE)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_CONIFEROUS_TREE)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_JUNGLE_TREE)
                .addTag(net.neoforged.neoforge.common.Tags.Biomes.IS_OLD_GROWTH)
                ;
    }

}

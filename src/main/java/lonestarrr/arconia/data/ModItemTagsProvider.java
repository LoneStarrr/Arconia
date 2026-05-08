package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, Arconia.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        /* Adding clover staff to this tag makes it eligible for the fortune enchant */
        tag(ItemTags.MINING_LOOT_ENCHANTABLE).add(ModItems.cloverStaff.get());
    }
}

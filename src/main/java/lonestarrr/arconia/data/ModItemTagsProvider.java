package lonestarrr.arconia.data;

import java.util.concurrent.CompletableFuture;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

public class ModItemTagsProvider extends ItemTagsProvider {

  public ModItemTagsProvider(
      PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(output, lookupProvider, Arconia.MOD_ID);
  }

  @Override
  protected void addTags(HolderLookup.Provider provider) {
    /* Adding clover staff to this tag makes it eligible for the fortune enchant */
    tag(ItemTags.MINING_LOOT_ENCHANTABLE).add(ModItems.cloverStaff.get());
  }
}

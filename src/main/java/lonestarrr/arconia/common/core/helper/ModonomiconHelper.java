package lonestarrr.arconia.common.core.helper;

import com.klikli_dev.modonomicon.registry.DataComponentRegistry;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class ModonomiconHelper {
  public static final Identifier GUIDE_BOOK_ID =
      Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "guide_book");

  public static ItemStack createGuideBook() {
    ItemStack bookStack = new ItemStack(ModItems.guideBook.get());
    // ModonomiconCustomItemBase only assigns BOOK_ID lazily on first getBookFor() call (e.g. from
    // getName()/use()) - set it explicitly here so it's present immediately for advancement
    // criteria that pattern-match on it (see AdvancementSubProvider).
    bookStack.set(DataComponentRegistry.BOOK_ID.get(), GUIDE_BOOK_ID);
    return bookStack;
  }

  public static ItemStackTemplate createGuideBookTemplate() {
    DataComponentPatch patch =
        DataComponentPatch.builder().set(DataComponentRegistry.BOOK_ID.get(), GUIDE_BOOK_ID).build();
    return new ItemStackTemplate(ModItems.guideBook.get(), 1, patch);
  }

  public static boolean isGuideBook(ItemStack itemStack) {
    return itemStack.getItem() == ModItems.guideBook.get();
  }
}

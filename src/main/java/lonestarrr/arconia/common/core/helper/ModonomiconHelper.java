package lonestarrr.arconia.common.core.helper;

import com.klikli_dev.modonomicon.registry.DataComponentRegistry;
import com.klikli_dev.modonomicon.registry.ItemRegistry;
import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class ModonomiconHelper {
  public static final Identifier GUIDE_BOOK_ID =
      Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "guide_book");

  public static ItemStack createGuideBook() {
    ItemStack bookStack = new ItemStack(ItemRegistry.MODONOMICON.get());
    bookStack.set(DataComponentRegistry.BOOK_ID.get(), GUIDE_BOOK_ID);
    return bookStack;
  }

  public static ItemStackTemplate createGuideBookTemplate() {
    DataComponentPatch patch =
        DataComponentPatch.builder().set(DataComponentRegistry.BOOK_ID.get(), GUIDE_BOOK_ID).build();
    return new ItemStackTemplate(ItemRegistry.MODONOMICON.get(), 1, patch);
  }

  public static boolean isGuideBook(ItemStack itemStack) {
    if (itemStack.getItem() != ItemRegistry.MODONOMICON.get()) {
      return false;
    }
    Identifier bookId = itemStack.get(DataComponentRegistry.BOOK_ID.get());
    return GUIDE_BOOK_ID.equals(bookId);
  }
}

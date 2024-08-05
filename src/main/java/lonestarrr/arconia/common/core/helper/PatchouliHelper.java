package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PatchouliHelper {
    public static final String GUIDE_BOOK_ID = new ResourceLocation(Arconia.MOD_ID, "guide_book").toString(); //Also hardcoded in advancement icon
    public static final ResourceLocation PATCHOULI_GUIDE_BOOK = new ResourceLocation("patchouli", "guide_book");
    public static final String TAG_PATCHOULI_BOOK = "patchouli:book";

    public static ItemStack createGuideBook() {
        Item bookItem = BuiltInRegistries.ITEM.get(PATCHOULI_GUIDE_BOOK);
        ItemStack bookStack = new ItemStack(bookItem);
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_PATCHOULI_BOOK, GUIDE_BOOK_ID);
        bookStack.setTag(tag);
        return bookStack;
    }

    public static boolean isGuideBook(ItemStack itemStack) {
        ResourceLocation itemResLoc = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (!itemResLoc.equals(PATCHOULI_GUIDE_BOOK)) {
            return false;
        }
        CompoundTag tag = itemStack.getTag();
        String bookId = tag.getString(TAG_PATCHOULI_BOOK);
        return (bookId.equals(GUIDE_BOOK_ID));
    }
}

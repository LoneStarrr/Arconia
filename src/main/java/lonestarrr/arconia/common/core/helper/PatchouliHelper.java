package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class PatchouliHelper {
    public static final String GUIDE_BOOK_ID = new ResourceLocation(Arconia.MOD_ID, "guide_book").toString(); //Also hardcoded in advancement icon
    public static final ResourceLocation PATCHOULI_GUIDE_BOOK = new ResourceLocation("patchouli", "guide_book");
    public static final String TAG_PATCHOULI_BOOK = "patchouli:book";

    public static ItemStack createGuideBook() {
        Item bookItem = ForgeRegistries.ITEMS.getValue(PATCHOULI_GUIDE_BOOK);
        ItemStack bookStack = new ItemStack(bookItem);
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_PATCHOULI_BOOK, GUIDE_BOOK_ID);
        bookStack.setTag(tag);
        return bookStack;
    }

    public static boolean isGuideBook(ItemStack itemStack) {
        if (!itemStack.getItem().getRegistryName().equals(PATCHOULI_GUIDE_BOOK)) {
            return false;
        }
        CompoundTag tag = itemStack.getTag();
        String bookId = tag.getString(TAG_PATCHOULI_BOOK);
        return (bookId != null && bookId.equals(GUIDE_BOOK_ID));
    }
}

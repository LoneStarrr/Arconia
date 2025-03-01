package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PatchouliHelper {
    public static final String GUIDE_BOOK_ID = ResourceLocation.fromNamespaceAndPath(Arconia.MOD_ID, "guide_book").toString(); //Also hardcoded in advancement icon
    public static final ResourceLocation PATCHOULI_GUIDE_BOOK = ResourceLocation.fromNamespaceAndPath("patchouli", "guide_book");
    public static final String TAG_PATCHOULI_BOOK = "patchouli:book";

    public static ItemStack createGuideBook() {
        Item bookItem = BuiltInRegistries.ITEM.get(PATCHOULI_GUIDE_BOOK);
        ItemStack bookStack = new ItemStack(bookItem);
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_PATCHOULI_BOOK, GUIDE_BOOK_ID);
        // TODO finish/rewrite this if this somehow magically works
        // TODO at least check for class cast exceptions!
        DataComponentType<?> dct = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("patchouli", "book"));
        if (dct != null) {
            DataComponentType<ResourceLocation> dctRloc = (DataComponentType<ResourceLocation>)dct;
            bookStack.set(dctRloc, new ResourceLocation(Arconia.MOD_ID, "guide_book"));
        }
        return bookStack;
    }

    public static DataComponentType<ResourceLocation> patchouliGuideBookComponent() {
        DataComponentType<?> dct = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("patchouli", "book"));
        return (DataComponentType<ResourceLocation>) dct;
    }

    public static boolean isGuideBook(ItemStack itemStack) {
        ResourceLocation itemResLoc = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (!itemResLoc.equals(PATCHOULI_GUIDE_BOOK)) {
            return false;
        }
        DataComponentType dct = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("patchouli", "book"));
        Object ob = itemStack.get(dct);
        if (ob != null) {
            ResourceLocation rloc = (ResourceLocation) ob;
            return rloc.equals(ResourceLocation.fromNamespaceAndPath(Arconia.MOD_ID, "guide_book"));
        }
        return false;
    }
}

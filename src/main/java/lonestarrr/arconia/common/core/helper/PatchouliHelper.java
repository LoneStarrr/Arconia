package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class PatchouliHelper {
    public static final String GUIDE_BOOK_ID = Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "guide_book").toString(); //Also hardcoded in advancement icon
    public static final Identifier PATCHOULI_GUIDE_BOOK = Identifier.fromNamespaceAndPath("patchouli", "guide_book");
    public static final String TAG_PATCHOULI_BOOK = "patchouli:book";

    public static ItemStackTemplate createGuideBookTemplate() {
        Item bookItem = BuiltInRegistries.ITEM.getValue(PATCHOULI_GUIDE_BOOK);
        if (bookItem == null || bookItem == net.minecraft.world.item.Items.AIR) return null;
        // TODO finish/rewrite this if this somehow magically works
        // TODO at least check for class cast exceptions!
        DataComponentType<?> dct = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.fromNamespaceAndPath("patchouli", "book"));
        DataComponentPatch patch = DataComponentPatch.EMPTY;
        if (dct != null) {
            DataComponentType<Identifier> dctRloc = (DataComponentType<Identifier>)dct;
            patch = DataComponentPatch.builder().set(dctRloc, ResourceLocationHelper.prefix("guide_book")).build();
        }
        return new ItemStackTemplate(bookItem, 1, patch);
    }

    public static ItemStack createGuideBook() {
        Item bookItem = BuiltInRegistries.ITEM.getValue(PATCHOULI_GUIDE_BOOK);
        if (bookItem == null) return ItemStack.EMPTY;
        ItemStack bookStack = new ItemStack(bookItem);
        // TODO finish/rewrite this if this somehow magically works
        // TODO at least check for class cast exceptions!
        DataComponentType<?> dct = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.fromNamespaceAndPath("patchouli", "book"));
        if (dct != null) {
            DataComponentType<Identifier> dctRloc = (DataComponentType<Identifier>)dct;
            bookStack.set(dctRloc, ResourceLocationHelper.prefix("guide_book"));
        }
        return bookStack;
    }

    public static DataComponentType<Identifier> patchouliGuideBookComponent() {
        DataComponentType<?> dct = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.fromNamespaceAndPath("patchouli", "book"));
        return (DataComponentType<Identifier>) dct;
    }

    public static boolean isGuideBook(ItemStack itemStack) {
        Identifier itemResLoc = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (!itemResLoc.equals(PATCHOULI_GUIDE_BOOK)) {
            return false;
        }
        DataComponentType dct = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.fromNamespaceAndPath("patchouli", "book"));
        Object ob = itemStack.get(dct);
        if (ob != null) {
            Identifier rloc = (Identifier) ob;
            return rloc.equals(Identifier.fromNamespaceAndPath(Arconia.MOD_ID, "guide_book"));
        }
        return false;
    }
}

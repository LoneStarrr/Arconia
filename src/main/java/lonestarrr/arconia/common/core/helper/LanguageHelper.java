package lonestarrr.arconia.common.core.helper;

import lonestarrr.arconia.common.Arconia;

/**
 * Helper functions for language / translation
 */
public class LanguageHelper {
    /**
     * @param itemKey Unique item key
     * @return Prefix to use in language file for this item's messages
     */
    public static String item(String itemKey) {
        return Arconia.MOD_ID + ".item." + itemKey;
    }

    /**
     * @param blockKey Unique block key
     * @return Prefix to use in language file for this block's messages
     */
    public static String block(String blockKey) {
        return Arconia.MOD_ID + ".block." + blockKey;
    }
}

package lonestarrr.arconia.common.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.util.INBTSerializable;
import lonestarrr.arconia.common.Arconia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * All the colors of the rainbow - used by various blocks and items
 */
public enum RainbowColor {
    RED(1, "red", "red", 0xff << 16),
    ORANGE(2, "orange", "orange", 0xff << 16 | 0xa5 << 8),
    YELLOW(3, "yellow", "yellow", 0xff << 16 | 0xff << 8),
    GREEN(4, "green", "green", 0xff << 8),
    BLUE(5, "blue", "blue", 0xff),
    INDIGO(6, "indigo", "indigo", 0x4b << 16 | 0x82),
    VIOLET(7, "violet", "violet", 0x77 << 16 | 0xff);

    private int tier;
    private int colorValue;
    private String tierName;
    private String unlocalizedName;

    private RainbowColor(int tier, String name, String unlocalizedName, int colorValue) {
        this.tier = tier;
        this.tierName = name;
        this.unlocalizedName = unlocalizedName;
        this.colorValue = colorValue;

    }

    @Nonnull
    public String getTierName() {
        return this.tierName;
    }

    /**
     *
     * @return Progression tier this color is associated (1..7)
     */
    public int getTier() {
        return tier;
    }

    /**
     *
     * @return The next tier, or null if this is the maximum tier
     */
    @Nullable
    public RainbowColor getNextTier() {
        if (tier == RainbowColor.VIOLET.tier) {
            return null;
        }
        return RainbowColor.values()[this.ordinal() + 1];
    }

    public String getUnlocalizedName() {
        return this.unlocalizedName;
    }
}

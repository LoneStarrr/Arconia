package lonestarrr.arconia.common.core;

import net.minecraft.util.FastColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * All the colors of the Minecraft rainbow roughly matching dye colors - used by various blocks and items
 */
public enum RainbowColor implements StringRepresentable {
    RED(1, "red", "Red", FastColor.ARGB32.color(0xFF, 0, 0)),
    ORANGE(2, "orange", "Orange", FastColor.ARGB32.color(0xFF, 0x7F, 0)),
    YELLOW(3, "yellow", "Yellow", FastColor.ARGB32.color(0xFF, 0xFF, 0)),
    GREEN(4, "green", "Green", FastColor.ARGB32.color(0, 0xFF, 0)),
    LIGHT_BLUE(5, "light_blue", "Light Blue", FastColor.ARGB32.color(0, 0xFF, 0xFF)),
    BLUE(6, "blue", "Blue", FastColor.ARGB32.color(0, 0, 0xFF)),
    PURPLE(7, "purple", "Purple", FastColor.ARGB32.color(0x80, 0, 0x80));

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

    public static RainbowColor byTier(int tier) {
        // TODO use a codec for this probably, and do this smarter?
        for (RainbowColor color: RainbowColor.values()) {
            if (color.getTier() == tier) {
                return color;
            }
        }
        return null;
    }

    @Nonnull
    public String getTierName() {
        return this.tierName;
    }

    @Nonnull
    /**
     * @return Color RGB value associated with the tier
     */
    public int getColorValue() {
        return this.colorValue;
    }

    /**
     *
     * @return Progression tier this color is associated (1..)
     */
    public int getTier() {
        return tier;
    }

    public static Stream<RainbowColor> stream() {
        return Stream.of(RainbowColor.values());
    }
    /**
     *
     * @return The next tier, or null if this is the maximum tier
     */
    @Nullable
    public RainbowColor getNextTier() {
        if (tier == RainbowColor.PURPLE.tier) {
            return null;
        }
        return RainbowColor.values()[this.ordinal() + 1];
    }

    public RainbowColor getPreviousTier() {
        if (tier == RainbowColor.RED.tier) {
            return null;
        }
        return RainbowColor.values()[this.ordinal() - 1];
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.tierName;
    }

    public String toString() {
        return this.tierName;
    }

    /**
     * @return Unlocalized name for default language data generation
     */
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public static Item woolByTier(RainbowColor tier) {
        Item wool;

        switch (tier) {
            case RED:
                wool = Items.RED_WOOL;
                break;
            case ORANGE:
                wool = Items.ORANGE_WOOL;
                break;
            case YELLOW:
                wool = Items.YELLOW_WOOL;
                break;
            case GREEN:
                wool = Items.GREEN_WOOL;
                break;
            case LIGHT_BLUE:
                wool = Items.LIGHT_BLUE_WOOL;
                break;
            case BLUE:
                wool = Items.BLUE_WOOL;
                break;
            case PURPLE:
                wool = Items.PURPLE_WOOL;
                break;
            default:
                wool = Items.RED_WOOL;
        }
        return wool;
    }

    public static Item dyeByTier(RainbowColor tier) {
        Item dye;

        switch (tier) {
            case RED:
                dye = Items.RED_DYE;
                break;
            case ORANGE:
                dye = Items.ORANGE_DYE;
                break;
            case YELLOW:
                dye = Items.YELLOW_DYE;
                break;
            case GREEN:
                dye = Items.GREEN_DYE;
                break;
            case LIGHT_BLUE:
                dye = Items.LIGHT_BLUE_DYE;
                break;
            case BLUE:
                dye = Items.BLUE_DYE;
                break;
            case PURPLE:
                dye = Items.PURPLE_DYE;
                break;
            default:
                dye = Items.RED_DYE;
        }
        return dye;
    }
}

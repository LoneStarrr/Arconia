package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.crafting.Ingredient;

public class ProgressOrangeSapling extends EntryProvider {

  public ProgressOrangeSapling(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    this.page(
            "page1",
            () ->
                    BookTextPageModel.create()
                            .withTitle(this.context().pageTitle())
                            .withText(this.context().pageText()));
    this.pageTitle("Orange sapling");
    this.pageText(
        """
        I will use my new sickle to break the leaves of red arconium trees to get a small chance to have them
        drop orange saplings. This, in turn, will give me orange branches, that can be imbued using the ritual to
        get more advanced items than I could get with the red branches. Like before, the first thing I should focus
        on is getting orange arconium essence first.
        """);
    this.page(
                    "page2",
                    () ->
                            BookSpotlightPageModel.create()
                                    .withItem(Ingredient.of(ModBlocks.getArconiumTreeSapling(RainbowColor.ORANGE))))
            .withTitle(this.context.pageTitle());
    this.pageTitle("Orange sapling");
  }

  @Override
  protected String entryName() {
    return "Orange sapling";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModBlocks.getArconiumTreeSapling(RainbowColor.ORANGE));
  }

  @Override
  protected String entryId() {
    return "progress_orange_sapling";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition = this.condition().advancement(ResourceLocationHelper.prefix("red_arconium_sickle"));
    return entry.withCondition(advancementCondition);
  }

}

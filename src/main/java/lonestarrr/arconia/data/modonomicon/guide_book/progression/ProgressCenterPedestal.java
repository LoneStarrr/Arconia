package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;

public class ProgressCenterPedestal extends EntryProvider {

  public ProgressCenterPedestal(CategoryProviderBase parent) {
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
    this.pageTitle("Center Pedestal");
    this.pageText(
        """
            To complete the ritual setup, I also need a pedestal made from stone. Once crafted, I need to place it down
            and surround it with up to eight wood pedestals nearby. A ritual requires items to be placed on the wood
            pedestals, and is then started by using the clover staff on the stone pedestal. The ritual will
            consume the placed down items, and magically transform them into a new item.
            """);
    this.page(
        "page2",
        () ->
            BookCraftingRecipePageModel.create()
                .withRecipeId1(ResourceLocationHelper.prefix("center_pedestal"))
                .withTitle1("Center Pedestal"));
    this.page("page3", () -> BookTextPageModel.create().withText(this.context().pageText()));
    this.pageText(
        """
            The generated item will appear on the center pedestal once the ritual is done.
            There are many recipes. They can be found typically by looking them up from the center pedestal, or
            from items used in the recipes, e.g. the colored tree branches.
            """);
  }

  @Override
  protected String entryName() {
    return "Center Pedestal";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModBlocks.centerPedestal.asItem());
  }

  @Override
  protected String entryId() {
    return "progress_center_pedestal";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition =
        this.condition().advancement(ResourceLocationHelper.prefix("pedestal"));
    return entry.withCondition(advancementCondition);
  }
}

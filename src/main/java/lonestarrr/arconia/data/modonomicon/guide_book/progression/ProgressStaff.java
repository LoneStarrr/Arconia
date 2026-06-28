package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.util.Util;

public class ProgressStaff extends EntryProvider {

  public ProgressStaff(CategoryProviderBase parent) {
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
    this.pageTitle("Clover Staff");
    this.pageText(
        """
        The clover staff will prove very useful for our plans. For once, using it to break clovers found in the world
        increases the odds of getting four-leaf clovers. That should save me some time exploring!
        Enchanting it with fortune will increase the odds even more, so I should make sure to do this first change I
        get.
        """);
    this.page(
        "page2",
        () ->
            BookCraftingRecipePageModel.create()
                .withRecipeId1(ResourceLocationHelper.prefix("clover_staff")));
  }

  @Override
  protected String entryName() {
    return "Clover Staff";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.cloverStaff);
  }

  @Override
  protected String entryId() {
    return "progress_staff";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition =
        this.condition().advancement(ResourceLocationHelper.prefix("four_leaf_clover"));
    this.lang()
        .add(
            Util.makeDescriptionId("advancement", advancementCondition.getAdvancementId())
                + ".title",
            "Find a four-leaf clover");

    return entry.withCondition(advancementCondition);
  }
}

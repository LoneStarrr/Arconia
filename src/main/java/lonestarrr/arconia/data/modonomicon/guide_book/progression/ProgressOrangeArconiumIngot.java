package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ModItems;

public class ProgressOrangeArconiumIngot extends EntryProvider {

  public ProgressOrangeArconiumIngot(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    this.page("page1", () -> BookTextPageModel.create().withText(this.context().pageText()))
        .withTitle(this.context.pageTitle());
    this.pageTitle("Orange arconium ingot");
    this.pageText(
        """
        Like before, orange ingots are crafted from orange arconium essence. I'll have to imbue an orange branch
        to extract orange essence from the pot. For the extraction to work, the pot will now also require an orange
        tree planted in one of the 8 positions surrounding it. The colored grass around the tree needs to match the
        color of the tree as well. If planted successfully, the pot will show orange rainbow particles.
        """);
    this.page(
        "page2",
        () ->
            BookImagePageModel.create()
                .withImages(
                    ResourceLocationHelper.prefix(
                        "textures/gui/guide_book/potofgold_trees_orange.png")));
  }

  @Override
  protected String entryName() {
    return "Orange arconium ingot";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.getArconiumIngot(RainbowColor.ORANGE));
  }

  @Override
  protected String entryId() {
    return "progress_orange_arconium_ingot";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition =
        this.condition().advancement(ResourceLocationHelper.prefix("orange_arconium_tree_sapling"));
    return entry.withCondition(advancementCondition);
  }
}

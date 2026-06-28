package lonestarrr.arconia.data.modonomicon.guide_book.diary;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class DiaryChapter3 extends EntryProvider {

  public DiaryChapter3(CategoryProviderBase parent) {
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
    this.pageTitle("Unfamiliar Noises");
    this.pageText(
        """
                Dear diary. Last night I was disturbed in my sleep by some unfamiliar noises.
                I swear it sounded like coins being tossed into piles.
                But coins have no business of being in an otherwise deserted forest, so this must be my imagination
                running wild. It has been a few days since I last had a proper dinner.
                """);
  }

  @Override
  protected String entryName() {
    return "Unfamiliar Noises";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.BOOK);
  }

  @Override
  protected String entryId() {
    return "diary_chapter3";
  }
}

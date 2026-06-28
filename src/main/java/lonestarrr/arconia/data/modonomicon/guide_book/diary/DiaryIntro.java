package lonestarrr.arconia.data.modonomicon.guide_book.diary;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class DiaryIntro extends EntryProvider {

  public DiaryIntro(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    this.page(
        "lore",
        () ->
            BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
    this.pageTitle("Diary");
    this.pageText(
        """
                        Background and lore for curious minds. Impatient travellers eager to get their hands dirty
                        may skip this section.
                        """);
  }

  @Override
  protected String entryName() {
    return "Diary";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.CATEGORY_START;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.WRITTEN_BOOK);
  }

  @Override
  protected String entryId() {
    return "diary";
  }
}

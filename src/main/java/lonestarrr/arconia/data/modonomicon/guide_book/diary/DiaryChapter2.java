package lonestarrr.arconia.data.modonomicon.guide_book.diary;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class DiaryChapter2 extends EntryProvider {

  public DiaryChapter2(CategoryProviderBase parent) {
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
    this.pageTitle("In a forest");
    this.pageText(
        """
        Dear diary. I have ventured into a rather dense forest. I am pretty sure that on the other side is the
        civilization I seek. I'm going to set up camp high up in the trees to be safe from these pesky zombies
        that always seem to find me.
        """);
    this.page(
        "page2",
        () ->
            BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
    this.pageText(
        """
            For as dense as this forest is, it seems to be strangely devoid of your typical nasty creatures that lurk in
            the shadows. I can only hope that this is a positive sign.
            """);
    this.pageTitle("");
  }

  @Override
  protected String entryName() {
    return "In a forest";
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
    return "diary_chapter2";
  }
}

/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package lonestarrr.arconia.data.modonomicon;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconLanguageProvider;
import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.data.modonomicon.guide_book.MainCategory;

public class GuideBook extends SingleBookSubProvider {

  public static final String ID = "guide_book";

  public GuideBook(String modid, ModonomiconLanguageProvider lang) {
    super(ID, modid, lang);
  }

  @Override
  protected BookModel additionalSetup(BookModel book) {
    // arconia:guide_book is a dedicated item (see ModItems), per modonomicon's custom-book-item
    // docs: disable the auto-generated book item and point the book at our own item instead.
    return book.withGenerateBookItem(false)
        .withCustomBookItem(this.modLoc(ItemNames.GUIDE_BOOK))
        .withTheme(
            theme ->
                theme.withLayout(
                    layout ->
                        layout
                            .withBookTextOffsetX(5)
                            .withBookTextOffsetY(0)
                            .withBookTextOffsetWidth(-5)))
        .withAllowOpenBooksWithInvalidLinks(true);
  }

  @Override
  protected void registerDefaultMacros() {
    // currently no macros
  }

  @Override
  protected void generateCategories() {
    this.add(new MainCategory(this).generate());
  }

  @Override
  protected String bookName() {
    return "Arconia Notebook";
  }

  @Override
  protected String bookTooltip() {
    return "A traveller's notes on all things Arconia";
  }
}

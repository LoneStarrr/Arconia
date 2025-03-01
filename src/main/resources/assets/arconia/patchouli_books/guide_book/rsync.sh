#!/bin/sh
# Quick & dirty helper script to copy files to dev instance so book can be updated in-game without restarting
rsync -va --delete . ../../../../../../../build/resources/main/assets/arconia/patchouli_books/guide_book

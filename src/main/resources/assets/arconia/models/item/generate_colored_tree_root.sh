#!/bin/sh

# Lazy man's data generators.
# Generates modelstates and block/item models for all tree root blocks.

echo "Console output can be appended/amended in lang/en_us.json" >&2

for clr in red orange yellow green blue indigo violet
do
    # Item models - the texture is colored using IItemColor
    cat << EOF > ${clr}_colored_tree_root.json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "arconia:item/colored_tree_root"
  }
}
EOF

    # Add to lang:
cat << EOF
    "item.arconia.${clr}_colored_tree_root": "${clr^} Colored Tree Root",
EOF
done


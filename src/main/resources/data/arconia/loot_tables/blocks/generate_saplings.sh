#!/usr/bin/env bash

# Lazy man's data generators - bite me

for clr in red orange yellow green blue light_blue purple magenta pink
do
    cat << EOF > ${clr}_arconium_tree_sapling.json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "arconia:${clr}_arconium_tree_sapling"
        }
      ]
    }
  ]
}
EOF
done

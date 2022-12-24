#!/usr/bin/env bash

# Lazy man's data generators - bite me

for clr in red orange yellow green blue light_blue purple
do
    cat << EOF > ${clr}_gold_arconium_block.json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "arconia:${clr}_gold_arconium_block",
          "functions": [
            {
              "function": "minecraft:copy_nbt",
              "source": "block_entity",
              "ops": [
                {
                  "source": "coins",
                  "target": "BlockEntityTag.coins",
                  "op": "replace"
                },
                {
                  "source": "infinite",
                  "target": "BlockEntityTag.infinite",
                  "op": "replace"
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
EOF
done

#!/usr/bin/env bash

# Lazy man's data generators - bite me

for clr in red orange yellow green light_blue blue purple
do
    cat << EOF > "${clr}_rainbow_grass_block.json"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "bonus_rolls": 0.0,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "enchantments": [
                      {
                        "enchantment": "minecraft:silk_touch",
                        "levels": {
                          "min": 1
                        }
                      }
                    ]
                  }
                }
              ],
              "name": "arconia:${clr}_rainbow_grass_block"
            },
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:survives_explosion"
                }
              ],
              "name": "minecraft:dirt"
            }
          ]
        }
      ]
    }
  ]
}
EOF
done

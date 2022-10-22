#!/usr/bin/env bash

# Lazy man's data generators - bite me

for clrcombo in red:orange orange:yellow yellow:green green:blue blue:light_blue light_blue:purple purple:magenta magenta:pink pink:pink
do
    clr=$(echo $clrcombo | cut -d ':' -f1)
    nextclr=$(echo $clrcombo | cut -d ':' -f2)
    cat << EOF > ${clr}_arconium_tree_leaves.json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "name": "arconia:${clr}_arconium_tree_leaves",
              "conditions": [
                {
                  "condition": "minecraft:alternative",
                  "terms": [
                    {
                      "condition": "minecraft:match_tool",
                      "predicate": {
                        "items": [ "minecraft:shears" ]
                      }
                    },
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
                  ]
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "arconia:${nextclr}_arconium_tree_sapling",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "items": [ "arconia:${clr}_arconium_sickle" ]
                  }
                },
                {
                  "condition": "minecraft:table_bonus",
                  "enchantment": "minecraft:fortune",
                  "chances": [
                    0.05,
                    0.0625,
                    0.083333336,
                    0.1
                  ]
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "arconia:${clr}_arconium_tree_sapling",
              "conditions": [
                {
                  "condition": "minecraft:survives_explosion"
                },
                {
                  "condition": "minecraft:table_bonus",
                  "enchantment": "minecraft:fortune",
                  "chances": [
                    0.05,
                    0.0625,
                    0.083333336,
                    0.1
                  ]
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "arconia:${clr}_colored_tree_root",
              "conditions": [
                {
                  "condition": "minecraft:survives_explosion"
                },
                {
                  "condition": "minecraft:table_bonus",
                  "enchantment": "minecraft:fortune",
                  "chances": [
                    0.05,
                    0.0625,
                    0.083333336,
                    0.1
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:stick",
          "conditions": [
            {
              "condition": "minecraft:table_bonus",
              "enchantment": "minecraft:fortune",
              "chances": [
                0.02,
                0.022222223,
                0.025,
                0.033333335,
                0.1
              ]
            }
          ],
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1.0,
                "max": 2.0,
                "type": "minecraft:uniform"
              }
            },
            {
              "function": "minecraft:explosion_decay"
            }
          ]
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:inverted",
          "term": {
            "condition": "minecraft:alternative",
            "terms": [
              {
                "condition": "minecraft:match_tool",
                "predicate": {
                  "items": [ "minecraft:shears" ]
                }
              },
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
            ]
          }
        }
      ]
    }
  ]
}
EOF
done

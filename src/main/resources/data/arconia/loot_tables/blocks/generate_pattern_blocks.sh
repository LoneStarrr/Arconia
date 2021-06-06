#!/usr/bin/env bash

for tier_type in t1:red t1:white t1:stem t1:sky
do
    tier=$(echo $tier_type | cut -d ':' -f1)
    ptype=$(echo $tier_type | cut -d ':' -f2)

    # Incomplete blocks can be harvested
    cat << EOF > pattern_${tier}_${ptype}_incomplete.json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "arconia:pattern_${tier}_${ptype}_incomplete"
        }
      ]
    }
  ]
}
EOF
done

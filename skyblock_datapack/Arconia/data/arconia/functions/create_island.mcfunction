fill -7 61 -7 7 64 7 minecraft:dirt
setblock 6 64 6 minecraft:water
setblock 6 64 5 minecraft:water
setblock 6 64 4 minecraft:air
setblock 6 63 4 minecraft:air
setblock 6 64 2 minecraft:lava
setblock 6 65 7 minecraft:barrel[facing=down]{Items: [{Slot: 0b, id: "arconia:red_arconium_tree_sapling", Count: 4b}, {Slot: 1b, id: "minecraft:bone_meal", Count: 64b}, {Slot: 2b, id: "minecraft:bone_meal", Count: 64b}, {Slot: 3b, id: "arconia:magic_in_a_bottle", Count: 1b, tag: {}}, {Slot: 4b, id: "patchouli:guide_book", Count: 1b, tag: {"patchouli:book": "arconia:guide_book"}}]}
tp @a 0 65 0
say I can't wait to start!
# Signal that the setup phase is complete
scoreboard players set isFirst setup_complete 1
gamemode survival @a

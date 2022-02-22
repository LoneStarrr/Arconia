# It appears there is no guarantee that things are executed in a sane order. E.g. commands before this line could be executed while the world is still generating, overwriting my commands. Thus, we abuse the scoreboard to force delays by executing code only once a player's "score" has been updated, thus is actively in the game.

# This adds 1 score every tick as soon as the player is properly logged in. Until the execute below registers and 
# prevents this function from being called. This seems to be the only way to reliably initialize the world/player.
scoreboard players add @a skyblock_install 1
execute as @a[scores={skyblock_install=1..}] run tp @a 0 128 0
execute as @a[scores={skyblock_install=1..}] run function arconia:create_island

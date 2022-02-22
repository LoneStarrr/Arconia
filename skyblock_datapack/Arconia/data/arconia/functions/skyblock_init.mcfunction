# Abuse the score board to run this logic only once, rather than every game load
scoreboard objectives add testFirst dummy
scoreboard objectives add setup_complete dummy
execute unless score isFirst setup_complete matches 1 run scoreboard players set isFirst setup_complete 0
execute unless score isFirst testFirst matches 1 run scoreboard players set isFirst testFirst 0
execute if score isFirst testFirst matches 0 run function arconia:bootstrap
scoreboard players set isFirst testFirst 1
# Run this until enough ticks have passed to pass initialization
execute if score isFirst setup_complete matches 0 run function arconia:welcome_player

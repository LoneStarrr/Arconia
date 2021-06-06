package lonestarrr.arconia.common.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import lonestarrr.arconia.common.Arconia;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Experimental - generate a tree using L-system fractals with custom rules
 *
 * Example run with 2 rules and 3 iterations: /fractal-tree "0:-1>W0<>N0<>E0<>S0<0+,1:11" 3
 *
 * http://algorithmicbotany.org/papers/abop/abop.pdf - page 27 and onwards are interesting, describing how to
 * introduce randomness
 */
public class FractalTreeCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("fractal-tree").then(
                    Commands.argument("rules", StringArgumentType.string()).then(
                        Commands.argument("iterations", IntegerArgumentType.integer(1, 8))
                        .executes(ctx -> { return fractalTree(ctx, StringArgumentType.getString(ctx, "rules"),
                                IntegerArgumentType.getInteger(ctx, "iterations")); })
                    )
                )
        );
    }

    private static int fractalTree(CommandContext<CommandSource> ctx, String rulesSerialized, int iterations) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().asPlayer();
        World world = ctx.getSource().getWorld();
        BlockPos pos = player.getPosition().up(2);
        Map<Character, String> rules = parseRules(rulesSerialized);
        String ltree = generateLTreeString(rules, iterations);
        try {
            renderLTree(world, pos, ltree, player, -1);
        } catch (Exception e) {
                Arconia.logger.warn("Error rendering tree");
        }
        //ctx.getSource().asPlayer().sendMessage(new StringTextComponent("Rules: " + rules));
        return Command.SINGLE_SUCCESS;
    }

    private static Map<Character, String> parseRules(String serialized) throws CommandSyntaxException {
        Map<Character, String> result = new HashMap<>();

        if (serialized.equals("default")) {
            result.put('0', "1[0]0");
            result.put('1', "11");
            return result;
        }

        // syntax: char:string,char2:string
        for (String singleRule: serialized.split(",")) {
            String[] ruleParts = singleRule.split(":");
            if (ruleParts.length != 2) {
                throw new SimpleCommandExceptionType(new StringTextComponent("Rule colon parse error")).create();
            }
            String ruleChar = ruleParts[0];
            String ruleSubst = ruleParts[1];
            if (ruleChar.length() != 1 || ruleSubst.length() < 1) {
                throw new SimpleCommandExceptionType(new StringTextComponent("Invalid rule lengths")).create();
            }
            result.put(ruleChar.charAt(0), ruleSubst);
        }
        return result;
    }

    private static void renderLTree(World worldIn, BlockPos posIn, String ltree, PlayerEntity player, int stopAt) {
        int count = 0;
        float numLogsToDraw = 3;
        DrawState s = new DrawState(posIn);

        Stack<DrawState> posStack = new Stack<>();
        Stack<Float> logSizeStack = new Stack<>();
        for (Character c: ltree.toCharArray()) {
            switch (c) {
                case '1': //place log
                    for (int i = 0; i < (int)numLogsToDraw; i++) {
                        s.pos = advancePos3D(s);
                        worldIn.setBlockState(s.pos, Blocks.OAK_LOG.getDefaultState());
                    }
                    break;
                case '0': //place leaf
                    s.pos = advancePos3D(s);
                    worldIn.setBlockState(s.pos, Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.DISTANCE, 1));
                    break;
                case 'W': //rotate "west"
                    s.rotationX = (s.rotationX + 315)%360;
                    break;
                case 'E': //rotate "east"
                    s.rotationX = (s.rotationX + 45)%360;
                    break;
                case 'N': //rotate "north"
                    s.rotationZ = (s.rotationZ + 315)%360;
                    break;
                case 'S': //rotate "south"
                    s.rotationZ = (s.rotationZ + 45)%360;
                    break;
                case '<': // pop stack
                    s = posStack.pop();
                    break;
                case '>': // push stack
                    posStack.push(new DrawState(s));
                    break;
                case '-': // shrink logs drawn
                    logSizeStack.push(numLogsToDraw);
                    numLogsToDraw = Math.max(1f, numLogsToDraw - 0.5f);
                    break;
                case '+': // grow logs drawn
                    try {
                        numLogsToDraw = logSizeStack.pop();
                    } catch (EmptyStackException e) {
                        Arconia.logger.error("Empty logsize stack while drawing ltree: " + ltree.substring(0,
                                count + 1) + " of " + ltree);
                    }
                    break;
            }
            count = count + 1;
            if (stopAt >= 0 && count >= stopAt) {
                String treeRendered = ltree.substring(0, count);
                player.sendMessage(new StringTextComponent("Position = " + s.pos + ", rotation = " + s.rotationX + "," + s.rotationZ + ", " +
                        " String drawn = " + treeRendered), Util.DUMMY_UUID);
                break;
            }
        }
    }

    private static BlockPos advancePos(BlockPos pos, Integer rotation) {
        BlockPos newPos;

        switch(rotation) {
            case 0: newPos = pos.add(0, 1, 0); break;
            case 315: newPos = pos.add(-1, 1, 0); break;
            case 270: newPos = pos.add(-1, 0, 0); break;
            case 225: newPos = pos.add(-1, -1, 0); break;
            case 180: newPos = pos.add(0, -1, 0); break;
            case 135: newPos = pos.add(1, -1, 0); break;
            case 90: newPos = pos.add(1, 0, 0); break;
            case 45: newPos = pos.add(1, 1, 0); break;
            default:
                newPos = pos;
        }
        return newPos;
    }

    private static BlockPos advancePos3D(DrawState s) {
        Vector3d vX, vZ;

        switch(s.rotationX) {
            case 0: vX = new Vector3d(0, 1, 0); break;
            case 315: vX = new Vector3d(0, 1, -1); break;
            case 270: vX = new Vector3d(0, 0, -1); break;
            case 225: vX = new Vector3d(0, -1, -1); break;
            case 180: vX = new Vector3d(0, -1, 0); break;
            case 135: vX = new Vector3d(0, -1, 1); break;
            case 90: vX = new Vector3d(0, 0, 1); break;
            case 45: vX = new Vector3d(0, 1, 1); break;
            default:
                vX = new Vector3d(0, 0, 0);
        }

        switch(s.rotationZ) {
            case 0: vZ = new Vector3d(0, 1, 0); break;
            case 315: vZ = new Vector3d(-1, 1, 0); break;
            case 270: vZ = new Vector3d(-1, 0, 0); break;
            case 225: vZ = new Vector3d(-1, -1, 0); break;
            case 180: vZ = new Vector3d(0, -1, 0); break;
            case 135: vZ = new Vector3d(1, -1, 0); break;
            case 90: vZ = new Vector3d(1, 0, 0); break;
            case 45: vZ = new Vector3d(1, 1, 0); break;
            default:
                vZ = new Vector3d(0, 0, 0);
        }

        double yCombined = vX.y + vZ.y;
        yCombined = (yCombined > 1 ? 1 : yCombined < -1? -1 : yCombined);
        return s.pos.add(vX.x + vZ.x, yCombined, vX.z + vZ.z);
    }

    private static String generateLTreeString(Map<Character, String> rules, int recursions) {
        // https://en.wikipedia.org/wiki/L-system#Example_2:_Fractal_(binary)_tree
        final String axiom = "0";

        String shape = axiom;
        for (int it = 0; it < recursions; it++) {
            StringBuilder output = new StringBuilder();
            for (Character c: shape.toCharArray()) {
                if (rules.containsKey(c)) {
                    output.append(rules.get(c));
                } else {
                    output.append(c);
                }
            }
            shape = output.toString();
        }

        return shape;
    }

}

class DrawState {
    public int rotationX;
    public int rotationZ;
    public BlockPos pos;

    public DrawState(BlockPos pos) {
        rotationX = 0;
        rotationZ = 0;
        this.pos = pos;
    }

    public DrawState(DrawState other) {
        this.rotationX = other.rotationX;
        this.rotationZ = other.rotationZ;
        this.pos = other.pos;
    }
}

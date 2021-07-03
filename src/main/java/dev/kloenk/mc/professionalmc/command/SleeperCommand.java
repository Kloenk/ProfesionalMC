package dev.kloenk.mc.professionalmc.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.kloenk.mc.professionalmc.profession.MerchantProfession;
import dev.kloenk.mc.professionalmc.profession.SleeperProfession;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SleeperCommand {
    private static final Text SUCCESS_MSG = new LiteralText("Success!").formatted(Formatting.GREEN);
    private static final Text PROFESSION_NOT_SET = new LiteralText("This taterzen lacks " + MerchantProfession.PROFESSION_ID + " profession!");
    private static final Text NO_NPC_SELECTED = new LiteralText("You have to select Taterzen first");
    private static final Text INVALID_NUMBER = new LiteralText("Invalid number");
    private static final Text NOT_A_BED = new LiteralText("Position does not contain a bed").formatted(Formatting.YELLOW);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("sleepnpc")
            .requires(src -> src.hasPermissionLevel(2))
            .then(literal("edit")
                    .then(literal("wakeuptime")
                            .then(
                                    argument("wakeup time", IntegerArgumentType.integer(0, 24000))
                                        .executes(SleeperCommand::editWakeUpTime)
                            )
                            .executes(SleeperCommand::getWakeupTime)
                    )
                    .then(literal("bedtime")
                            .then(
                                    argument("bed time", IntegerArgumentType.integer(0, 24000))
                                        .executes(SleeperCommand::editBedTime)
                            )
                            .executes(SleeperCommand::getBedTime)
                    )
                    .then(literal("bedpos")
                            .then(
                                    argument("position", BlockPosArgumentType.blockPos())
                                        .executes(SleeperCommand::setBedPos)
                            )
                            .executes(SleeperCommand::getBedPos)
                    )
                    .then(literal("bedrequired")
                            .then(
                                    argument("required", BoolArgumentType.bool())
                                        .executes(SleeperCommand::setRequireBed)
                            )
                            .executes(SleeperCommand::getRequireBed)
                    )
            )
        );
    }

    private static int editWakeUpTime(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            long wakeupTime = IntegerArgumentType.getInteger(ctx, "wakeup time");

            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                if (profession.setWakeUpTime(wakeupTime)) {
                    source.sendFeedback(SUCCESS_MSG, false);
                } else {
                    source.sendError(INVALID_NUMBER);
                }
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }

    private static int getWakeupTime(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                Text text = new LiteralText("Wakeup Time is set to: " + profession.getWakeUpTime() );
               source.sendFeedback(text, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }

    private static int editBedTime(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            long wakeupTime = IntegerArgumentType.getInteger(ctx, "bed time");

            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                if (profession.setBedTime(wakeupTime)) {
                    source.sendFeedback(SUCCESS_MSG, false);
                } else {
                    source.sendError(INVALID_NUMBER);
                }
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }

    private static int getBedTime(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                Text text = new LiteralText("Bed Time is set to: " + profession.getBedTime() );
                source.sendFeedback(text, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }

    private static int setBedPos(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
                BlockState block =  npc.world.getBlockState(pos);
                profession.setBedPosition(pos);
                if (!(block.getBlock() instanceof BedBlock)) {
                    source.sendFeedback(NOT_A_BED, false);
                }
                source.sendFeedback(SUCCESS_MSG, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }

    private static int getBedPos(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                BlockPos pos = profession.getBedPosition();
                Text text = new LiteralText("Bed is on position: " + pos.toString());
                source.sendFeedback(text, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }

    private static int setRequireBed(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                boolean require = BoolArgumentType.getBool(ctx, "required");
                profession.setRequireBed(require);
                source.sendFeedback(SUCCESS_MSG, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }

    private static int getRequireBed(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        if (npc != null) {
            SleeperProfession profession = (SleeperProfession) npc.getProfession(SleeperProfession.PROFESSION_ID);
            if (profession != null) {
                boolean require = profession.doesRequireBed();
                Text msg;
                if (require) {
                    msg = new LiteralText("NPC does require a bed to sleep");
                } else {
                    msg = new LiteralText("NPC does not require a bed to sleep").formatted(Formatting.ITALIC);
                }
                source.sendFeedback(msg, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }
}

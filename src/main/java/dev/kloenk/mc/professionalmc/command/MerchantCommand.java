package dev.kloenk.mc.professionalmc.command;

import dev.kloenk.mc.professionalmc.profession.MerchantProfession;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * The /merchant command
 * @see <a href="https://fabricmc.net/wiki/tutorial:commands">Command Tutorial</a>
 */
public class MerchantCommand {
    private static final Text HOORAY_MSG = new LiteralText("Success!").formatted(Formatting.GREEN);
    private static final Text PROFESSION_NOT_SET = new LiteralText("This taterzen lacks " + MerchantProfession.PROFESSION_ID + " profession!");
    private static final Text NO_NPC_SELECTED = new LiteralText("You have to select Taterzen first");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("merchant")
            .requires(src -> src.hasPermissionLevel(2))
            .then(literal("edit")
                .then(literal("punchMessage")
                    .then(argument("new ouch message", MessageArgumentType.message())
                        .executes(MerchantCommand::editPunchMessage)
                    )
                )
                .then(literal("paymentCount")
                    .then(argument("throw count", IntegerArgumentType.integer(0, 64))
                        .executes(MerchantCommand::editPaymentCount)
                    )
                )
            )
        );
    }

    private static int editPaymentCount(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC taterzen = ((ITaterzenEditor)source.getPlayer()).getNpc();
        if (taterzen != null) {
            int throwCount = IntegerArgumentType.getInteger(ctx, "throw count");

            // Get the profession instance
            MerchantProfession profession = (MerchantProfession) taterzen.getProfession(MerchantProfession.PROFESSION_ID);
            if(profession != null) {
                profession.setThrowCount(throwCount);
                source.sendFeedback(HOORAY_MSG, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            // Error for no selected Taterzen is already available
            source.sendError(NO_NPC_SELECTED);
            //source.sendError(TaterzensAPI.noSelectedTaterzenError());
        }
        return 0;
    }

    private static int editPunchMessage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        TaterzenNPC taterzen = ((ITaterzenEditor)source.getPlayer()).getNpc();
        if (taterzen != null) {
            String ouchMessage = MessageArgumentType.getMessage(ctx, "new ouch message").getString();

            // Get the profession instance
            MerchantProfession profession = (MerchantProfession) taterzen.getProfession(MerchantProfession.PROFESSION_ID);
            if(profession != null) {
                profession.setPunchMessage(ouchMessage);
                source.sendFeedback(HOORAY_MSG, false);
            } else {
                source.sendError(PROFESSION_NOT_SET);
            }
        } else {
            // Error for no selected Taterzen is already available
            source.sendError(NO_NPC_SELECTED);
        }
        return 0;
    }
}

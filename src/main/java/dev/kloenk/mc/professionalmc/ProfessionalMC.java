package dev.kloenk.mc.professionalmc;

import dev.kloenk.mc.professionalmc.command.MerchantCommand;
import dev.kloenk.mc.professionalmc.command.SleeperCommand;
import dev.kloenk.mc.professionalmc.profession.MerchantProfession;
import dev.kloenk.mc.professionalmc.profession.SleeperProfession;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.samo_lego.taterzens.api.TaterzensAPI;

public class ProfessionalMC implements ModInitializer {

    /**
     * Mod id, should be same as in fabric.mod.json
     */
    public static final String MOD_ID = "professionalmc";

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        System.out.println("Loading professions from : " + MOD_ID);

        // Registering our profession(s)
        // This will also make it automatically appear in command suggestions
        // for adding profession (`/npc edit professions add <PROFESSION_ID>`)
        TaterzensAPI.registerProfession(MerchantProfession.PROFESSION_ID, new MerchantProfession());
        TaterzensAPI.registerProfession(SleeperProfession.PROFESSION_ID, new SleeperProfession());

        // Registering command for editing merchant profession
        CommandRegistrationCallback.EVENT.register(MerchantCommand::register);
        CommandRegistrationCallback.EVENT.register(SleeperCommand::register);
    }
}

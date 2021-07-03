package dev.kloenk.mc.professionalmc.profession;

import dev.kloenk.mc.professionalmc.ProfessionalMC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.npc.NPCData.Movement;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.Optional;

public class SleeperProfession implements TaterzenProfession {
    public static final Identifier PROFESSION_ID = new Identifier(ProfessionalMC.MOD_ID, "sleeper");
    private static final Logger LOGGER = LogManager.getLogger("SleeperProfession");
    private TaterzenNPC npc;
    private BlockPos bedPosition;
    private Movement lastMovement;

    private boolean isSleeping;
    private boolean onWayToSleep;
    private long bedTime, wakeUpTime;

    public SleeperProfession() {
        isSleeping = false;
        onWayToSleep = false;
        bedTime = 12400;
        wakeUpTime = 1000;
    }

    @Override
    public SleeperProfession create(TaterzenNPC taterzenNPC) {
        LOGGER.info("creating sleeper");
        SleeperProfession profession = new SleeperProfession();

        profession.npc = taterzenNPC;
        // FIXME: none hardcoded and updating
        profession.lastMovement = Movement.PATH;
        profession.isSleeping = taterzenNPC.getFakePlayer().isSleeping();

        return profession;
    }

    /*@Override
    public ActionResult tick() {
        long time = npc.world.getTimeOfDay();

        LOGGER.info("time: " + time);
        return ActionResult.PASS;
    }*/

    @Override
    public ActionResult tickMovement() {
        long time = npc.world.getTimeOfDay();

        if (time >= bedTime && !isSleeping) {
            if (onWayToSleep && bedPosition != null) {
                boolean isInReach =  bedPosition.isWithinDistance(npc.getPos(), 2.0);
                if (isInReach) {
                    onWayToSleep = false;

                    LOGGER.info("sleeping: " + npc.getName().getString());
                    // TODO: check if there is a bed (maybe also parameter for requireBed)
                    npc.getFakePlayer().sleep(bedPosition);

                    isSleeping = true;
                    npc.removePathTarget(bedPosition);
                    npc.setMovement(Movement.NONE);
                }
            } else if (bedPosition != null) {
                LOGGER.info("going to sleep: " + npc.getName().getString());
                npc.addPathTarget(bedPosition);
                // FIXME: store movement
                npc.setMovement(Movement.FORCED_PATH);
                onWayToSleep = true;
            }
        } else if (time >= wakeUpTime && time < bedTime && isSleeping) {
            LOGGER.info("waking up: " + npc.getName().getString());
            npc.getFakePlayer().wakeUp();
            npc.setMovement(lastMovement);
            isSleeping = false;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onRemove() {
        if (npc.getFakePlayer().isSleeping()) {
            // do not occupy bed after removal
            npc.getFakePlayer().wakeUp();
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        this.wakeUpTime = tag.getLong("wakeupTime");
        this.bedTime = tag.getLong("bedTime");

        int x = tag.getInt("bedPos.x");
        int y = tag.getInt("bedPos.y");
        int z = tag.getInt("bedPos.z");
        this.bedPosition = new BlockPos(x, y, z);
        try {
            this.lastMovement = Movement.valueOf(tag.getString("lastMovement"));
        } catch (Exception e) {
            // FIXME: decide on a good default movement
            this.lastMovement = Movement.PATH;
        }
    }

    @Override
    public void saveNbt(NbtCompound tag) {
        tag.putLong("wakeupTime", this.wakeUpTime);
        tag.putLong("bedTime", this.bedTime);
        tag.putString("lastMovement", lastMovement.name());

        if (this.bedPosition != null) {
            tag.putInt("bedPos.x", this.bedPosition.getX());
            tag.putInt("bedPos.y", this.bedPosition.getY());
            tag.putInt("bedPos.z", this.bedPosition.getZ());
        }
    }

    public boolean setBedTime(long bedTime) {
        if (this.wakeUpTime >= bedTime) {
            return false;
        }
        this.bedTime = bedTime;
        return true;
    }

    public long getBedTime() {
        return bedTime;
    }

    public boolean setWakeUpTime(long wakeUpTime) {
        if (this.bedTime <= wakeUpTime) {
            return false;
        }
        this.wakeUpTime = wakeUpTime;
        return true;
    }

    public long getWakeUpTime() {
        return wakeUpTime;
    }

    public void setBedPosition(BlockPos bedPosition) {
        this.bedPosition = bedPosition;
    }

    public BlockPos getBedPosition() {
        return bedPosition;
    }
}

package dev.kloenk.mc.professionalmc.profession;

import dev.kloenk.mc.professionalmc.ProfessionalMC;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityPose;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.npc.NPCData.Movement;
import org.samo_lego.taterzens.npc.TaterzenNPC;



public class SleeperProfession implements TaterzenProfession {
    public static final Identifier PROFESSION_ID = new Identifier(ProfessionalMC.MOD_ID, "sleeper");
    private static final Logger LOGGER = LogManager.getLogger("SleeperProfession");
    private TaterzenNPC npc;
    private BlockPos bedPosition;
    private Movement lastMovement, settingMovement;

    private boolean isSleeping, onWayToSleep;
    private long bedTime, wakeUpTime;

    private boolean requireBed;


    public SleeperProfession() {
        isSleeping = false;
        onWayToSleep = false;
        requireBed = true;
        bedTime = 12400;
        wakeUpTime = 0;
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
                trySleep();
            } else if (bedPosition != null) {
                LOGGER.info("going to sleep: " + npc.getName().getString());
                npc.addPathTarget(bedPosition);
                // FIXME: store movement
                setMovement(Movement.FORCED_PATH);
                onWayToSleep = true;
            }
        } else if (time >= wakeUpTime && time < bedTime && (isSleeping || onWayToSleep)) {
            LOGGER.info("waking up: " + npc.getName().getString());
            if (isSleeping) {
                npc.getFakePlayer().wakeUp();
                LOGGER.info("restoring movement: " + lastMovement.toString());
                setMovement(lastMovement);
                npc.setPose(EntityPose.STANDING);
            } else if (onWayToSleep && bedPosition != null) {
                npc.removePathTarget(bedPosition);
            }
            isSleeping = false;
            onWayToSleep = false;
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

        this.requireBed = tag.getBoolean("requireBed");

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

        if (this.bedPosition != null) {
            tag.putInt("bedPos.x", this.bedPosition.getX());
            tag.putInt("bedPos.y", this.bedPosition.getY());
            tag.putInt("bedPos.z", this.bedPosition.getZ());
        }

        tag.putBoolean("requireBed", this.requireBed);

        tag.putString("lastMovement", lastMovement.name());
    }

    @Override
    public void onMovementSet(Movement movement) {
        if (movement == settingMovement) {
            settingMovement = null;
            return;
        }
        lastMovement = movement;
        LOGGER.info("set movement: " + movement.toString());
    }

    private void setMovement(Movement movement) {
        settingMovement = movement;
        npc.setMovement(movement);
    }

    private void trySleep() {
        boolean isInReach = bedPosition.isWithinDistance(npc.getPos(), 2.0);
        if (isInReach) {
            onWayToSleep = false;
            npc.removePathTarget(bedPosition);

            LOGGER.info("sleeping: " + npc.getName().getString());
            BlockState bed = npc.world.getBlockState(bedPosition);
            if (bed.getBlock() instanceof BedBlock || !requireBed) {
                BlockPos realBedPos = bedPosition;
                // Calculate real position to sleep in
                if (bed.getBlock() instanceof BedBlock) {
                    BedBlock bedBlock = (BedBlock) bed.getBlock();
                    if (bed.get(BedBlock.PART) != BedPart.HEAD) {
                        realBedPos = realBedPos.offset((Direction) bed.get(BedBlock.FACING));
                    }
                    realBedPos = realBedPos.add(0, 0.5625, 0);
                } else {
                    realBedPos = realBedPos.add(0, 0.12, 0);
                }

                npc.getFakePlayer().sleep(bedPosition);
                npc.setPose(EntityPose.SLEEPING);
                setMovement(Movement.NONE);
                npc.teleport(realBedPos.getX() + 0.445, realBedPos.getY(), realBedPos.getZ() + 0.532, false);
                isSleeping = true;
            } else {
                LOGGER.info("Could not sleep, possible missing bed");
                setMovement(lastMovement);
            }
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

    public boolean doesRequireBed() {
        return requireBed;
    }

    public void setRequireBed(boolean requireBed) {
        this.requireBed = requireBed;
    }

    public void setBedPosition(BlockPos bedPosition) {
        this.bedPosition = bedPosition;
    }

    public BlockPos getBedPosition() {
        return bedPosition;
    }
}

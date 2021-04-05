package meow.konc.hack.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import meow.konc.hack.event.events.player.EventPlayerPostMotionUpdate;
import meow.konc.hack.event.events.player.EventPlayerPreMotionUpdate;
import meow.konc.hack.module.Module;
import net.minecraft.item.*;

@Module.Info(name = "Aimbot", category = Module.Category.COMBAT, showOnArray = Module.ShowOnArray.OFF)
public class Aimbot extends Module {

    public float yaw, pitch;
    public boolean shouldRotate = false;
    private boolean shouldReset = false;
    //TODO priority rotation
    float rotationYaw, rotationPitch;

    public void setRotation(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
        shouldRotate = true;
    }

    public void resetRotation() {
        shouldRotate = false;
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
        mc.player.setRotationYawHead(mc.player.rotationYaw);
    }

    public static boolean isCurrentViewEntity() {
        return mc.getRenderViewEntity() == mc.player;
    }

    @EventHandler
    private Listener<EventPlayerPostMotionUpdate> w = new Listener<>(event -> {
        if (shouldReset) {
            mc.player.rotationPitch = rotationPitch;
            mc.player.rotationYaw = rotationYaw;
            shouldReset = false;
        }
    });
    @EventHandler
    private Listener<EventPlayerPreMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        /*
        if (p_Event.getEra() != UwUGodEvent.Era.PRE)
            return;

        if (!shouldRotate)
            return;

        p_Event.cancel();


        boolean l_IsSprinting = mc.player.isSprinting();

        if (l_IsSprinting != mc.player.serverSprintState)
        {
            if (l_IsSprinting)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = l_IsSprinting;
        }

        boolean l_IsSneaking = mc.player.isSneaking();

        if (l_IsSneaking != mc.player.serverSneakState)
        {
            if (l_IsSneaking)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = l_IsSneaking;
        }
        if (isCurrentViewEntity()) {
            float l_Pitch = mc.player.rotationPitch;
            float l_Yaw = mc.player.rotationYaw;

            if (shouldRotate) {
                l_Pitch = pitch;
                l_Yaw = yaw;
                mc.player.setRotationYawHead(l_Yaw);
                shouldReset = true;
            }


            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double l_PosXDifference = mc.player.posX - mc.player.lastReportedPosX;
            double l_PosYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
            double l_PosZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
            double l_YawDifference = (double) (l_Yaw - mc.player.lastReportedYaw);
            double l_RotationDifference = (double) (l_Pitch - mc.player.lastReportedPitch);
            ++mc.player.positionUpdateTicks;
            boolean l_MovedXYZ = l_PosXDifference * l_PosXDifference + l_PosYDifference * l_PosYDifference + l_PosZDifference * l_PosZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean l_MovedRotation = l_YawDifference != 0.0D || l_RotationDifference != 0.0D;

            if (mc.player.isRiding()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, l_Yaw, l_Pitch, mc.player.onGround));
                l_MovedXYZ = false;
            } else if (l_MovedXYZ && l_MovedRotation) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, l_Yaw, l_Pitch, mc.player.onGround));
            } else if (l_MovedXYZ) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            } else if (l_MovedRotation) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(l_Yaw, l_Pitch, mc.player.onGround));
            } else if (mc.player.prevOnGround != mc.player.onGround) {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (l_MovedXYZ) {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = axisalignedbb.minY;
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (l_MovedRotation) {
                mc.player.lastReportedYaw = l_Yaw;
                mc.player.lastReportedPitch = l_Pitch;
            }

            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }
*/
        if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle) return;
        if (shouldRotate) {
            rotationPitch = mc.player.rotationPitch;
            rotationYaw = mc.player.rotationYaw;
            mc.player.rotationPitch = pitch;
            mc.player.rotationYaw = yaw;
            shouldReset = true;
        }
    });
}

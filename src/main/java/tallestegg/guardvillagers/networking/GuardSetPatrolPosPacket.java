package tallestegg.guardvillagers.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.entities.Guard;

public class GuardSetPatrolPosPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(GuardVillagers.MODID, "set_patrol_packet");
    private final int entityId;
    private boolean pressed;


    public GuardSetPatrolPosPacket(int entityId, boolean pressed) {
        this.pressed = pressed;
        this.entityId = entityId;
    }

    public GuardSetPatrolPosPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.pressed = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.pressed);
    }


    public int getEntityId() {
        return this.entityId;
    }

    public boolean isPressed() {
        return this.pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public void handle(PlayPayloadContext context) {
        context.workHandler().execute(() -> setPatrolPosition(context.player().orElseThrow(), this));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void setPatrolPosition(Player player, GuardSetPatrolPosPacket packet) {
        if (player != null && player.level() instanceof ServerLevel) {
            Entity entity = player.level().getEntity(packet.getEntityId());
            if (entity instanceof Guard) {
                Guard guard = (Guard) entity;
                BlockPos pos = packet.isPressed() ? null : guard.blockPosition();
                if (guard.blockPosition() != null)
                    guard.setPatrolPos(pos);
                guard.setPatrolling(!packet.isPressed());
                packet.setPressed(!packet.isPressed());
            }
        }
    }
}

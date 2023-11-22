package tallestegg.guardvillagers.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.NetworkEvent;
import tallestegg.guardvillagers.entities.Guard;

public class GuardSetPatrolPosPacket {
    private final int entityId;
    private boolean pressed;

    public GuardSetPatrolPosPacket(int entityId, boolean pressed) {
        this.pressed = pressed;
        this.entityId = entityId;
    }

    public static GuardSetPatrolPosPacket decode(FriendlyByteBuf buf) {
        return new GuardSetPatrolPosPacket(buf.readInt(), buf.readBoolean());
    }

    public static void encode(GuardSetPatrolPosPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.pressed);
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

    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel) {
                Entity entity = player.level().getEntity(this.getEntityId());
                if (entity instanceof Guard) {
                    Guard guard = (Guard) entity;
                    BlockPos pos = this.isPressed() ? null : guard.blockPosition();
                    if (guard.blockPosition() != null)
                        guard.setPatrolPos(pos);
                    guard.setPatrolling(!this.isPressed());
                    this.setPressed(!this.isPressed());
                }
            }
        });
        context.setPacketHandled(true);
    }
}

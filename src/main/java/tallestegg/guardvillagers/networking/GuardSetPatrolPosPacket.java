package tallestegg.guardvillagers.networking;

import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import tallestegg.guardvillagers.entities.GuardEntity;

public class GuardSetPatrolPosPacket {
    private final int entityId;
    private boolean pressed;

    public GuardSetPatrolPosPacket(int entityId, boolean pressed) {
        this.pressed = pressed;
        this.entityId = entityId;
    }

    public static GuardSetPatrolPosPacket decode(PacketBuffer buf) {
        return new GuardSetPatrolPosPacket(buf.readInt(), buf.readBoolean());
    }

    public static void encode(GuardSetPatrolPosPacket msg, PacketBuffer buf) {
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

    public static void handle(GuardSetPatrolPosPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                ((NetworkEvent.Context) context.get()).enqueueWork(new Runnable() {
                    @Override
                    public void run() {
                        ServerPlayerEntity player = ((NetworkEvent.Context) context.get()).getSender();
                        if (player != null && player.world instanceof ServerWorld) {
                            Entity entity = player.world.getEntityByID(msg.getEntityId());
                            if (entity instanceof GuardEntity) {
                                GuardEntity guard = (GuardEntity) entity;
                                BlockPos pos = msg.isPressed() ? null : guard.getPosition();
                                if (guard.getPosition() != null)
                                    guard.setPatrolPos(pos);
                                guard.setPatrolling(!msg.isPressed());
                                msg.setPressed(!msg.isPressed());
                            }
                        }
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}

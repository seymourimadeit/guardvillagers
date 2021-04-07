package tallestegg.guardvillagers.networking;

import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import tallestegg.guardvillagers.entities.GuardEntity;

public class GuardFollowPacket {
    private final int entityId;

    public GuardFollowPacket(int entityId) {
        this.entityId = entityId;
    }

    public static GuardFollowPacket decode(PacketBuffer buf) {
        return new GuardFollowPacket(buf.readInt());
    }

    public static void encode(GuardFollowPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public static void handle(GuardFollowPacket msg, Supplier<NetworkEvent.Context> context) {
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
                                guard.setFollowing(!guard.isFollowing());
                                guard.setOwnerId(player.getUniqueID());
                                guard.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
                            }
                        }
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}
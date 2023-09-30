package tallestegg.guardvillagers.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.network.CustomPayloadEvent;
import tallestegg.guardvillagers.entities.Guard;

public class GuardFollowPacket {
    private final int entityId;

    public GuardFollowPacket(int entityId) {
        this.entityId = entityId;
    }

    public static GuardFollowPacket decode(FriendlyByteBuf buf) {
        return new GuardFollowPacket(buf.readInt());
    }

    public static void encode(GuardFollowPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel) {
                Entity entity = player.level().getEntity(this.getEntityId());
                if (entity instanceof Guard) {
                    Guard guard = (Guard) entity;
                    guard.setFollowing(!guard.isFollowing());
                    guard.setOwnerId(player.getUUID());
                    guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
package tallestegg.guardvillagers.networking;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
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

    public static void handle(GuardFollowPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                ((NetworkEvent.Context) context.get()).enqueueWork(new Runnable() {
                    @Override
                    public void run() {
                        ServerPlayer player = ((NetworkEvent.Context) context.get()).getSender();
                        if (player != null && player.level instanceof ServerLevel) {
                            Entity entity = player.level.getEntity(msg.getEntityId());
                            if (entity instanceof Guard) {
                                Guard guard = (Guard) entity;
                                guard.setFollowing(!guard.isFollowing());
                                guard.setOwnerId(player.getUUID());
                                guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                            }
                        }
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}
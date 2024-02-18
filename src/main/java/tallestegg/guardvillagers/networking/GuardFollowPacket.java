package tallestegg.guardvillagers.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.entities.Guard;

public class GuardFollowPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(GuardVillagers.MODID, "set_following_packet");
    private final int entityId;

    public GuardFollowPacket(int entityId) {
        this.entityId = entityId;
    }

    public GuardFollowPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player().orElseThrow();
            if (player != null && player.level() instanceof ServerLevel) {
                Entity entity = player.level().getEntity(getEntityId());
                if (entity instanceof Guard) {
                    Guard guard = (Guard) entity;
                    guard.setFollowing(!guard.isFollowing());
                    guard.setOwnerId(player.getUUID());
                    guard.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                }
            }
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
package tallestegg.guardvillagers.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.entities.Guard;

public record GuardFollowPacket(int entityId) implements CustomPacketPayload {
    public static final Type<GuardFollowPacket> TYPE = new Type<>(new ResourceLocation(GuardVillagers.MODID, "following"));
    public static final StreamCodec<FriendlyByteBuf, GuardFollowPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, GuardFollowPacket::entityId,
            GuardFollowPacket::new
    );

    public static void handle(GuardFollowPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player != null && player.level() instanceof ServerLevel) {
                Entity entity = player.level().getEntity(packet.entityId());
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
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
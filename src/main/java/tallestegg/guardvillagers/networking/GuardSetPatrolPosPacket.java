package tallestegg.guardvillagers.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.common.entities.Guard;

public record GuardSetPatrolPosPacket(int entityId, boolean pressed) implements CustomPacketPayload {
    public static final Type<GuardSetPatrolPosPacket> TYPE = new Type<>(new ResourceLocation(GuardVillagers.MODID, "set_patrol"));
    public static final StreamCodec<FriendlyByteBuf, GuardSetPatrolPosPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, GuardSetPatrolPosPacket::entityId,
            ByteBufCodecs.BOOL, GuardSetPatrolPosPacket::pressed,
            GuardSetPatrolPosPacket::new
    );

    public static void setPatrolPosition(GuardSetPatrolPosPacket packet, IPayloadContext context) {
        Player player = context.player();
        if (player != null && player.level() instanceof ServerLevel) {
            Entity entity = player.level().getEntity(packet.entityId());
            if (entity instanceof Guard) {
                Guard guard = (Guard) entity;
                BlockPos pos = packet.pressed() ? null : guard.blockPosition();
                if (guard.blockPosition() != null)
                    guard.setPatrolPos(pos);
                guard.setPatrolling(!packet.pressed());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

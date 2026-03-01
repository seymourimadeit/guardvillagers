package tallestegg.guardvillagers.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.common.entities.Guard;

public record GuardSetPatrolPosPacket(int entityId, boolean pressed) implements CustomPacketPayload {
    public static final Type<GuardSetPatrolPosPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "set_patrol_pos"));

    public static final StreamCodec<FriendlyByteBuf, GuardSetPatrolPosPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, GuardSetPatrolPosPacket::entityId,
            ByteBufCodecs.BOOL, GuardSetPatrolPosPacket::pressed,
            GuardSetPatrolPosPacket::new
    );

    public static void handle(final GuardSetPatrolPosPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;

            var entity = sp.serverLevel().getEntity(payload.entityId());
            if (entity instanceof Guard guard) {
                guard.setPatrolling(payload.pressed());

                if (payload.pressed()) {
                    guard.setPatrolPos(sp.blockPosition());
                }
            }
        });
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

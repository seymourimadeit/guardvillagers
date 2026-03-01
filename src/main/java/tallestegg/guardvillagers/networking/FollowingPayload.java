package tallestegg.guardvillagers.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import tallestegg.guardvillagers.GuardVillagers;

public record FollowingPayload(int guardEntityId, boolean following) implements CustomPacketPayload {

    // guardvillagers:following
    public static final CustomPacketPayload.Type<FollowingPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "following"));

    public static final StreamCodec<ByteBuf, FollowingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FollowingPayload::guardEntityId,
            ByteBufCodecs.BOOL, FollowingPayload::following,
            FollowingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
package tallestegg.guardvillagers.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestegg.guardvillagers.GuardPacketHandler;
import tallestegg.guardvillagers.GuardVillagers;

public record GuardOpenInventoryPacket(int id, int size, int entityId) implements CustomPacketPayload {
    public static final Type<GuardOpenInventoryPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "open_inventory"));

    public static final StreamCodec<ByteBuf, GuardOpenInventoryPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, GuardOpenInventoryPacket::id,
            ByteBufCodecs.INT, GuardOpenInventoryPacket::size,
            ByteBufCodecs.INT, GuardOpenInventoryPacket::entityId,
            GuardOpenInventoryPacket::new
    );

    public static void handle(GuardOpenInventoryPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> GuardPacketHandler.openGuardInventory(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
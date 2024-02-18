package tallestegg.guardvillagers.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import tallestegg.guardvillagers.GuardPacketHandler;
import tallestegg.guardvillagers.GuardVillagers;

public class GuardOpenInventoryPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(GuardVillagers.MODID, "open_inventory_packet");
    private final int id;
    private final int size;
    private final int entityId;

    public GuardOpenInventoryPacket(int id, int size, int entityId) {
        this.id = id;
        this.size = size;
        this.entityId = entityId;
    }

    public GuardOpenInventoryPacket(FriendlyByteBuf buf) {
        this.id = buf.readUnsignedByte();
        this.size = buf.readVarInt();
        this.entityId = buf.readInt();
    }

    public static GuardOpenInventoryPacket decode(FriendlyByteBuf buf) {
        return new GuardOpenInventoryPacket(buf.readUnsignedByte(), buf.readVarInt(), buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.id);
        buf.writeVarInt(this.size);
        buf.writeInt(this.entityId);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public int getId() {
        return this.id;
    }

    public int getSize() {
        return this.size;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void handle(PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            GuardPacketHandler.openGuardInventory(this);
        });
    }


}
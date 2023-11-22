package tallestegg.guardvillagers.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import tallestegg.guardvillagers.GuardPacketHandler;

public class GuardOpenInventoryPacket {
    private final int id;
    private final int size;
    private final int entityId;

    public GuardOpenInventoryPacket(int id, int size, int entityId) {
        this.id = id;
        this.size = size;
        this.entityId = entityId;
    }
    
    public static GuardOpenInventoryPacket decode(FriendlyByteBuf buf) {
        return new GuardOpenInventoryPacket(buf.readUnsignedByte(), buf.readVarInt(), buf.readInt());
    }

    public static void encode(GuardOpenInventoryPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.id);
        buf.writeVarInt(msg.size);
        buf.writeInt(msg.entityId);
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

    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            GuardPacketHandler.openGuardInventory(this);
        });
        context.setPacketHandled(true);
    }
}
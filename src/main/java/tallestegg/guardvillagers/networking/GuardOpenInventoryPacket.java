package tallestegg.guardvillagers.networking;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
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
    
    public static GuardOpenInventoryPacket decode(PacketBuffer buf) {
        return new GuardOpenInventoryPacket(buf.readUnsignedByte(), buf.readVarInt(), buf.readInt());
    }

    public static void encode(GuardOpenInventoryPacket msg, PacketBuffer buf) {
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

    public static void handle(GuardOpenInventoryPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            GuardPacketHandler.openGuardInventory(msg);
        });
        context.get().setPacketHandled(true);
    }
}
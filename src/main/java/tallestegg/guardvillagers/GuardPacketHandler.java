package tallestegg.guardvillagers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tallestegg.guardvillagers.client.gui.GuardInventoryScreen;
import tallestegg.guardvillagers.entities.GuardContainer;
import tallestegg.guardvillagers.entities.GuardEntity;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

public class GuardPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GuardVillagers.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, GuardOpenInventoryPacket.class, GuardOpenInventoryPacket::encode, GuardOpenInventoryPacket::decode, GuardOpenInventoryPacket::handle);
        INSTANCE.registerMessage(id++, GuardFollowPacket.class, GuardFollowPacket::encode, GuardFollowPacket::decode, GuardFollowPacket::handle);
        INSTANCE.registerMessage(id++, GuardSetPatrolPosPacket.class, GuardSetPatrolPosPacket::encode, GuardSetPatrolPosPacket::decode, GuardSetPatrolPosPacket::handle);
    }

    @OnlyIn(Dist.CLIENT) // This should be removed when I find a better solution.
    public static void openGuardInventory(GuardOpenInventoryPacket packet) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.world.getEntityByID(packet.getEntityId());
            if (entity instanceof GuardEntity) {
                GuardEntity guard = (GuardEntity) entity;
                ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
                GuardContainer container = new GuardContainer(packet.getId(), player.inventory, guard.guardInventory, guard);
                clientplayerentity.openContainer = container;
                Minecraft.getInstance().displayGuiScreen(new GuardInventoryScreen(container, player.inventory, guard));
            }
        }
    }
}

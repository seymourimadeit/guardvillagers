package tallestegg.guardvillagers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tallestegg.guardvillagers.client.gui.GuardInventoryScreen;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.GuardContainer;
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

    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT)
    public static void openGuardInventory(GuardOpenInventoryPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level.getEntity(packet.getEntityId());
            if (entity instanceof Guard guard) {
                LocalPlayer clientplayerentity = Minecraft.getInstance().player;
                GuardContainer container = new GuardContainer(packet.getId(), player.getInventory(), guard.guardInventory, guard);
                clientplayerentity.containerMenu = container;
                Minecraft.getInstance().setScreen(new GuardInventoryScreen(container, player.getInventory(), guard));
            }
        }
    }
}

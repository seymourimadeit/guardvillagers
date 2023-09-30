package tallestegg.guardvillagers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;
import tallestegg.guardvillagers.client.gui.GuardInventoryScreen;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.GuardContainer;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

public class GuardPacketHandler {
    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = ChannelBuilder.named("guardvillagers").networkProtocolVersion(PROTOCOL_VERSION).clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).simpleChannel();

    public static void registerPackets() {
        INSTANCE.messageBuilder(GuardOpenInventoryPacket.class, 0).encoder(GuardOpenInventoryPacket::encode).decoder(GuardOpenInventoryPacket::decode).consumerMainThread(GuardOpenInventoryPacket::handle).add();
        INSTANCE.messageBuilder(GuardFollowPacket.class, 1).encoder(GuardFollowPacket::encode).decoder(GuardFollowPacket::decode).consumerNetworkThread(GuardFollowPacket::handle).add();
        INSTANCE.messageBuilder(GuardSetPatrolPosPacket.class, 2).encoder(GuardSetPatrolPosPacket::encode).decoder(GuardSetPatrolPosPacket::decode).consumerNetworkThread(GuardSetPatrolPosPacket::handle).add();
    }

    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT)
    public static void openGuardInventory(GuardOpenInventoryPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level().getEntity(packet.getEntityId());
            if (entity instanceof Guard guard) {
                LocalPlayer clientplayerentity = Minecraft.getInstance().player;
                GuardContainer container = new GuardContainer(packet.getId(), player.getInventory(), guard.guardInventory, guard);
                clientplayerentity.containerMenu = container;
                Minecraft.getInstance().setScreen(new GuardInventoryScreen(container, player.getInventory(), guard));
            }
        }
    }
}

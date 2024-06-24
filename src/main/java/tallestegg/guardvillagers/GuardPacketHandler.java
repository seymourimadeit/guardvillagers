package tallestegg.guardvillagers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import tallestegg.guardvillagers.client.gui.GuardInventoryScreen;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.common.entities.GuardContainer;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;

public class GuardPacketHandler {
    @SuppressWarnings("resource")
    public static void openGuardInventory(GuardOpenInventoryPacket packet) {
        if (FMLEnvironment.dist.isClient()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                Entity entity = player.level().getEntity(packet.entityId());
                if (entity instanceof Guard guard) {
                    LocalPlayer clientplayerentity = Minecraft.getInstance().player;
                    GuardContainer container = new GuardContainer(packet.id(), player.getInventory(), guard.guardInventory, guard);
                    clientplayerentity.containerMenu = container;
                    Minecraft.getInstance().setScreen(new GuardInventoryScreen(container, player.getInventory(), guard));
                }
            }
        }
    }
}
package tallestegg.guardvillagers.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.networking.handler.FollowingPayloadHandler;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = GuardVillagers.MODID)
public final class GuardNetworking {
    private GuardNetworking() {}

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                GuardOpenInventoryPacket.TYPE,
                GuardOpenInventoryPacket.STREAM_CODEC,
                GuardOpenInventoryPacket::handle
        );

        registrar.playToServer(GuardFollowPacket.TYPE, GuardFollowPacket.STREAM_CODEC, GuardFollowPacket::handle);
        registrar.playToServer(GuardSetPatrolPosPacket.TYPE, GuardSetPatrolPosPacket.STREAM_CODEC, GuardSetPatrolPosPacket::handle);
    }
}
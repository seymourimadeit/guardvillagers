package tallestegg.guardvillagers.networking.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestegg.guardvillagers.networking.FollowingPayload;

public final class FollowingPayloadHandler {
    private FollowingPayloadHandler() {}

    public static void handle(final FollowingPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            int guardId = payload.guardEntityId();
            boolean following = payload.following();

        });
    }
}
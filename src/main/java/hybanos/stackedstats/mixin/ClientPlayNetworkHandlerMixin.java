package hybanos.stackedstats.mixin;

import hybanos.stackedstats.StackedStatsClient;
import hybanos.stackedstats.StatsSaver;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.FileNotFoundException;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method="onStatistics", at=@At("HEAD"), locals= LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void getStats(StatisticsS2CPacket packet, CallbackInfo info)  {
        if (!(StatsSaver.cancelNextPacket || StackedStatsClient.mc.isInSingleplayer())) {
            StackedStatsClient.saver.saveStats(packet);
        } else if (StatsSaver.cancelNextPacket){
            StatsSaver.cancelNextPacket = false;
            info.cancel();
        }
    }
}

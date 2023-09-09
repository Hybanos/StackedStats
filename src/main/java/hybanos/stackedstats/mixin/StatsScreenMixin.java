package hybanos.stackedstats.mixin;


import hybanos.stackedstats.StackedStatsClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(StatsScreen.class)
public class StatsScreenMixin extends Screen {

    protected StatsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method="createButtons", at=@At("TAIL"), locals= LocalCapture.CAPTURE_FAILHARD)
    private void getStats(CallbackInfo info) {
        if (!StackedStatsClient.mc.isInSingleplayer()) {
            this.addDrawableChild(new ButtonWidget(20, 10, 80, 20, Text.translatable("Inject stats"), button -> StackedStatsClient.saver.load()));
        }
    }
}

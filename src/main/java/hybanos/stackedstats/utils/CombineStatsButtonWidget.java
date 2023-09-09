package hybanos.stackedstats.utils;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CombineStatsButtonWidget extends ButtonWidget {
    public CombineStatsButtonWidget(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        super(x, y, width, height, message, onPress, textSupplier -> (MutableText)textSupplier.get());
    }
}

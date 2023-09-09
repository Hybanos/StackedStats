package hybanos.stackedstats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class StackedStatsClient implements ClientModInitializer {

    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final String MOD_ID = "stackedstats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final File FOLDER = new File(FabricLoader.getInstance().getGameDir().toString(), MOD_ID);
    public static StatsSaver saver;

    @Override
    public void onInitializeClient() {
        saver = new StatsSaver();
    }
}

package hybanos.stackedstats;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import hybanos.stackedstats.mixin.ServerStatHandlerAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.MinecraftVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.registry.Registries;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StatsSaver {

    private Object2IntMap<Stat<?>> statMap = Object2IntMaps.synchronize(new Object2IntOpenHashMap());
    public static boolean cancelNextPacket = false;

    public void saveStats(StatisticsS2CPacket packet) {
        for (Map.Entry<Stat<?>, Integer> entry : packet.getStatMap().entrySet()) {
            Stat<?> stat = entry.getKey();
            int i = entry.getValue();
            this.statMap.put(stat, i);
        }
        this.save();
    }

    public void save() {
        try {
            File serverDir = new File(StackedStatsClient.FOLDER.getPath() + "/" + StackedStatsClient.mc.getCurrentServerEntry().address);
            serverDir.mkdirs();
            File file2 = new File(serverDir, StackedStatsClient.mc.player.getUuidAsString() + ".json");
            FileUtils.writeStringToFile(file2, this.asString());
        }
        catch (IOException iOException) {
        }
    }

    public String asString() {
        HashMap<StatType, JsonObject> map = Maps.newHashMap();
        for (Object2IntMap.Entry entry : this.statMap.object2IntEntrySet()) {
            Stat stat = (Stat)entry.getKey();
            map.computeIfAbsent(stat.getType(), statType -> new JsonObject()).addProperty(ServerStatHandlerAccessor.callGetStatId(stat).toString(), entry.getIntValue());
        }
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry entry : map.entrySet()) {
            jsonObject.add(Registries.STAT_TYPE.getId((StatType)entry.getKey()).toString(), (JsonElement)entry.getValue());
        }
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("stats", jsonObject);
        jsonObject2.addProperty("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
        return jsonObject2.toString();
    }

    public void load() {
        cancelNextPacket = true;
        statMap.clear();
        File serverDir = new File(StackedStatsClient.FOLDER.getPath() + "/" + StackedStatsClient.mc.getCurrentServerEntry().address);
        File[] files = serverDir.listFiles();
        for (File file : files) {
            String json = file.getName();
            try (JsonReader jsonReader = new JsonReader(new FileReader(file));){
                jsonReader.setLenient(false);
                JsonElement jsonElement = Streams.parse(jsonReader);
                if (jsonElement.isJsonNull()) {
                    return;
                }
                NbtCompound nbtCompound = ServerStatHandlerAccessor.callJsonToCompound(jsonElement.getAsJsonObject());

                NbtCompound nbtCompound2 = nbtCompound.getCompound("stats");
                for (String string : nbtCompound2.getKeys()) {
                    if (!nbtCompound2.contains(string, NbtElement.COMPOUND_TYPE)) continue;
                    Util.ifPresentOrElse(Registries.STAT_TYPE.getOrEmpty(new Identifier(string)), statType -> {
                        NbtCompound nbtCompound3 = nbtCompound2.getCompound(string);
                        for (String string2 : nbtCompound3.getKeys()) {
                            if (nbtCompound3.contains(string2, NbtElement.NUMBER_TYPE)) {
                                Util.ifPresentOrElse(this.createStat((StatType)statType, string2), id -> this.statMap.put((Stat<?>) id, nbtCompound3.getInt(string2) + this.statMap.getInt(id)), () -> StackedStatsClient.LOGGER.warn("Invalid statistic in {}: Don't know what {} is", (Object)json, (Object)string2));
                                continue;
                            }
                        }
                    }, () -> StackedStatsClient.LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", (Object)json, (Object)string));
                }
            }
            catch (Exception exception) {
                StackedStatsClient.LOGGER.info(exception.toString());
            }
        }

        for (Map.Entry<Stat<?>, Integer> entry : statMap.entrySet()) {
            Stat<?> stat = entry.getKey();
            int i = entry.getValue();
            StackedStatsClient.mc.player.getStatHandler().setStat(StackedStatsClient.mc.player, stat, i);
        }

        StackedStatsClient.mc.player.closeScreen();
        StackedStatsClient.mc.setScreen(new StatsScreen(null, StackedStatsClient.mc.player.getStatHandler()));

        if (StackedStatsClient.mc.currentScreen instanceof StatsListener) {
            ((StatsListener)((Object)StackedStatsClient.mc.currentScreen)).onStatsReady();
        }
    }

    private <T> Optional<Stat<T>> createStat(StatType<T> type, String id) {
        return Optional.ofNullable(Identifier.tryParse(id)).flatMap(type.getRegistry()::getOrEmpty).map(type::getOrCreateStat);
    }
}

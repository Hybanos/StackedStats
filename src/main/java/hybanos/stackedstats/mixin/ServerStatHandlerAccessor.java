package hybanos.stackedstats.mixin;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatHandler.class)
public interface ServerStatHandlerAccessor {
    @Invoker("getStatId")
    static <T> Identifier callGetStatId(Stat<T> stat) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static NbtCompound callJsonToCompound(JsonObject json) {
        throw new UnsupportedOperationException();
    }
}

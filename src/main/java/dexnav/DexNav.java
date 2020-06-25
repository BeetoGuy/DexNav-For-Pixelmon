package dexnav;

import com.pixelmonmod.pixelmon.Pixelmon;
import dexnav.capability.CapabilityDexTracker;
import dexnav.capability.DexPlayer;
import dexnav.capability.IDexTracker;
import dexnav.event.DexNavEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod(
        modid = DexNav.MOD_ID,
        name = DexNav.MOD_NAME,
        version = DexNav.VERSION,
        dependencies = "required-after:pixelmon@[8.0.0,)"
)
public class DexNav {

    public static final String MOD_ID = "dexnav";
    public static final String MOD_NAME = "DexNav";
    public static final String VERSION = "1.0.0";

    public Random rand = new Random();

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static DexNav INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        DexRegistry.init();
        CapabilityManager.INSTANCE.register(IDexTracker.class, new CapabilityDexTracker.DexTrackingImpl<>(), DexPlayer.class);
        MinecraftForge.EVENT_BUS.register(new DexNavEventHandler.ForgeEvents());
        Pixelmon.EVENT_BUS.register(new DexNavEventHandler.PixelmonEvents());
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppingEvent evt) {
        DexNavEventHandler.clearCache();
    }
}

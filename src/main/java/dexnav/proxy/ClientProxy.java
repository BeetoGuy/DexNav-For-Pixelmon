package dexnav.proxy;

import dexnav.DexRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = "dexnav", value = Side.CLIENT)
public class ClientProxy {
    @SubscribeEvent
    public static void registerRendering(ModelRegistryEvent evt) {
        for (Item item : DexRegistry.ITEMS) {
            registerItemModel(item);
        }
    }

    private static void registerItemModel(Item item) {
        String location = item.getRegistryName() != null ? item.getRegistryName().toString() : "";
        if (!location.equals(""))
            ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(location, "inventory"));
    }
}

package dexnav;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dexnav.item.ItemDexNav;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "dexnav")
public class DexRegistry {
    public static List<Item> ITEMS = Lists.newArrayList();

    public static Item dexNav = new ItemDexNav().setRegistryName(new ResourceLocation("dexnav", "dexnav")).setTranslationKey("dexnav:dexnav");

    public static void init() {
        addItem(dexNav);
    }

    private static void addItem(Item item) {
        ITEMS.add(item);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        ITEMS.forEach(evt.getRegistry()::register);
    }

    @SubscribeEvent
    public static void onMissingItemMapping(RegistryEvent.MissingMappings<Item> evt) {
        ImmutableList<RegistryEvent.MissingMappings.Mapping<Item>> mappings = ImmutableList.copyOf(evt.getAllMappings().stream().filter(e -> e.key.getNamespace().equals("ivchain")).collect(Collectors.toList()));
        if (!mappings.isEmpty()) {
            for (RegistryEvent.MissingMappings.Mapping<Item> map : mappings) {
                String name = map.key.getPath();
                if (evt.getRegistry().containsKey(new ResourceLocation("dexnav", name))) {
                    map.remap(evt.getRegistry().getValue(new ResourceLocation("dexnav", name)));
                }
            }
        }
    }
}

package dexnav.item;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import dexnav.event.DexNavEventHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class ItemDexNav extends Item {
    public ItemDexNav() {
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if (player != null && !player.world.isRemote && entity instanceof EntityPixelmon) {
            EntityPixelmon pixelmon = (EntityPixelmon)entity;
            if (pixelmon.getOwner() == null) {
                player.sendStatusMessage(DexNavEventHandler.PixelmonEvents.getPokemonInformation(player, pixelmon.getPokemonData()), true);
                return true;
            }
        }
        return false;
    }
}

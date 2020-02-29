package dexnav.util;

import dexnav.capability.CapabilityDexTracker;
import dexnav.capability.IDexTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.DimensionManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DexNavSaveManager {
    private EntityPlayerMP player;

    public DexNavSaveManager(EntityPlayerMP player) {
        this.player = player;
    }

    public IDexTracker getTracker() {
        return player.getCapability(CapabilityDexTracker.TRACKER, EnumFacing.UP);
    }

    public void writeToFile() {
        if (getTracker().isDirty()) {
            File file = getFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                CompressedStreamTools.safeWrite(getTracker().getPokedexMap(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readFromFile() {
        if (getFile().exists()) {
            try {
                DataInputStream dataStream = new DataInputStream(new FileInputStream(getFile()));
                Throwable th = null;
                try {
                    NBTTagCompound tag = CompressedStreamTools.read(dataStream);
                    getTracker().readPokedexMap(tag);
                } catch (Throwable thro) {
                    th = thro;
                } finally {
                    if (dataStream != null) {
                        if (th != null) {
                            try {
                                dataStream.close();
                            } catch (Throwable thro) {
                                th.addSuppressed(thro);
                            }
                        } else {
                            dataStream.close();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public File getFile() {
        return new File(DimensionManager.getCurrentSaveRootDirectory(), "dexnav/" + player.getUniqueID().toString() + ".dat");
    }
}

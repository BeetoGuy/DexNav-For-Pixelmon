package dexnav.capability;

import net.minecraft.nbt.NBTTagCompound;

public interface IDexTracker {
    void incrementEncounterValue(short natDex);

    short getEncounterValue(short natDex);

    NBTTagCompound getPokedexMap();

    void readPokedexMap(NBTTagCompound tag);

    boolean isDirty();
}

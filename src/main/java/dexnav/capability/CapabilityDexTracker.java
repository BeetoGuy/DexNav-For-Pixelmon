package dexnav.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityDexTracker {
    @CapabilityInject(IDexTracker.class)
    public static Capability<IDexTracker> TRACKER = null;

    public static class DexTrackingImpl<T extends IDexTracker> implements Capability.IStorage<IDexTracker> {
        @Override
        public NBTBase writeNBT(Capability<IDexTracker> cap, IDexTracker tracker, EnumFacing side) {
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(Capability<IDexTracker> cap, IDexTracker tracker, EnumFacing side, NBTBase nbt) {}
    }

    public static class DexTrackerDefault implements IDexTracker {
        @Override
        public boolean isDirty() {
            return false;
        }

        @Override
        public void incrementEncounterValue(short value) {

        }

        @Override
        public short getEncounterValue(short value) {
            return 0;
        }

        @Override
        public NBTTagCompound getPokedexMap() {
            return new NBTTagCompound();
        }

        @Override
        public void readPokedexMap(NBTTagCompound tag) {}
    }
}

package dexnav.event;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.FishingEvent;
import com.pixelmonmod.pixelmon.api.events.PixelmonBlockStartingBattleEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.items.ItemLure;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import dexnav.DexNav;
import dexnav.capability.CapabilityDexTracker;
import dexnav.capability.IDexTracker;
import dexnav.item.ItemDexNav;
import dexnav.util.DexNavSaveManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DexNavEventHandler {
    public static IDexTracker getPlayer(EntityPlayer player) {
        return player.getCapability(CapabilityDexTracker.TRACKER, EnumFacing.UP);
    }

    public static class ForgeEvents {
        private static ResourceLocation DEXNAV = new ResourceLocation("dexnav", "encounters");
        @SubscribeEvent
        public void attachCapability(AttachCapabilitiesEvent<Entity> evt) {
            if (evt.getObject() instanceof EntityPlayerMP) {
                evt.addCapability(DEXNAV, new ICapabilitySerializable<NBTTagCompound>() {
                    IDexTracker tracker = CapabilityDexTracker.TRACKER.getDefaultInstance();
                    @Override
                    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
                        return cap == CapabilityDexTracker.TRACKER;
                    }

                    @Override
                    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
                        return cap == CapabilityDexTracker.TRACKER ? CapabilityDexTracker.TRACKER.cast(tracker) : null;
                    }

                    @Override
                    public NBTTagCompound serializeNBT() {
                        return new NBTTagCompound();
                    }

                    @Override
                    public void deserializeNBT(NBTTagCompound tag) {}
                });
            }
        }

        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone evt) {
            if (evt.getEntityPlayer() instanceof EntityPlayerMP && evt.getOriginal() instanceof EntityPlayerMP) {
                NBTTagCompound t = getPlayer(evt.getOriginal()).getPokedexMap();
                getPlayer(evt.getEntityPlayer()).readPokedexMap(t);
            }
        }

        @SubscribeEvent
        public void onPlayerLogin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent evt) {
            if (evt.player instanceof EntityPlayerMP) {
                DexNavSaveManager man = new DexNavSaveManager((EntityPlayerMP) evt.player);
                man.readFromFile();
            }
        }

        @SubscribeEvent
        public void onPlayerLogout(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent evt) {
            if (evt.player instanceof EntityPlayerMP) {
                DexNavSaveManager man = new DexNavSaveManager((EntityPlayerMP) evt.player);
                man.writeToFile();
            }
        }
    }

    public static class PixelmonEvents {
        private static final StatsType[] STATS_TYPES = {StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed};

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onGrassEncounter(PixelmonBlockStartingBattleEvent evt) {
            IDexTracker tracker = getPlayer(evt.player);
            if (tracker != null && playerIsHoldingDexNav(evt.player)) {
                if (evt.wildPixelmon1 != null)
                    doDexNavBonuses(evt.player, evt.wildPixelmon1);
                if (evt.wildPixelmon2 != null)
                    doDexNavBonuses(evt.player, evt.wildPixelmon2);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onFishing(FishingEvent.Reel evt) {
            IDexTracker tracker = getPlayer(evt.player);
            if (evt.isPokemon() && tracker != null && playerIsHoldingDexNav(evt.player)) {
                EntityPixelmon pixelmon = (EntityPixelmon)evt.optEntity.get();
                doDexNavBonuses(evt.player, pixelmon);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPixelmonDefeat(BeatWildPixelmonEvent evt) {
            WildPixelmonParticipant pixelmon = evt.wpp;
            String name = pixelmon.controlledPokemon.get(0).getPokemonName();
            EntityPlayerMP player = evt.player;
            advanceChain(player, name);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPixelmonCatch(CaptureEvent.SuccessfulCapture evt) {
            EntityPixelmon pixelmon = evt.getPokemon();
            String name = pixelmon.getPokemonName();
            EntityPlayerMP player = evt.player;
            advanceChain(player, name);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onLegendarySpawn(LegendarySpawnEvent.DoSpawn evt) {
            if (evt.action.spawnLocation.cause instanceof EntityPlayerMP) {
                EntityPixelmon pixelmon = evt.action.getOrCreateEntity();
                EntityPlayerMP player = (EntityPlayerMP)evt.action.spawnLocation.cause;
                if (playerIsHoldingDexNav(player)) {
                    doDexNavBonuses(player, pixelmon);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPixelmonSpawn(SpawnEvent evt) {
            if (evt.action.getOrCreateEntity() instanceof EntityPixelmon && evt.action.spawnLocation.cause instanceof EntityPlayerMP) {
                EntityPixelmon pixelmon = (EntityPixelmon)evt.action.getOrCreateEntity();
                EntityPlayerMP player = (EntityPlayerMP)evt.action.spawnLocation.cause;
                if (playerIsHoldingDexNav(player)) {
                    doDexNavBonuses(player, pixelmon);
                }
            }
        }

        private void doDexNavBonuses(EntityPlayerMP player, EntityPixelmon pixelmon) {
            if (pixelmon != null) {

                if (EnumSpecies.legendaries.contains(pixelmon.getSpecies().getPokemonName()) || EnumSpecies.ultrabeasts.contains(pixelmon.getSpecies().getPokemonName()) || pixelmon.isBossPokemon())
                    return;
                Pokemon poke = pixelmon.getPokemonData();
                IDexTracker tracker = getPlayer(player);
                Random rand = DexNav.INSTANCE.rand;
                if (rand.nextInt(20) == 0) {
                    poke.setLevel(Math.min(100, poke.getLevel() + 10));
                }
                short searchLevel = tracker.getEncounterValue((short) poke.getSpecies().getNationalPokedexInteger());
                short shinyProbability = getShinyProbability(searchLevel);
                if (rand.nextDouble() < (shinyProbability * 0.01D) / 10000D) {
                    double attempts = rand.nextInt(100) < 4 ? 4 : 1;
                    PlayerPartyStorage party = Pixelmon.storageManager.getParty(player);
                    if (party.getShinyCharmState().isActive())
                        attempts += 2;
                    if (party.getLure() != null && party.getLure().type == ItemLure.LureType.SHINY) {
                        attempts *= Math.sqrt(party.getLure().strength.multiplier);
                    }
                    int shinyRate = Math.round(PixelmonConfig.getShinyRate(player.dimension));
                    if (shinyRate > 0) {
                        if (RandomHelper.getRandomChance(attempts / shinyRate)) {
                            poke.setShiny(true);
                        }
                    }
                }
                byte searchStage = getSearchStage(searchLevel);
                int chance = rand.nextInt(100);
                if (threestar[searchStage] > chance) {
                    doIVAllocation(pixelmon, 3);
                } else if (twostar[searchStage] > chance) {
                    doIVAllocation(pixelmon, 2);
                } else if (onestar[searchStage] > chance) {
                    doIVAllocation(pixelmon, 1);
                }
                chance = rand.nextInt(100);
                if (eggmove[searchStage] > chance) {
                    BaseStats stats = pixelmon.getPokemonData().getBaseStats();
                    if (stats != null && stats.getEggMoves() != null) {
                        ArrayList<Attack> eggMoves = stats.getEggMoves();
                        if (!eggMoves.isEmpty())
                            pixelmon.getPokemonData().getMoveset().set(0, eggMoves.get(rand.nextInt(eggMoves.size())));
                    }
                }
                chance = rand.nextInt(100);
                if (hiddenability[searchStage] > chance) {
                    pixelmon.getPokemonData().setAbilitySlot(2);
                }
            }
        }

        private void advanceChain(EntityPlayerMP player, String pixelmonName) {
            getPlayer(player).incrementEncounterValue((short)EnumSpecies.getPokedexNumber(pixelmonName));
        }

        private void doIVAllocation(EntityPixelmon pixelmon, int rolls) {
            List<StatsType> types = Lists.newArrayList();
            for (StatsType type : STATS_TYPES) {
                //If we want to skip already-perfect IVs, then we just don't add them to the list.
                if (pixelmon.getPokemonData().getStats().ivs.get(type) == IVStore.MAX_IVS)
                    continue;
                types.add(type);
            } //If all the IVs are perfect, then this isn't worth going through.
            if (types.isEmpty()) return;
            for (int i = 0; i < rolls && !types.isEmpty(); i++) {
                int place = types.size() == 1 ? 0 : DexNav.INSTANCE.rand.nextInt(types.size());
                pixelmon.getPokemonData().getStats().ivs.set(types.get(place), IVStore.MAX_IVS);
                types.remove(place);
            }
            pixelmon.updateStats();
        }

        private byte[] onestar = {0, 14, 17, 17, 15, 8};
        private byte[] twostar = {0, 1, 9, 16, 17, 24};
        private byte[] threestar = {0, 0, 1, 7, 6, 12};
        private byte[] eggmove = {21, 46, 58, 63, 65, 83};
        private byte[] hiddenability = {0, 0, 5, 15, 20, 23};

        private byte getSearchStage(short searchLevel) {
            if (searchLevel > 99) return 5;
            else if (searchLevel > 49) return 4;
            else if (searchLevel > 24) return 3;
            else if (searchLevel > 9) return 2;
            else if (searchLevel > 4) return 1;
            else return 0;
        }

        private short getShinyProbability(short searchLevel) {
            if (searchLevel > 200) {
                short newLevel = (short)(searchLevel - 200);
                return (short)(800 + newLevel);
            } else if (searchLevel > 100) {
                short newLevel = (short)(searchLevel - 100);
                newLevel *= 2;
                return (short)(600 + newLevel);
            } else {
                return (short)(searchLevel * 6);
            }
        }

        private boolean playerIsHoldingDexNav(EntityPlayer player) {
            return player.getHeldItemMainhand().getItem() instanceof ItemDexNav || player.getHeldItemOffhand().getItem() instanceof ItemDexNav;
        }

        public static TextComponentBase getPokemonInformation(EntityPlayer player, Pokemon poke) {
            IDexTracker tracker = getPlayer(player);
            String info = I18n.translateToLocalFormatted("dexnav.pokecheck.0");
            if (tracker != null) {
                short track = tracker.getEncounterValue((short)poke.getSpecies().getNationalPokedexInteger());
                if (track > 0)
                    info = track >= 5 ? getFullInformation(player, poke) : track >= 3 ? getThirdStageInformation(player, poke) : track == 2 ? getSecondStageInformation(player, poke) : getFirstStageInformation(player, poke);
            }
            return new TextComponentString(info);
        }

        private static String getFirstStageInformation(EntityPlayer player, Pokemon poke) {
            return I18n.translateToLocalFormatted("dexnav.pokecheck.1", poke.getDisplayName(), getPlayer(player).getEncounterValue((short)poke.getSpecies().getNationalPokedexInteger()));
        }

        private static String getSecondStageInformation(EntityPlayer player, Pokemon poke) {
            String name = poke.getDisplayName();
            String move = poke.getMoveset().get(0).getMove().getLocalizedName();
            return I18n.translateToLocalFormatted("dexnav.pokecheck.2", name, getPlayer(player).getEncounterValue((short)poke.getSpecies().getNationalPokedexInteger()), move);
        }

        private static String getThirdStageInformation(EntityPlayer player, Pokemon poke) {
            String name = poke.getDisplayName();
            String move = poke.getMoveset().get(0).getMove().getLocalizedName();
            String ability = poke.getAbilityName();
            return I18n.translateToLocalFormatted("dexnav.pokecheck.3", name, getPlayer(player).getEncounterValue((short)poke.getSpecies().getNationalPokedexInteger()), move, ability);
        }

        private static String getFullInformation(EntityPlayer player, Pokemon poke) {
            String name = poke.getDisplayName();
            String move = poke.getMoveset().get(0).getMove().getLocalizedName();
            String IVs = getPerfectIVs(poke);
            String ability = poke.getAbilityName();
            return I18n.translateToLocalFormatted("dexnav.pokecheck.4", name, getPlayer(player).getEncounterValue((short)poke.getSpecies().getNationalPokedexInteger()), move, ability, IVs);
        }

        private static String getPerfectIVs(Pokemon poke) {
            int perfect = 0;
            for (int stat : poke.getStats().ivs.getArray()) {
                if (stat == 31) {
                    perfect++;
                }
            }
            return perfect >= 3 ? I18n.translateToLocal("dexnav.3star") : perfect == 2 ? I18n.translateToLocal("dexnav.2star") : perfect == 1 ? I18n.translateToLocal("dexnav.1star") : I18n.translateToLocal("dexnav.0star");
        }
    }
}

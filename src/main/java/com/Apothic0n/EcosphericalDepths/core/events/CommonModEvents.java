package com.Apothic0n.EcosphericalDepths.core.events;

import com.Apothic0n.EcosphericalDepths.EcosphericalDepths;
import com.Apothic0n.EcosphericalDepths.core.objects.EcodBlocks;
import com.Apothic0n.EcosphericalDepths.world.dimension.DepthsDimensions;
import com.Apothic0n.EcosphericalDepths.world.dimension.DepthsITeleporter;
import com.Apothic0n.EcosphericalDepths.world.dimension.DepthsTeleporter;
import com.cursedcauldron.wildbackport.common.registry.worldgen.WBFeatures;
import com.cursedcauldron.wildbackport.common.registry.worldgen.WBWorldGeneration;
import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.naterbobber.darkerdepths.init.DDConfiguredFeatures;
import com.naterbobber.darkerdepths.init.DDPlacedFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.orcinus.galosphere.init.GConfiguredFeatures;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static vazkii.quark.content.world.module.GlimmeringWealdModule.placed_glow_extras;
import static vazkii.quark.content.world.module.GlimmeringWealdModule.placed_glow_shrooms;


@Mod.EventBusSubscriber(modid = EcosphericalDepths.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonModEvents {

    @SubscribeEvent
    public static void chorusTeleport(EntityTeleportEvent.ChorusFruit event) {
        LivingEntity Player = event.getEntityLiving();
        if (!Player.level.isClientSide()) {
            MinecraftServer server = Player.getServer();
            if (server != null) {
                ServerLevel overWorld = server.getLevel(Level.OVERWORLD);
                ServerLevel depthsDim = server.getLevel(DepthsDimensions.DepthsDim);
                BlockState standingOn = Player.level.getBlockState(Player.blockPosition().below());
                BlockState standingUnder = Player.level.getBlockState(Player.blockPosition().above(2));
                if (standingUnder != null || standingOn != null) {
                    BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
                    if (Player.level.dimension() == Level.OVERWORLD && standingOn == bedrock) {
                        Player.changeDimension(depthsDim, new DepthsTeleporter(Player.blockPosition(), false));
                    } else if (Player.level.dimension() == DepthsDimensions.DepthsDim && standingUnder == bedrock) {
                        Player.changeDimension(overWorld, new DepthsTeleporter(Player.blockPosition(), true));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        MinecraftServer minecraftServer = player.getServer();
        Level level = player.level;
        if (level.isClientSide) { //Do something to the player from the client every tick
            if (!player.getCooldowns().isOnCooldown(Items.AIR) && !ModList.get().isLoaded("inversia")) { //Use a shared 50s delay if Inversia isn't installed. DO NOT USE WITH TWO THINGS THAT CAN HAPPEN AT THE SAME TIME
                if (level.getBlockState(player.blockPosition().below()) == Blocks.BEDROCK.defaultBlockState() && level.dimension().equals(Level.OVERWORLD)) {
                    player.displayClientMessage(new TranslatableComponent("block.minecraft.bedrock.under"), true);
                    player.playSound(SoundEvents.PORTAL_AMBIENT, 0.3f, 0.8f);
                    player.getCooldowns().addCooldown(Items.AIR, 1000);
                } else if (level.getBlockState(player.blockPosition().above(2)) == Blocks.BEDROCK.defaultBlockState() && level.dimension().equals(DepthsDimensions.DepthsDim)) {
                    player.displayClientMessage(new TranslatableComponent("block.minecraft.bedrock.above"), true);
                    player.playSound(SoundEvents.PORTAL_AMBIENT, 0.3f, 0.8f);
                    player.getCooldowns().addCooldown(Items.AIR, 1000);
                }
            }
        } else if (minecraftServer != null) { //Do something to the player from the server every tick
            ServerPlayer serverPlayer = ((ServerPlayer) event.player);
            if (Level.NETHER.equals(level.dimension()) && !ModList.get().isLoaded("inversia")) { //Do something to the player from the server as long as they are in the nether dimension and inversia is not installed
                if (player.blockPosition().getY() >= 320) {
                    ArrayList teleporterData = DepthsITeleporter.acsendFromNetherRoof(serverPlayer);
                    ServerPlayer newServerPlayer = (ServerPlayer) teleporterData.get(0);
                    BlockPos newPlayerPosition = (BlockPos) teleporterData.get(1);
                    newServerPlayer.teleportTo(minecraftServer.getLevel(DepthsDimensions.DepthsDim), newPlayerPosition.getX(), newPlayerPosition.getY(), newPlayerPosition.getZ(), 0, 0);
                }
            } else if (DepthsDimensions.DepthsDim.equals(level.dimension())) { //Do something to the player from the server as long as they are in the depths dimension
                if (player.blockPosition().getY() <= -144) {
                    ArrayList teleporterData = DepthsITeleporter.fallToNetherRoof(serverPlayer);
                    ServerPlayer newServerPlayer = (ServerPlayer) teleporterData.get(0);
                    BlockPos newPlayerPosition = (BlockPos) teleporterData.get(1);
                    newServerPlayer.teleportTo(minecraftServer.getLevel(Level.NETHER), newPlayerPosition.getX(), newPlayerPosition.getY(), newPlayerPosition.getZ(), 0, 0);
                    player.getCooldowns().addCooldown(Items.INK_SAC, 200);
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerFall(LivingFallEvent event) {
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        if (livingEntity instanceof ServerPlayer serverPlayer && !ModList.get().isLoaded("inversia")) {
            if (serverPlayer.getCooldowns().isOnCooldown(Items.INK_SAC)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void itemUsed(PlayerInteractEvent.RightClickBlock event) {
        Level pLevel = event.getWorld();
        BlockPos pPos =  event.getHitVec().getBlockPos();
        BlockState pBlock = pLevel.getBlockState(pPos);
        ItemStack pStack = event.getItemStack();
        Player player = event.getPlayer();
        if (!pLevel.isClientSide) { //Runs stuff on the server every time a player right clicks a block
            if (pStack.getItem() == Items.GLOW_INK_SAC && pBlock.getBlock() == Blocks.AMETHYST_CLUSTER) {
                pLevel.setBlock(pPos, EcodBlocks.GLOWING_AMETHYST.get().withPropertiesOf(pBlock), 2);
                float f = Mth.randomBetween(pLevel.random, 0.8F, 1.2F);
                pLevel.playSound((Player)null, pPos, SoundEvents.DOLPHIN_EAT, SoundSource.BLOCKS, 1.0F, f);
                player.swing(event.getHand(), true);
                if (!player.isCreative()) {
                    pStack.setCount(pStack.getCount() - 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void biomeLoading(@Nonnull BiomeLoadingEvent event) {
        if (ModList.get().isLoaded("darkerdepths")) {
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.SILVER_ORE);
            event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.MAGMA_ORE);
            if (event.getName().toString().equals("ecod:submerged_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.GLIMMERING_VINES);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.PETRIFIED_BRANCH);
            } else if (event.getName().toString().equals("ecod:tropical_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.AMBER);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.GLIMMERING_VINES);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.HUGE_GLOWSHROOM);
                final Holder<PlacedFeature> SPARSE_GRIME_SURFACE = BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation("ecod", "sparse_grime_surface"), new PlacedFeature(Holder.hackyErase(DDConfiguredFeatures.GRIME_SURFACE), List.of(CountPlacement.of(UniformInt.of(112, 148)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome())));
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, SPARSE_GRIME_SURFACE);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.PETRIFIED_BRANCH);
            } else if (event.getName().toString().equals("ecod:lush_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.AMBER);
                final Holder<PlacedFeature> DENSE_PETRIFIED_BRANCH = BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation("ecod", "dense_petrified_branch"), new PlacedFeature(Holder.hackyErase(DDConfiguredFeatures.PETRIFIED_BRANCH), List.of(CountPlacement.of(UniformInt.of(236, 254)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome())));
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DENSE_PETRIFIED_BRANCH);
            } else if (event.getName().toString().equals("ecod:dripping_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.AMBER);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.ARID_SURFACE);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.ARID_BOULDER);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.PETRIFIED_BRANCH);
            } else if (event.getName().toString().equals("ecod:deep_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.SHALE_SURFACE);
            } else if (event.getName().toString().equals("ecod:molten_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.AMBER);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.MOLTEN_POOL);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, DDPlacedFeatures.MOLTEN_SPRING);
            }
        }
        if (ModList.get().isLoaded("quark")) {
            if (event.getName().toString().equals("ecod:tropical_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, placed_glow_shrooms);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, placed_glow_extras);
            }
        }
        if (ModList.get().isLoaded("galosphere")) {
            Holder<PlacedFeature> GALOSPHERE_LARGE_CEILING_ALLURITE_PLACED = PlacementUtils.register("large_ceiling_allurite", GConfiguredFeatures.LARGE_ALLURITE_CRYSTAL_CEILING, CountPlacement.of(UniformInt.of(60, 90)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome());
            Holder<PlacedFeature> GALOSPHERE_CEILING_ALLURITE_PLACED = PlacementUtils.register("ceiling_allurite", GConfiguredFeatures.ALLURITE_CRYSTAL_CEILING, CountPlacement.of(UniformInt.of(90, 140)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome());
            Holder<PlacedFeature> GALOSPHERE_FLOOR_ALLURITE_PLACED = PlacementUtils.register("floor_allurite", GConfiguredFeatures.ALLURITE_CRYSTAL_FLOOR, CountPlacement.of(UniformInt.of(45, 75)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome());

            Holder<PlacedFeature> GALOSPHERE_LARGE_FLOOR_LUMIERE_PLACED = PlacementUtils.register("large_floor_lumiere", GConfiguredFeatures.LARGE_LUMIERE_CRYSTAL_FLOOR, CountPlacement.of(UniformInt.of(6, 20)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome());
            Holder<PlacedFeature> GALOSPHERE_FLOOR_LUMIERE_PLACED = PlacementUtils.register("floor_lumiere", GConfiguredFeatures.LUMIERE_CRYSTAL_FLOOR, CountPlacement.of(UniformInt.of(10, 16)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome());

            if (event.getName().toString().equals("ecod:frozen_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GALOSPHERE_LARGE_CEILING_ALLURITE_PLACED);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GALOSPHERE_CEILING_ALLURITE_PLACED);
            } else if (event.getName().toString().equals("ecod:lush_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GALOSPHERE_LARGE_FLOOR_LUMIERE_PLACED);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GALOSPHERE_FLOOR_LUMIERE_PLACED);
            } else if (event.getName().toString().equals("ecod:submerged_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GALOSPHERE_LARGE_CEILING_ALLURITE_PLACED);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GALOSPHERE_CEILING_ALLURITE_PLACED);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GALOSPHERE_FLOOR_ALLURITE_PLACED);
            }
        }
        if (ModList.get().isLoaded("alexsmobs")) {
            if (event.getName().getNamespace().equals("ecod")) {
                event.getSpawns().addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(AMEntityRegistry.COCKROACH.get(), 80, 2, 3));
                event.getSpawns().addMobCharge(AMEntityRegistry.COCKROACH.get(), 0.1D, 0.1D);
            }
            if (event.getName().toString().equals("ecod:lush_cavity")) {
                event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.FLUTTER.get(), 140, 3, 6));
                event.getSpawns().addMobCharge(AMEntityRegistry.FLUTTER.get(), 0.1D, 0.05D);
                event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.LEAFCUTTER_ANT.get(), 60, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.LEAFCUTTER_ANT.get(), 0.2D, 0.1D);
            } else if (event.getName().toString().equals("ecod:dripping_cavity")) {
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.CENTIPEDE_HEAD.get(), 130, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.CENTIPEDE_HEAD.get(), 0.3D, 0.3D);
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.ROCKY_ROLLER.get(), 80, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.ROCKY_ROLLER.get(), 0.3D, 0.3D);
            } else if (event.getName().toString().equals("ecod:deep_cavity")) {
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.MIMICUBE.get(), 130, 2, 3));
                event.getSpawns().addMobCharge(AMEntityRegistry.MIMICUBE.get(), 0.2D, 0.2D);
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.SOUL_VULTURE.get(), 150, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.SOUL_VULTURE.get(), 0.2D, 0.2D);
            } else if (event.getName().toString().equals("ecod:frozen_cavity")) {
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.ROCKY_ROLLER.get(), 80, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.ROCKY_ROLLER.get(), 0.4D, 0.3D);
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.FROSTSTALKER.get(), 100, 1, 3));
                event.getSpawns().addMobCharge(AMEntityRegistry.FROSTSTALKER.get(), 0.2D, 0.2D);
            } else if (event.getName().toString().equals("ecod:molten_cavity")) {
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.STRADDLER.get(), 20, 1, 1));
                event.getSpawns().addMobCharge(AMEntityRegistry.STRADDLER.get(), 0.4D, 0.4D);
            } else if (event.getName().toString().equals("ecod:tropical_cavity")) {
                event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.HUMMINGBIRD.get(), 70, 1, 3));
                event.getSpawns().addMobCharge(AMEntityRegistry.HUMMINGBIRD.get(), 0.2D, 0.2D);
                event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.TOUCAN.get(), 50, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.TOUCAN.get(), 0.3D, 0.3D);
                event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.LEAFCUTTER_ANT.get(), 30, 2, 3));
                event.getSpawns().addMobCharge(AMEntityRegistry.LEAFCUTTER_ANT.get(), 0.15D, 0.1D);
                event.getSpawns().addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(AMEntityRegistry.FLUTTER.get(), 100, 3, 6));
                event.getSpawns().addMobCharge(AMEntityRegistry.FLUTTER.get(), 0.1D, 0.05D);
                event.getSpawns().addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(AMEntityRegistry.FLY.get(), 80, 2, 3));
                event.getSpawns().addMobCharge(AMEntityRegistry.FLY.get(), 0.1D, 0.05D);
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.GORILLA.get(), 50, 1, 1));
                event.getSpawns().addMobCharge(AMEntityRegistry.GORILLA.get(), 0.5D, 0.6D);
            } else if (event.getName().toString().equals("ecod:submerged_cavity")) {
                event.getSpawns().addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.BLOBFISH.get(), 40, 3, 4));
                event.getSpawns().addMobCharge(AMEntityRegistry.BLOBFISH.get(), 0.2D, 0.15D);
                event.getSpawns().addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.CATFISH.get(), 60, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.CATFISH.get(), 0.1D, 0.1D);
                event.getSpawns().addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.FLYING_FISH.get(), 40, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.FLYING_FISH.get(), 0.2D, 0.15D);
                event.getSpawns().addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.MIMIC_OCTOPUS.get(), 50, 1, 1));
                event.getSpawns().addMobCharge(AMEntityRegistry.MIMIC_OCTOPUS.get(), 0.3D, 0.25D);

                event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(AMEntityRegistry.SEAL.get(), 60, 1, 2));
                event.getSpawns().addMobCharge(AMEntityRegistry.SEAL.get(), 0.5D, 0.4D);
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.FRILLED_SHARK.get(), 85, 1, 1));
                event.getSpawns().addMobCharge(AMEntityRegistry.FRILLED_SHARK.get(), 0.5D, 0.4D);
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.MANTIS_SHRIMP.get(), 15, 1, 1));
                event.getSpawns().addMobCharge(AMEntityRegistry.MANTIS_SHRIMP.get(), 0.2D, 0.15D);
                event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(AMEntityRegistry.HAMMERHEAD_SHARK.get(), 35, 1, 1));
                event.getSpawns().addMobCharge(AMEntityRegistry.HAMMERHEAD_SHARK.get(), 0.4D, 0.4D);
                event.getSpawns().addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(AMEntityRegistry.LOBSTER.get(), 35, 2, 4));
                event.getSpawns().addMobCharge(AMEntityRegistry.LOBSTER.get(), 0.2D, 0.05D);
                event.getSpawns().addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(AMEntityRegistry.SEAGULL.get(), 75, 1, 1));
                event.getSpawns().addMobCharge(AMEntityRegistry.SEAGULL.get(), 0.2D, 0.1D);
                event.getSpawns().addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(AMEntityRegistry.TERRAPIN.get(), 25, 1, 3));
                event.getSpawns().addMobCharge(AMEntityRegistry.TERRAPIN.get(), 0.4D, 0.15D);
            }
        }
        if (ModList.get().isLoaded("wildbackport")) {
            Holder<PlacedFeature> WILD_SCULK_PATCH = PlacementUtils.register("wild_sculk_patch", WBWorldGeneration.SCULK_PATCH_DEEP_DARK_CONFIG, CountPlacement.of(UniformInt.of(96, 144)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome());
            Holder<PlacedFeature> WILD_ANCIENT_SCULK_PATCH = PlacementUtils.register("wild_ancient_sculk_patch", WBWorldGeneration.SCULK_PATCH_ANCIENT_CITY_CONFIG, CountPlacement.of(UniformInt.of(4, 10)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome());
            if (event.getName().toString().equals("ecod:deep_cavity")) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WILD_ANCIENT_SCULK_PATCH);
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WILD_SCULK_PATCH);
            }
        }
    }
}

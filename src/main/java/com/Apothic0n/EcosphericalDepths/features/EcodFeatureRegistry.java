package com.Apothic0n.EcosphericalDepths.features;

import com.Apothic0n.EcosphericalDepths.EcosphericalDepths;
import com.Apothic0n.EcosphericalDepths.features.configuartions.CatchingFallConfiguration;
import com.Apothic0n.EcosphericalDepths.features.configuartions.FloatingBlobConfiguration;
import com.Apothic0n.EcosphericalDepths.features.configuartions.SpiralConfiguration;
import com.Apothic0n.EcosphericalDepths.features.configuartions.VerticalBlobConfiguration;
import com.Apothic0n.EcosphericalDepths.features.types.*;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class EcodFeatureRegistry {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, EcosphericalDepths.MODID);

    public static final RegistryObject<Feature<VerticalBlobConfiguration>> ADDITIVE_BLOB = FEATURES.register("additive_blob", () ->
            new AdditiveBlobFeature(VerticalBlobConfiguration.CODEC));

    public static final RegistryObject<Feature<VerticalBlobConfiguration>> ADDITIVE_GROUND_BLOB = FEATURES.register("additive_ground_blob", () ->
            new AdditiveGroundBlobFeature(VerticalBlobConfiguration.CODEC));

    public static final RegistryObject<Feature<FloatingBlobConfiguration>> FLOATING_BLOB = FEATURES.register("floating_blob", () ->
            new FloatingBlobFeature(FloatingBlobConfiguration.CODEC));

    public static final RegistryObject<Feature<CatchingFallConfiguration>> CATCHING_FALL = FEATURES.register("catching_fall", () ->
            new CatchingFallFeature(CatchingFallConfiguration.CODEC));

    public static final RegistryObject<Feature<LargeDripstoneConfiguration>> LARGE_BASALT_PILLAR = FEATURES.register("large_basalt_pillar", () ->
            new LargeBasaltPillarFeature(LargeDripstoneConfiguration.CODEC));

    public static final RegistryObject<Feature<LargeDripstoneConfiguration>> PACKED_ICE_PILLAR = FEATURES.register("packed_ice_pillar", () ->
            new LargePackedIcePillarFeature(LargeDripstoneConfiguration.CODEC));

    public static final RegistryObject<Feature<LargeDripstoneConfiguration>> THIN_BLACKSTONE_PILLAR = FEATURES.register("thin_blackstone_pillar", () ->
            new LargeBlackstonePillarFeature(LargeDripstoneConfiguration.CODEC));

    public static final RegistryObject<Feature<LargeDripstoneConfiguration>> LARGE_CALCITE_PILLAR = FEATURES.register("large_calcite_pillar", () ->
            new LargeCalcitePillarFeature(LargeDripstoneConfiguration.CODEC));

    public static final RegistryObject<Feature<VerticalBlobConfiguration>> CRYSTAL_SPIKE = FEATURES.register("crystal_spike", () ->
            new CrystalSpikeFeature(VerticalBlobConfiguration.CODEC));

    public static final RegistryObject<Feature<SpiralConfiguration>> SPIRAL = FEATURES.register("spiral", () ->
            new SpiralFeature(SpiralConfiguration.CODEC));

    public static final RegistryObject<Feature<GeodeConfiguration>> VOID_GEODE = FEATURES.register("void_geode", () ->
            new VoidGeodeFeature(GeodeConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}

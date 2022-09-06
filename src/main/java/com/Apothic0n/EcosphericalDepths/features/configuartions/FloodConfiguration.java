package com.Apothic0n.EcosphericalDepths.features.configuartions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class FloodConfiguration implements FeatureConfiguration {
    public static final Codec<FloodConfiguration> CODEC = RecordCodecBuilder.create((fields) -> {
        return fields.group(BlockStateProvider.CODEC.fieldOf("material").forGetter((v) -> {
            return v.material;
        }), IntProvider.codec(-94, 318).fieldOf("elevation").forGetter((v) -> {
            return v.elevation;
        })).apply(fields, FloodConfiguration::new);
    });
    public final BlockStateProvider material;
    private final IntProvider elevation;

    public FloodConfiguration(BlockStateProvider material, IntProvider elevation) {
        this.material = material;
        this.elevation = elevation;
    }

    public IntProvider getElevation() {return this.elevation;}

}

package com.Apothic0n.EcosphericalDepths.world.dimension;

import com.Apothic0n.EcosphericalDepths.EcosphericalDepths;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DepthsDimensions {
    public static ResourceKey<Level> DepthsDim = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            new ResourceLocation(EcosphericalDepths.MODID, "the_depths"));
}

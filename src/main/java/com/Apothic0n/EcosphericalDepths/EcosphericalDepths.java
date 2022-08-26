package com.Apothic0n.EcosphericalDepths;

import com.Apothic0n.EcosphericalDepths.core.objects.EcodBlocks;
import com.Apothic0n.EcosphericalDepths.features.EcodFeatureRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EcosphericalDepths.MODID)
public class EcosphericalDepths {
    public static final String MODID = "ecod";

    public EcosphericalDepths() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        EcodFeatureRegistry.register(eventBus);
        EcodBlocks.BLOCKS.register(eventBus);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        EcodBlocks.fixBlockRenderLayers();
    }
}
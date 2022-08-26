package com.Apothic0n.EcosphericalDepths.core.events;

import com.Apothic0n.EcosphericalDepths.EcosphericalDepths;
import com.Apothic0n.EcosphericalDepths.core.objects.EcodBlocks;
import com.Apothic0n.EcosphericalDepths.world.dimension.DepthsDimensions;
import com.Apothic0n.EcosphericalDepths.world.dimension.DepthsITeleporter;
import com.Apothic0n.EcosphericalDepths.world.dimension.DepthsTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;


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
}

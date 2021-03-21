package com.xjb.autoshutdown;



import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("autoshutdown")
public class autoshutdown
{
    //Time Till Shutdown (Config)
    private static int maxEmptyTime = 15; // In Seconds;

    //Timer
    public static int ticksSinceEmpty = -1;
    //Server Reference
    private static MinecraftServer server;

    //Whether or not to do the countdown
    private static Boolean countdown;

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public autoshutdown() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,config.spec);

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Auto shutdown is a server only mod. Consider removing it to save ram.", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event)
    {
    }

    @SubscribeEvent
    public void FMLServerStartingEvent(FMLServerStartingEvent event){
        LOGGER.info("Registering Server");
        server = event.getServer();
        ticksSinceEmpty = 0;
        maxEmptyTime = config.timeEmptyBeforeShutdown.get();
        countdown = config.doCountdown.get();
    }
    @SubscribeEvent
    public void ServerTickEvent(TickEvent.ServerTickEvent event){
    if(event.phase == TickEvent.Phase.END){
        if(ticksSinceEmpty != -1){
            ticksSinceEmpty++;
            if(ticksSinceEmpty >= 20){
                Boolean serverEmpty = server.getPlayerList().getPlayers().size() == 0;
                if(serverEmpty){
                    if(ticksSinceEmpty == 20){
                            LOGGER.info("Server is empty. Shutting down in {}.",ticksToTime((maxEmptyTime * 20) - ticksSinceEmpty));
                    }
                    if(ticksSinceEmpty % (20 * 30) == 0){
                        if(countdown)
                            LOGGER.info("Shutting down in {}.",ticksToTime((maxEmptyTime * 20) - ticksSinceEmpty));
                    }
                    if((ticksSinceEmpty / 20) > maxEmptyTime){
                        //Shutdown
                        LOGGER.info("Server has been empty for too long. Shutting Down.");
                        ticksSinceEmpty = -1;
                        server.getCommandManager().handleCommand(server.getCommandSource(),"/say Server Shutting Down");
                        server.getCommandManager().handleCommand(server.getCommandSource(),"/stop");
                    }
                }else{
                    ticksSinceEmpty = 0;
                }
            }
        }

    }
}

    public String ticksToTime(int time){
        int ticks = time;
        String ret = "";
        if(time > (20 * 60 * 60)){ //Hours
            int hours = ((ticks - (ticks % (20 * 60 * 60))) / (20 * 60 * 60));
            ret += hours + ":";
            ticks = (ticks % (20 * 60 * 60));
        }
            int mins = ((ticks - (ticks % (20 * 60))) / (20 * 60));
            if(mins < 10){
                ret += "0";
            }
            ret += mins + ":";
            ticks = (ticks % (20 * 60));
        if(time > (20)){ //Seconds
            int secs = ((ticks - (ticks % (20))) / (20));
            if(secs < 10){
                ret += "0";
            }
            ret += secs;

        }else{
            ret = "< 1 Second";
        }
        return ret;
    }
}

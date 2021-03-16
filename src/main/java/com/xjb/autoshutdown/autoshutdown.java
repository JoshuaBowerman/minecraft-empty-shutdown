package com.xjb.autoshutdown;



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
                        LOGGER.info("Server is empty. Shutting down in " + (maxEmptyTime) + " seconds.");
                    }
                    if(ticksSinceEmpty % (20 * 30) == 0){
                        LOGGER.info("Shutting down in {} seconds.",maxEmptyTime - (ticksSinceEmpty / 20));
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

}

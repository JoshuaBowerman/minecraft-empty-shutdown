package com.xjb.autoshutdown;


import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "autoshutdown", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class config {
    public static final ForgeConfigSpec spec;

    public static final ForgeConfigSpec.IntValue timeEmptyBeforeShutdown;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        {
            builder.push("time");
            {
                timeEmptyBeforeShutdown = builder
                        .comment("Time in seconds to wait after server becomes empty before shutting down.")
                        .defineInRange("timeEmptyBeforeShutdown",300,0,Integer.MAX_VALUE);
            }
            builder.pop();
        }
        spec = builder.build();
    }

    private config() {}

}

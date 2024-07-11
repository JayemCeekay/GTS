package org.pokesplash.gts.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.util.CommandsRegistry;

@Mod(Gts.MOD_ID)
public class GtsForge {
    public GtsForge() {
        Gts.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandsRegistry.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void test(LevelEvent.Unload event) { Gts.server = event.getLevel().getServer();
    }

    @SubscribeEvent
    public void worldLoadEvent(LevelEvent.Load event) {
        Gts.server = event.getLevel().getServer();
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.ServerTickEvent event) {
        Gts.listings.check();
    }

}
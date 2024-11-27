package com.matyrobbrt.mekanisticrouters.client;

import com.matyrobbrt.mekanisticrouters.MekRouters;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = MekRouters.MOD_ID, dist = Dist.CLIENT)
public class MekRoutersClient {
    public MekRoutersClient(IEventBus bus) {
        bus.addListener((final RegisterMenuScreensEvent event) -> {
            event.register(MekRouters.CHEMICAL_MODULE_MENU.get(), ChemicalModuleScreen::new);
            event.register(MekRouters.CHEMICAL_REFILL_MODULE_MENU.get(), ChemicalRefillModuleScreen::new);
        });

        bus.addListener((final RegisterColorHandlersEvent.Item event) -> {
            for (var item : MekRouters.ITEMS.getEntries()) {
                if (item.get() instanceof ModItems.ITintable tintable) {
                    event.register((stack, idx) -> switch (idx) {
                        case 0, 2 -> TintColor.WHITE.getRGB();
                        case 1 -> tintable.getItemTint().getRGB();
                        default -> TintColor.BLACK.getRGB();  // shouldn't get here
                    }, item.get());
                }
            }
        });
    }
}

package com.itsfirestorm.world_of_color;

import com.itsfirestorm.world_of_color.api.PaintRegistryImpl;
import com.itsfirestorm.world_of_color.api.WorldOfColorsAPI;
import com.itsfirestorm.world_of_color.events.BasinDyeEventHandler;
import com.itsfirestorm.world_of_color.items.Paint;
import com.itsfirestorm.world_of_color.registries.ModFluids;
import com.itsfirestorm.world_of_color.registries.ModRecipeSerializers;
import com.itsfirestorm.world_of_color.registries.ModTriggers;
import com.itsfirestorm.world_of_color.registries.ModClientSetup;
import com.itsfirestorm.world_of_color.registries.ModCreativeModeTabs;
import com.itsfirestorm.world_of_color.registries.ModItems;
import com.itsfirestorm.world_of_color.util.EmptyBottleFluidHandler;
import com.itsfirestorm.world_of_color.util.FluidItemHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(WorldOfColors.MODID)
public class WorldOfColors {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "world_of_color";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public WorldOfColors(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        modEventBus.register(Config.class);

        modEventBus.addListener(this::registerCapabilities);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (world_of_color) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Register mod items
        ModItems.ITEMS.register(modEventBus);

        // Register mod triggers
        ModTriggers.TRIGGERS.register(modEventBus);

        // Register mod fluids
        ModFluids.FLUIDS.register(modEventBus);

        // Register mod fluid types
        ModFluids.FLUID_TYPES.register(modEventBus);

        // Register mod creative mode tabs
        ModCreativeModeTabs.register(modEventBus);

        // Register secret item tooltip
        NeoForge.EVENT_BUS.addListener(Paint::onTooltip);

        // Register recipe serializers
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        // Register right-click event for dyeing items in a basin with paint
        NeoForge.EVENT_BUS.addListener(BasinDyeEventHandler::onRightClickBlock);

        // Register client-side events, this will make basins display paint textures.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ModClientSetup::onRegisterClientExtensions);
        }

        // Register API
        WorldOfColorsAPI.internalInit(new PaintRegistryImpl());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register fluid handler capability for all paint items
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> {
                    if (stack.getItem() instanceof Paint paintItem) {
                        return paintItem.getFluid().map(f -> {
                            FluidStack fluidStack = new FluidStack(f.get(), 250);
                            return new FluidItemHandler(stack, fluidStack);
                        }).orElse(null);
                    }
                    return null;
                },
                WorldOfColorsAPI.registry().allPaintItems().values().toArray(Item[]::new)
        );

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new EmptyBottleFluidHandler(stack),
                Items.GLASS_BOTTLE
        );
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}

package com.matyrobbrt.mekanisticrouters;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MRConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BASE_CHEMICAL_RATE = BUILDER.comment("Base Chemical transfer rate (mB/tick)")
            .defineInRange("baseChemicalRate", 1000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CHEMICAL_MODULE_1FE = BUILDER.comment("Energy cost (FE) to run one operation for the Chemical Module Mk1")
            .defineInRange("chemicalModuleEnergyCost", 0, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CHEMICAL_MODULE_2FE = BUILDER.comment("Energy cost (FE) to run one operation for the Chemical Module Mk2")
            .defineInRange("chemicalModule2EnergyCost", 0, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CHEMICAL_REFILL_MODULE_FE = BUILDER.comment("Energy cost (FE) to run one operation for the Chemical Refill Module")
            .defineInRange("chemicalRefillModuleEnergyCost", 0, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CHEMICAL_MODULE_2_BASE_RANGE = BUILDER.comment("Base range for Chemical Module Mk2 (no range upgrades)")
            .defineInRange("chemicalModule2BaseRange", 12, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue CHEMICAL_MODULE_2_MAX_RAMGE = BUILDER.comment("Max range for Chemical Module Mk2")
            .defineInRange("chemicalModule2MaxRange", 24, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CHEMICAL_UPGRADE_MB = BUILDER.comment("Chemical transfer rate increase per Chemical Transfer Upgrade")
                .defineInRange("mBperChemicalUpgrade", 200, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue RADIATION_TRANSFER = BUILDER.comment("Whether to allow routers to transfer irradiated gases.",
                    "NOTE: Enabling this allows voiding nuclear waste and other gases with radiation without releasing the radiation!",
                    "However, tanks placed inside the router will still respect their radiation check no matter what this config value is")
            .define("radiationTransfer", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}

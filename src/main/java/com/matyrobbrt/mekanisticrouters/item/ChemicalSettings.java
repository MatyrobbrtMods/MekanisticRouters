package com.matyrobbrt.mekanisticrouters.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.logic.settings.TransferDirection;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.fluids.FluidType;

public record ChemicalSettings(TransferDirection direction, int maxTransfer, boolean regulateAbsolute) {
    public static final ChemicalSettings DEFAULT = new ChemicalSettings(TransferDirection.TO_ROUTER, 0, false);
    public static final Codec<ChemicalSettings> CODEC = RecordCodecBuilder.create(in -> in.group(
            StringRepresentable.fromEnum(TransferDirection::values)
                    .optionalFieldOf("direction", TransferDirection.TO_ROUTER)
                    .forGetter(ChemicalSettings::direction),
            Codec.INT.optionalFieldOf("maxTransfer", 0).forGetter(ChemicalSettings::maxTransfer),
            Codec.BOOL.optionalFieldOf("regulateAbsolute", false).forGetter(ChemicalSettings::regulateAbsolute)
    ).apply(in, ChemicalSettings::new));
    public static final StreamCodec<ByteBuf, ChemicalSettings> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public int getEffectiveMaxTransfer(int routerMax) {
        return maxTransfer() == 0 ? routerMax : Math.min(maxTransfer(), routerMax);
    }
}

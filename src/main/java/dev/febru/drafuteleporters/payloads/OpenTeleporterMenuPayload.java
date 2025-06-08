package dev.febru.drafuteleporters.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

import dev.febru.drafuteleporters.manager.TeleporterDataManager.TeleporterData;
import net.minecraft.util.math.BlockPos;

public record OpenTeleporterMenuPayload(List<TeleporterData> teleporters) implements CustomPayload {
    public static final CustomPayload.Id<OpenTeleporterMenuPayload> ID = new CustomPayload.Id<>(Identifier.of("drafuteleporters", "open_teleporter_menu"));

    // Codec for TeleporterData
    public static final PacketCodec<RegistryByteBuf, TeleporterData> TELEPORTER_DATA_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, data -> data.name,
            BlockPos.PACKET_CODEC, data -> data.pos1,
            BlockPos.PACKET_CODEC, data -> data.pos2,
            TeleporterData::new
    );

    // Codec that handles the list of TeleporterData
    public static final PacketCodec<RegistryByteBuf, OpenTeleporterMenuPayload> CODEC = PacketCodec.tuple(
            TELEPORTER_DATA_CODEC.collect(PacketCodecs.toList()), OpenTeleporterMenuPayload::teleporters,
            OpenTeleporterMenuPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
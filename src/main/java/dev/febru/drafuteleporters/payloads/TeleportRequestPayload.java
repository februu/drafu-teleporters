package dev.febru.drafuteleporters.payloads;

import dev.febru.drafuteleporters.managers.TeleporterDataManager.TeleporterData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;

public record TeleportRequestPayload(UUID playerUuid, TeleporterData teleporterData, boolean cancelled) implements CustomPayload {
    public static final Id<TeleportRequestPayload> ID = new Id<>(Identifier.of("drafuteleporters", "teleport_request"));

    // Codec for TeleporterData
    public static final PacketCodec<RegistryByteBuf, TeleporterData> TELEPORTER_DATA_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, data -> data.name,
            BlockPos.PACKET_CODEC, data -> data.pos1,
            BlockPos.PACKET_CODEC, data -> data.pos2,
            TeleporterData::new
    );

    public static final PacketCodec<RegistryByteBuf, TeleportRequestPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                PacketCodecs.STRING.encode(buf, payload.playerUuid.toString());
                PacketCodecs.BOOLEAN.encode(buf, payload.cancelled);
                if (!payload.cancelled) {
                    TELEPORTER_DATA_CODEC.encode(buf, payload.teleporterData);
                }
            },
            (buf) -> {
                UUID playerUuid = UUID.fromString(PacketCodecs.STRING.decode(buf));
                boolean cancelled = PacketCodecs.BOOLEAN.decode(buf);
                TeleporterData teleporterData = cancelled ? null : TELEPORTER_DATA_CODEC.decode(buf);
                return new TeleportRequestPayload(playerUuid, teleporterData, cancelled);
            }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
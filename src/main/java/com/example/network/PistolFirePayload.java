package com.example.network;

import com.example.TemplateMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PistolFirePayload(boolean aiming) implements CustomPacketPayload {
	public static final Type<PistolFirePayload> TYPE = new Type<>(TemplateMod.id("pistol_fire"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PistolFirePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, PistolFirePayload::aiming, PistolFirePayload::new);

	@Override
	public Type<PistolFirePayload> type() {
		return TYPE;
	}
}

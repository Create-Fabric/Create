package com.simibubi.create.infrastructure.debugInfo;

import java.util.List;
import java.util.Objects;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.debugInfo.element.DebugInfoSection;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class ServerDebugInfoPacket extends SimplePacketBase {

	private final List<DebugInfoSection> serverInfo;
	private final Player player;

	public ServerDebugInfoPacket(Player player) {
		this.serverInfo = DebugInformation.getServerInfo();
		this.player = player;
	}

	public ServerDebugInfoPacket(FriendlyByteBuf buffer) {
		this.serverInfo = buffer.readList(DebugInfoSection::readDirect);
		this.player = null;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeCollection(this.serverInfo, (buf, section) -> section.write(player, buf));
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> this::handleOnClient));
		return true;
	}

	private void printInfo(String side, Player player, List<DebugInfoSection> sections, StringBuilder output) {
		output.append("<details>");
		output.append('\n');
		output.append("<summary>")
			.append(side)
			.append(" Info")
			.append("</summary>");
		output.append('\n')
			.append('\n');
		output.append("```");
		output.append('\n');

		for (int i = 0; i < sections.size(); i++) {
			if (i != 0) {
				output.append('\n');
			}
			sections.get(i)
				.print(player, line -> output.append(line)
					.append('\n'));
		}

		output.append("```");
		output.append('\n')
			.append('\n');
		output.append("</details>");
		output.append('\n');
	}

	@Environment(EnvType.CLIENT)
	private void handleOnClient() {
		Player player = Objects.requireNonNull(Minecraft.getInstance().player);
		StringBuilder output = new StringBuilder();
		List<DebugInfoSection> clientInfo = DebugInformation.getClientInfo();

		printInfo("Client", player, clientInfo, output);
		output.append("\n\n");
		printInfo("Server", player, serverInfo, output);

		String text = output.toString();
		Minecraft.getInstance().keyboardHandler.setClipboard(text);
		Lang.translate("command.debuginfo.saved_to_clipboard")
			.color(DyeHelper.DYE_TABLE.get(DyeColor.LIME)
				.getFirst())
			.sendChat(player);
	}
}

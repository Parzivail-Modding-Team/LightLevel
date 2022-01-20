package com.parzivail.lightlevel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.LightType;

public class LLWorldRender
{
	private static final VertexConsumerProvider vertConsumer;

	static
	{
		vertConsumer = VertexConsumerProvider.immediate(new BufferBuilder(256));
	}

	private static void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f)
	{
		if (!LightLevel.isEnabled())
			return;

		var client = MinecraftClient.getInstance();
		if (client == null)
			return;

		var player = client.player;
		var world = client.world;
		if (player == null || world == null)
			return;

		var f = client.textRenderer;
		if (f == null)
			return;

		var pos = camera.getPos();

		matrices.push();

		var showBothValues = client.options.debugEnabled;

		var s = showBothValues ? 1 / 32f : 1 / 16f;

		var frustum = new Frustum(matrices.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix());
		frustum.setPosition(pos.x, pos.y, pos.z);

		var playerPos = player.getBlockPos();
		matrices.translate(-pos.x, -pos.y, -pos.z);
		matrices.translate(playerPos.getX(), playerPos.getY(), playerPos.getZ());

		var mutablePos = playerPos.mutableCopy();
		var q = new Quaternion(Vec3f.POSITIVE_X, -90, true);

		VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

		for (int x = -16; x < 16; x++)
			for (int y = -16; y < 2; y++)
				for (int z = -16; z < 16; z++)
				{
					var queryPos = mutablePos.set(playerPos, x, y, z);

					if (!frustum.isVisible(new Box(queryPos)) || !world.isTopSolid(queryPos.down(), player) || world.isTopSolid(queryPos, player))
						continue;

					matrices.push();
					matrices.translate(x, y, z);
					matrices.multiply(q);
					matrices.scale(s, -s, 1);

					var blockLight = world.getLightLevel(LightType.BLOCK, queryPos);
					var skyLight = world.getLightLevel(LightType.SKY, queryPos);

					var color = 0xFFFFFF; // spawn never

					if (blockLight == 0)
					{
						if (skyLight == 0)
							color = 0xFF0000; // Spawn at any time
						else
							color = 0xFFFF00; // Spawn only at night
					}

					if (showBothValues)
					{
						drawNumber(matrices, immediate, f, "■" + blockLight, color, 13, 8);
						drawNumber(matrices, immediate, f, "☀" + skyLight, color, 21, 23);
					}
					else
						drawNumber(matrices, immediate, f, String.valueOf(blockLight), color, 9, 8);

					matrices.pop();
				}

		matrices.pop();

		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-1, -2);

		immediate.draw();

		RenderSystem.disablePolygonOffset();
	}

	private static void drawNumber(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, TextRenderer f, String str, int color, int offsetX, int offsetY)
	{
		matrices.push();
		matrices.translate(offsetX - str.length() * 3.5f, offsetY + 1 - f.fontHeight / 2f, 0);
		f.draw(str, 0, 0, color & 0x3F3F3F, false, matrices.peek().getPositionMatrix(), immediate, false, 0, 0xF000F0, false);
		matrices.translate(-1.1, -0.9, 0.0005);
		f.draw(str, 0, 0, color, false, matrices.peek().getPositionMatrix(), immediate, false, 0, 0xF000F0, false);
		matrices.pop();
	}

	public static void afterTranslucent(WorldRenderContext wrc)
	{
		if (wrc.advancedTranslucency())
		{
			LLWorldRender.render(wrc.matrixStack(), wrc.tickDelta(), wrc.limitTime(), wrc.blockOutlines(), wrc.camera(), wrc.gameRenderer(), wrc.lightmapTextureManager(), wrc.projectionMatrix());
		}
	}

	public static void onEnd(WorldRenderContext wrc)
	{
		if (!wrc.advancedTranslucency())
		{
			LLWorldRender.render(wrc.matrixStack(), wrc.tickDelta(), wrc.limitTime(), wrc.blockOutlines(), wrc.camera(), wrc.gameRenderer(), wrc.lightmapTextureManager(), wrc.projectionMatrix());
		}
	}
}

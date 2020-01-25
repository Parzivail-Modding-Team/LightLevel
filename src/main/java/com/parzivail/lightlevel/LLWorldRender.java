package com.parzivail.lightlevel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

public class LLWorldRender
{
	private static VertexConsumerProvider vertConsumer;

	static
	{
		vertConsumer = VertexConsumerProvider.immediate(new BufferBuilder(256));
	}

	public static void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f)
	{
		if (!LightLevel.isEnabled())
			return;

		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null)
			return;

		ClientPlayerEntity player = client.player;
		ClientWorld world = client.world;
		if (player == null || world == null)
			return;

		TextRenderer f = client.getFontManager().getTextRenderer(MinecraftClient.DEFAULT_TEXT_RENDERER_ID);
		if (f == null)
			return;

		Vec3d pos = camera.getPos();

		matrices.push();

		boolean showBothValues = client.options.debugEnabled;

		float s = showBothValues ? 1 / 32f : 1 / 16f;

		matrices.translate(-pos.x, -pos.y, -pos.z);

		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-1, -1);

		for (int x = -16; x < 16; x++)
			for (int y = -16; y < 16; y++)
				for (int z = -16; z < 16; z++)
				{
					BlockPos queryPos = player.getBlockPos().add(x, y, z);

					if (!world.isTopSolid(queryPos.down(), player) || world.isTopSolid(queryPos, player))
						continue;

					matrices.push();
					matrices.translate(queryPos.getX(), queryPos.getY(), queryPos.getZ());
					matrices.multiply(new Quaternion(Vector3f.POSITIVE_X, -90, true));
					matrices.scale(s, -s, s);
					matrix4f = matrices.peek().getModel();
					RenderSystem.pushMatrix();
					RenderSystem.multMatrix(matrix4f);

					int blockLight = world.getLightLevel(LightType.BLOCK, queryPos);
					int skyLight = world.getLightLevel(LightType.SKY, queryPos);

					int color = 0xFFFFFF; // spawn never

					if (blockLight < 8)
					{
						if (skyLight < 8)
							color = 0xFF0000; // Spawn at any time
						else
							color = 0xFFFF00; // Spawn only at night
					}

					if (showBothValues)
					{
						drawNumber(f, "■" + blockLight, color, 8, 8);
						drawNumber(f, "☀" + skyLight, color, 19, 18);
					}
					else
						drawNumber(f, String.valueOf(blockLight), color, 8, 8);

					matrices.pop();

					RenderSystem.popMatrix();
				}

		matrices.pop();

		RenderSystem.disablePolygonOffset();
	}

	private static void drawNumber(TextRenderer f, String str, int color, int offsetX, int offsetY)
	{
		int w = f.getStringWidth(str);

		RenderSystem.translated(offsetX - w / 2f, offsetY + 1 - f.fontHeight / 2f, 0);

		f.drawWithShadow(str, 0, 0, color);
	}
}

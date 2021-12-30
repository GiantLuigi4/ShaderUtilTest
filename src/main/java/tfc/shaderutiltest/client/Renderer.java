package tfc.shaderutiltest.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import tfc.shaderutiltest.tile.EnumShader;
import tfc.shaderutiltest.tile.TestBlock;
import tfc.shaderutiltest.tile.TestTile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

public class Renderer implements BlockEntityRenderer {
	public Renderer() {
	}
	
	private static final HashMap<EnumShader, DistortionBuffer> buffers = new HashMap<>();
	
	static {
		for (EnumShader value : EnumShader.values()) {
			if (value == EnumShader.NONE) continue;
			buffers.put(value, new DistortionBuffer(
					new Identifier("shaderutiltest", value.asString().toLowerCase() + "/blur"),
					new Identifier("shaderutiltest:blit"),
					new Identifier("shaderutiltest", value.asString().toLowerCase() + "/distort"),
					value.asString().toLowerCase()
			));
		}
	}
	
	public static void resetFBOs() {
		for (DistortionBuffer value : buffers.values()) {
			value.addPasses();
			value.clearFBO();
		}
	}
	
	public static void finishFrame() {
		for (DistortionBuffer value : buffers.values()) value.finishFrame();
	}
	
	public static void reloadResources(ResourceManager manager) throws IOException {
		for (DistortionBuffer value : buffers.values()) value.reloadResources(manager);
	}
	
	public static void updateFBOSize(int framebufferWidth, int framebufferHeight) {
		for (DistortionBuffer value : buffers.values()) value.updateFBOSize(framebufferWidth, framebufferHeight);
	}
	
	@Override
	public void render(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		TestTile tile = (TestTile) entity;
		BlockState state = tile.getCachedState();
		
		EnumShader distortion = state.get(TestBlock.SHADER);
		if (distortion == EnumShader.NONE) {
			Camera camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
			matrices.push();
			matrices.translate(0.5, 0.5, 0.5);
			matrices.peek().getModel().multiply(camera.getRotation());
			matrices.multiply(new Quaternion(-90, 0, 0, true));
//			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("shaderutiltest:textures/distort.png")));
			renderWater(tile, matrices, vertexConsumers, LightmapTextureManager.pack(15, 15));
			matrices.pop();
			vertexConsumers.getBuffer(RenderLayer.getSolid()); // force render
			return;
		}
		
		DistortionBuffer buffer = buffers.get(distortion);
		
		buffer.bind();
		
		matrices.push();
//		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("shaderutiltest:textures/distort.png")));
		if (distortion == EnumShader.BETWEENLANDS) {
			matrices.translate(0.5, 0.5, 0.5);
			Camera camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
			matrices.peek().getModel().multiply(camera.getRotation());
			matrices.multiply(new Quaternion(-90, 0, 0, true));
			renderWater(tile, matrices, vertexConsumers, LightmapTextureManager.pack(15, 15));
		} else {
			BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
			manager.renderBlock(
					Blocks.STONE.getDefaultState(),
					tile.getPos(),
					tile.getWorld(),
					matrices,
					vertexConsumers.getBuffer(RenderLayer.getSolid()),
					false, new Random(0)
			);
			vertexConsumers.getBuffer(RenderLayer.getCutout());
		}
		matrices.pop();
		
		buffer.finish(Optional.of(vertexConsumers));
	}
	
	// https://github.com/BloomhouseMC/TerraFabriCraft/blob/c26f7f50695a0f666f82c245487ac6e013d31084/src/main/java/com/bloomhousemc/terrafabricraft/client/renderer/block/KegRenderer.java#L66-L80
	private void renderWater(TestTile entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.translate(-0.5, 0, -0.5);
		// TODO: find a better way to get a sphere-like shape into the depth buffer
		for (int i = 27; i >= 15; i--) {
			matrices.push();
			matrices.translate(0, Math.sin(Math.toRadians(i * (180 / 32f))) / 1.2f, 0);
			float sizeFactor = (float) Math.cos(Math.toRadians(i * (180 / 32f)));
			int red = 255;
			int green = 255;
			int blue = 255;
			Matrix4f matrix4f = matrices.peek().getModel();
//			VertexConsumer buffer = vertexConsumers.getBuffer()
			if (i != 27) GlStateManager._colorMask(false, false, false, false);
			else GlStateManager._colorMask(true, true, true, true);
			VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("shaderutiltest:textures/distort.png")));
			buffer.vertex(matrix4f, sizeFactor, 0, 1 - sizeFactor).color(red, green, blue, 255).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 1, 1).next();
			buffer.vertex(matrix4f, 1 - sizeFactor, 0, 1 - sizeFactor).color(red, green, blue, 255).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 1, 1).next();
			buffer.vertex(matrix4f, 1 - sizeFactor, 0, sizeFactor).color(red, green, blue, 255).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 1, 1).next();
			buffer.vertex(matrix4f, sizeFactor, 0, sizeFactor).color(red, green, blue, 255).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 1, 1).next();
			if (i == 27) vertexConsumers.getBuffer(RenderLayer.getSolid());
			matrices.pop();
		}
		vertexConsumers.getBuffer(RenderLayer.getSolid());
		matrices.pop();
	}
}

package tfc.shaderutiltest.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import tfc.shaderutiltest.tile.EnumDistortion;
import tfc.shaderutiltest.tile.TestBlock;
import tfc.shaderutiltest.tile.TestTile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class Renderer implements BlockEntityRenderer {
	public Renderer() {
	}
	
	private static final HashMap<EnumDistortion, DistortionBuffer> buffers = new HashMap<>();
	
	static {
		for (EnumDistortion value : EnumDistortion.values()) {
			if (value == EnumDistortion.NONE) continue;
			buffers.put(value, new DistortionBuffer(
					new Identifier("shaderutiltest", value.asString().toLowerCase() + "_blur"),
					new Identifier("shaderutiltest:blit"),
					new Identifier("shaderutiltest", value.asString().toLowerCase() + "_distort"),
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
		
		EnumDistortion distortion = state.get(TestBlock.SHADER);
		if (distortion == EnumDistortion.NONE) {
			Camera camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
			matrices.push();
			matrices.translate(0.5, 0.5, 0.5);
			matrices.peek().getModel().multiply(camera.getRotation());
			matrices.multiply(new Quaternion(-90, 0, 0, true));
			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("shaderutiltest:textures/distort.png")));
			renderWater(tile, matrices, vertexConsumers, LightmapTextureManager.pack(15, 15));
			matrices.pop();
			vertexConsumers.getBuffer(RenderLayer.getSolid()); // force render
			return;
		}
		
		DistortionBuffer buffer = buffers.get(distortion);
		
		buffer.bind();
		
		Camera camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera;
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.peek().getModel().multiply(camera.getRotation());
		matrices.multiply(new Quaternion(-90, 0, 0, true));
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("shaderutiltest:textures/distort.png")));
		renderWater(tile, matrices, vertexConsumers, LightmapTextureManager.pack(15, 15));
		matrices.pop();
		
		buffer.finish(Optional.of(vertexConsumers));
	}
	
	float minU = 0;
	float minV = 0;
	float maxU = 255;
	float maxV = 255;
	float angle = 0;
	float prevAngle = 0;
	float colorRed = 0;
	float colorBlue = 0;
	float colorGreen = 0;
	float colorAlpha = 0;
	
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
			buffer.vertex(matrix4f, sizeFactor, 0, sizeFactor).color(red, green, blue, 255).texture(0, 0 + minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(1, 1, 1).next();
			if (i == 27) vertexConsumers.getBuffer(RenderLayer.getSolid());
			matrices.pop();
		}
		vertexConsumers.getBuffer(RenderLayer.getSolid());
		matrices.pop();
	}
	
	private void drawQuad(VertexConsumer vertexConsumer, Camera camera, float tickDelta, float x, float y, float z, int light) {
		Vec3d vec3d = camera.getPos();
		float f = (float) (x - vec3d.getX());
		float g = (float) (y - vec3d.getY());
		float h = (float) (z - vec3d.getZ());
		Quaternion quaternion;
		if (this.angle == 0.0F) {
			quaternion = camera.getRotation();
		} else {
			quaternion = new Quaternion(camera.getRotation());
			float i = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
			quaternion.hamiltonProduct(Vec3f.POSITIVE_Z.getRadialQuaternion(i));
		}
		
		Vec3f i = new Vec3f(-1.0F, -1.0F, 0.0F);
		i.rotate(quaternion);
		Vec3f[] vec3fs = new Vec3f[]{new Vec3f(-1.0F, -1.0F, 0.0F), new Vec3f(-1.0F, 1.0F, 0.0F), new Vec3f(1.0F, 1.0F, 0.0F), new Vec3f(1.0F, -1.0F, 0.0F)};
		float j = 1;
		
		for (int k = 0; k < 4; ++k) {
			Vec3f vec3f = vec3fs[k];
			vec3f.rotate(quaternion);
			vec3f.scale(j);
			vec3f.add(f, g, h);
		}
		
		float k = this.minU;
		float vec3f = this.maxU;
		float l = this.minV;
		float m = this.maxV;
		int n = light;
		vertexConsumer.vertex(vec3fs[0].getX(), vec3fs[0].getY(), vec3fs[0].getZ()).color(this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha).texture(vec3f, m).overlay(OverlayTexture.DEFAULT_UV).light(n).normal(0, 0, 0).next();
		vertexConsumer.vertex(vec3fs[1].getX(), vec3fs[1].getY(), vec3fs[1].getZ()).color(this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha).texture(vec3f, l).overlay(OverlayTexture.DEFAULT_UV).light(n).normal(0, 0, 0).next();
		vertexConsumer.vertex(vec3fs[2].getX(), vec3fs[2].getY(), vec3fs[2].getZ()).color(this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha).texture(k, l).overlay(OverlayTexture.DEFAULT_UV).light(n).normal(0, 0, 0).next();
		vertexConsumer.vertex(vec3fs[3].getX(), vec3fs[3].getY(), vec3fs[3].getZ()).color(this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha).texture(k, m).overlay(OverlayTexture.DEFAULT_UV).light(n).normal(0, 0, 0).next();
	}
}

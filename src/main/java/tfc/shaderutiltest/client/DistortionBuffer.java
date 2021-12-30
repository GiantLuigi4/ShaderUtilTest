package tfc.shaderutiltest.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.JsonEffectGlShader;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import tfc.shaderutil.client.api.FBOBinder;
import tfc.shaderutil.client.api.PostProcessingUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DistortionBuffer {
	private final Framebuffer main = new SimpleFramebuffer(10, 10, true, MinecraftClient.IS_SYSTEM_MAC);
	private final Framebuffer swap = new SimpleFramebuffer(10, 10, true, MinecraftClient.IS_SYSTEM_MAC);
	// TODO: reduce count of framebuffers used
	private final Framebuffer dst = new SimpleFramebuffer(10, 10, true, MinecraftClient.IS_SYSTEM_MAC);
//	private final Framebuffer dst = main;
	
	private PostProcessShader blur;
	private PostProcessShader blit;
	
	public PostProcessShader distort;
	
	public void updateFBOSize(int framebufferWidth, int framebufferHeight) {
		if (framebufferHeight > 0 && framebufferWidth > 0) {
			main.resize(framebufferWidth, framebufferHeight, MinecraftClient.IS_SYSTEM_MAC);
			swap.resize(framebufferWidth, framebufferHeight, MinecraftClient.IS_SYSTEM_MAC);
			dst.resize(framebufferWidth, framebufferHeight, MinecraftClient.IS_SYSTEM_MAC);
		}
	}
	
	public void clearFBO() {
		try {
			writeBuffer(main, "fboPreClear");
		} catch (Throwable ignored) {
		}
		FBOBinder binder = new FBOBinder();
		main.setClearColor(0, 0, 0, 0);
		main.clear(MinecraftClient.IS_SYSTEM_MAC);
		binder.rebind();
		MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
	}
	
	protected void drawBuffer(PostProcessShader pshader, Framebuffer fbo, int width, int height, boolean disableBlend) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager._colorMask(true, true, true, false);
		GlStateManager._disableDepthTest();
		GlStateManager._depthMask(false);
		GlStateManager._viewport(0, 0, width, height);
		if (disableBlend) {
			GlStateManager._disableBlend();
		}
		
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		JsonEffectGlShader shader = pshader.getProgram();
		shader.bindSampler("DiffuseSampler", fbo::getColorAttachment);
		Matrix4f matrix4f = Matrix4f.projectionMatrix((float) width, (float) (-height), 1000.0F, 3000.0F);
		RenderSystem.setProjectionMatrix(matrix4f);
//		if (shader.modelViewMat != null) {
//			shader.modelViewMat.set(Matrix4f.translate(0.0F, 0.0F, -2000.0F));
//		}

//		if (shader.projectionMat != null) {
//			shader.projectionMat.set(matrix4f);
//		}
		shader.getUniformByNameOrDummy("ProjMat").set(matrix4f);
		
		shader.enable();
		float f = (float) width;
		float g = (float) height;
		float h = (float) fbo.viewportWidth / (float) fbo.textureWidth;
		float i = (float) fbo.viewportHeight / (float) fbo.textureHeight;
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(0.0D, (double) g, 0.0D).texture(0, i).color(255, 255, 255, 255).next();
		bufferBuilder.vertex((double) f, (double) g, 0.0D).texture(h, 0).color(255, 255, 255, 255).next();
		bufferBuilder.vertex((double) f, 0.0D, 0.0D).texture(h, i).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(0.0D, 0.0D, 0.0D).texture(0, i).color(255, 255, 255, 255).next();
		bufferBuilder.end();
		BufferRenderer.postDraw(bufferBuilder);
		shader.disable();
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
	}
	
	private final long startMillis = System.currentTimeMillis();
	
	public void finishFrame() {
		swap.setClearColor(0, 0, 0, 0);
		swap.clear(MinecraftClient.IS_SYSTEM_MAC);
		dst.setClearColor(0, 0, 0, 0);
		dst.clear(MinecraftClient.IS_SYSTEM_MAC);

//		FBOBinder binder = new FBOBinder();
		if (blur != null) {
			Matrix4f mat = new Matrix4f();
			mat.loadIdentity();
			blur.setProjectionMatrix(mat);
			blit.setProjectionMatrix(mat);
//			blit.render(0);
			
			try {
				writeBuffer(main, "fbo");
				
				swap.beginWrite(true);
				blur.render(0);
				swap.beginWrite(true);
				blur.getProgram().enable();
				drawBuffer(blur, main, swap.textureWidth, swap.textureHeight, true);
				blur.getProgram().disable();
				swap.endWrite();
				
				writeBuffer(swap, "swap");
				
				dst.beginWrite(true);
				blit.render(0);
				dst.beginWrite(true);
				blit.getProgram().enable();
				drawBuffer(blit, swap, dst.textureWidth, dst.textureHeight, true);
				blit.getProgram().disable();
				dst.endWrite();
				
				dst.copyDepthFrom(main);
				
				writeBuffer(dst, "flip");
				
				distort.getProgram().getUniformByNameOrDummy("timeMillis").set(startMillis - System.currentTimeMillis());
				Vec3d vec = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();
				distort.getProgram().getUniformByNameOrDummy("camPos").set((float) vec.x, (float) vec.y, (float) vec.z);
//				Matrix4f projMat = RenderSystem.getProjectionMatrix().copy();
				Matrix4f mdlMat = RenderSystem.getProjectionMatrix().copy();
//				projMat.invert();
				mdlMat.invert();
//				mdlMat.multiply(projMat);
				distort.getProgram().getUniformByNameOrDummy("invProj").set(mdlMat);
				if (distort != null) {
					if (!new PostProcessingUtils().checkAuxTarget(distort, "DistortionSampler"))
						distort.addAuxTarget("DistortionSampler", dst::getColorAttachment, dst.textureWidth, dst.textureHeight);
					if (!new PostProcessingUtils().checkAuxTarget(distort, "DistortionDepthSampler"))
						distort.addAuxTarget("DistortionDepthSampler", main::getDepthAttachment, dst.textureWidth, dst.textureHeight);
				}
			} catch (Throwable ignored) {
			}
		}
		MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
//		binder.rebind();
	}
	
	private void writeBuffer(Framebuffer fbo, String path) throws IOException {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			File fboFile = new File(outputPath + "/" + path + "_color.png");
			if (!fboFile.exists()) {
				fboFile.getAbsoluteFile().getParentFile().mkdirs();
				fboFile.createNewFile();
//				NativeImage img = ScreenshotRecorder.takeScreenshot(fbo);
				NativeImage img;
				{
					int i = fbo.textureWidth;
					int j = fbo.textureHeight;
					NativeImage nativeImage = new NativeImage(i, j, false);
					RenderSystem.bindTexture(fbo.getColorAttachment());
					nativeImage.loadFromTextureImage(0, true);
					nativeImage.mirrorVertically();
					img = nativeImage;
				}
				img.writeTo(fboFile);
				img.close();
			}
			
			fboFile = new File(outputPath + "/" + path + "_depth.png");
			if (!fboFile.exists()) {
				fboFile.getAbsoluteFile().getParentFile().mkdirs();
				fboFile.createNewFile();
//				NativeImage img = ScreenshotRecorder.takeScreenshot(fbo);
				NativeImage img;
				{
					int i = fbo.textureWidth;
					int j = fbo.textureHeight;
					NativeImage nativeImage = new NativeImage(i, j, false);
					RenderSystem.bindTexture(fbo.getDepthAttachment());
					nativeImage.loadFromTextureImage(0, true);
					nativeImage.mirrorVertically();
					img = nativeImage;
				}
				img.writeTo(fboFile);
				img.close();
			}
		}
	}
	
	private final Identifier blurName;
	private final Identifier blitName;
	private final Identifier distortionName;
	private final String outputPath;
	
	public DistortionBuffer(Identifier blurName, Identifier blitName, Identifier distortionName, String outputPath) {
		this.blurName = blurName;
		this.blitName = blitName;
		this.distortionName = distortionName;
		// in dev envro, a png file will be written in this path
		// make this null to prevent it from being written
		this.outputPath = outputPath;
	}
	
	public void reloadResources(ResourceManager manager) throws IOException {
		if (blur != null) {
			blur.close();
			blit.close();
		}
		blur = new PostProcessShader(manager, blurName.toString(), main, swap);
		blit = new PostProcessShader(manager, blitName.toString(), swap, dst);
	}
	
	FBOBinder bound;
	
	public void bind() {
		bound = new FBOBinder();
		main.beginWrite(true);
	}
	
	public void finish(Optional<VertexConsumerProvider> vertexConsumers) {
		vertexConsumers.ifPresent((consumers) -> consumers.getBuffer(RenderLayer.getSolid())); // force render
		main.endWrite();
		bound.rebind();
		// TODO: make this not needed
		MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
	}
	
	public void addPasses() {
		if (!PostProcessingUtils.hasPass(distortionName)) {
			distort = PostProcessingUtils.addPass(distortionName, distortionName);
			PostProcessingUtils.addPass(blitName, blitName); // suboptimal, but easy to setup
		}
	}
}

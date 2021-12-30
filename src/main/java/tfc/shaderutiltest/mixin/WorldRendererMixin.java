package tfc.shaderutiltest.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.shaderutiltest.client.Renderer;

// shader related stuff only really needs to be done if a world is present, so to avoid unwanted crashes, mixin to world renderer
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(at = @At("HEAD"), method = "render")
	public void preRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		Renderer.resetFBOs();
	}
	
	@Inject(at = @At("RETURN"), method = "render")
	public void postRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		Renderer.finishFrame();
	}
}

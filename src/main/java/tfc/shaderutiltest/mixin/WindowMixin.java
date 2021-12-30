package tfc.shaderutiltest.mixin;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.shaderutiltest.client.Renderer;

// matches the size of the distortion buffers to the window buffer
@Mixin(Window.class)
public class WindowMixin {
	@Shadow private int framebufferWidth;
	
	@Shadow private int framebufferHeight;
	
	@Inject(at = @At("HEAD"), method = "onFramebufferSizeChanged")
	public void preUpdateFBOSize(CallbackInfo ci) {
		Renderer.updateFBOSize(framebufferWidth, framebufferHeight);
	}
}

package tfc.shaderutiltest.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.shaderutiltest.client.Renderer;

import java.io.IOException;

// handles resource loading
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(at = @At("TAIL"), method = "loadShaders")
	public void postLoadShaders(ResourceManager manager, CallbackInfo ci) throws IOException {
		try {
			Renderer.reloadResources(manager);
		} catch (IOException err) {
			if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
				err.printStackTrace();
				Runtime.getRuntime().exit(-1);
			}
			throw err;
		}
	}
}

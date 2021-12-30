package tfc.shaderutiltest.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import tfc.shaderutiltest.Shaderutiltest;
import tfc.shaderutiltest.tile.TestTile;

@Environment(EnvType.CLIENT)
public class ShaderutiltestClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(Shaderutiltest.TILE, (BlockEntityRendererFactory.Context rendererDispatcherIn) -> new Renderer());
	}
}

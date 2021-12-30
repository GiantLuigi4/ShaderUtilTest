package tfc.shaderutiltest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import tfc.shaderutiltest.tile.TestBlock;
import tfc.shaderutiltest.tile.TestTile;

public class Shaderutiltest implements ModInitializer {
	public static Block BLOCK = new TestBlock(AbstractBlock.Settings.copy(Blocks.STONE));
	
	static {
		BLOCK = Registry.register(Registry.BLOCK, new Identifier("shaderutiltest:testblock"), BLOCK);
	}
	
	public static BlockEntityType<?> TILE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier("shaderutiltest:tile"),
			FabricBlockEntityTypeBuilder.create(TestTile::new, BLOCK).build()
	);
	
	@Override
	public void onInitialize() {
		System.out.println(BLOCK);
		System.out.println(TILE);
	}
}

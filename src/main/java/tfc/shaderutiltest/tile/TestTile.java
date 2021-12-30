package tfc.shaderutiltest.tile;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import tfc.shaderutiltest.Shaderutiltest;

public class TestTile extends BlockEntity {
	public TestTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	public TestTile(BlockPos pos, BlockState state) {
		this(Shaderutiltest.TILE, pos, state);
	}
}

package tfc.shaderutiltest.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TestBlock extends BlockWithEntity {
	public TestBlock(Settings settings) {
		super(settings);
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TestTile(pos, state);
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return null;
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> GameEventListener getGameEventListener(World world, T blockEntity) {
		return null;
	}
	
	public static final EnumProperty<EnumShader> SHADER = EnumProperty.of("type", EnumShader.class);
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(SHADER);
	}
}

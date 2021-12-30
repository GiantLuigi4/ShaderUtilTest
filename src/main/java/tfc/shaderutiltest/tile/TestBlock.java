package tfc.shaderutiltest.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

public class TestBlock extends BlockWithEntity {
	public TestBlock(Settings settings) {
		super(settings);
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TestTile(pos, state);
	}
	
	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		super.onBlockAdded(state, world, pos, oldState, notify);
//		System.out.println("test");
		world.getBlockTickScheduler().schedule(pos, state.getBlock(), 10);
	}
	
	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		super.scheduledTick(state, world, pos, random);
//		System.out.println("test");
		if (world.isClient) {
			boolean bl = world.getBlockEntity(pos) instanceof TestTile;
			System.out.println(bl);
		}
		world.getBlockTickScheduler().schedule(pos, state.getBlock(), 10);
	}
	
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		super.randomDisplayTick(state, world, pos, random);
		if (world.isClient) {
			boolean bl = world.getBlockEntity(pos) instanceof TestTile;
//			System.out.println(bl);
		}
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
	
	public static final EnumProperty<EnumDistortion> SHADER = EnumProperty.of("type", EnumDistortion.class);
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(SHADER);
	}
}

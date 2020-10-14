package net.ludocrypt.truerooms.blocks;

import java.util.Random;
import java.util.stream.IntStream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;

public class StairBlock extends CamoBlock implements Waterloggable {

	public static final DirectionProperty FACING;
	public static final EnumProperty<BlockHalf> HALF;
	public static final EnumProperty<StairShape> SHAPE;
	public static final BooleanProperty WATERLOGGED;
	public static final VoxelShape TOP_SHAPE;
	public static final VoxelShape BOTTOM_SHAPE;
	public static final VoxelShape BOTTOM_NORTH_WEST_CORNER_SHAPE;
	public static final VoxelShape BOTTOM_SOUTH_WEST_CORNER_SHAPE;
	public static final VoxelShape TOP_NORTH_WEST_CORNER_SHAPE;
	public static final VoxelShape TOP_SOUTH_WEST_CORNER_SHAPE;
	public static final VoxelShape BOTTOM_NORTH_EAST_CORNER_SHAPE;
	public static final VoxelShape BOTTOM_SOUTH_EAST_CORNER_SHAPE;
	public static final VoxelShape TOP_NORTH_EAST_CORNER_SHAPE;
	public static final VoxelShape TOP_SOUTH_EAST_CORNER_SHAPE;
	public static final VoxelShape[] TOP_SHAPES;
	public static final VoxelShape[] BOTTOM_SHAPES;
	private static final int[] SHAPE_INDICES;
	private final Block baseBlock;
	private final BlockState baseBlockState;

	private static VoxelShape[] composeShapes(VoxelShape base, VoxelShape northWest, VoxelShape northEast,
			VoxelShape southWest, VoxelShape southEast) {
		return (VoxelShape[]) IntStream.range(0, 16).mapToObj((i) -> {
			return composeShape(i, base, northWest, northEast, southWest, southEast);
		}).toArray((i) -> {
			return new VoxelShape[i];
		});
	}

	private static VoxelShape composeShape(int i, VoxelShape base, VoxelShape northWest, VoxelShape northEast,
			VoxelShape southWest, VoxelShape southEast) {
		VoxelShape voxelShape = base;
		if ((i & 1) != 0) {
			voxelShape = VoxelShapes.union(base, northWest);
		}

		if ((i & 2) != 0) {
			voxelShape = VoxelShapes.union(voxelShape, northEast);
		}

		if ((i & 4) != 0) {
			voxelShape = VoxelShapes.union(voxelShape, southWest);
		}

		if ((i & 8) != 0) {
			voxelShape = VoxelShapes.union(voxelShape, southEast);
		}

		return voxelShape;
	}

	public StairBlock(BlockState baseBlockState) {
		super(FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).hardness(1).resistance(2));
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateManager
				.getDefaultState()).with(FACING, Direction.NORTH)).with(HALF, BlockHalf.BOTTOM)).with(SHAPE,
						StairShape.STRAIGHT)).with(WATERLOGGED, false));
		this.baseBlock = baseBlockState.getBlock();
		this.baseBlockState = baseBlockState;
	}

	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return (state.get(HALF) == BlockHalf.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_INDICES[this
				.getShapeIndexIndex(state)]];
	}

	private int getShapeIndexIndex(BlockState state) {
		return ((StairShape) state.get(SHAPE)).ordinal() * 4 + ((Direction) state.get(FACING)).getHorizontal();
	}

	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		this.baseBlock.randomDisplayTick(state, world, pos, random);
	}

	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		this.baseBlockState.onBlockBreakStart(world, pos, player);
	}

	public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
		this.baseBlock.onBroken(world, pos, state);
	}

	public float getBlastResistance() {
		return this.baseBlock.getBlastResistance();
	}

	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!state.isOf(state.getBlock())) {
			this.baseBlockState.neighborUpdate(world, pos, Blocks.AIR, pos, false);
			this.baseBlock.onBlockAdded(this.baseBlockState, world, pos, oldState, false);
		}
		super.onBlockAdded(state, world, pos, oldState, notify);
	}

	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			this.baseBlockState.onStateReplaced(world, pos, newState, moved);
		}
	}

	public void onSteppedOn(World world, BlockPos pos, Entity entity) {
		this.baseBlock.onSteppedOn(world, pos, entity);
	}

	public boolean hasRandomTicks(BlockState state) {
		return this.baseBlock.hasRandomTicks(state);
	}

	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		this.baseBlock.randomTick(state, world, pos, random);
	}

	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		this.baseBlock.scheduledTick(state, world, pos, random);
	}

	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		return this.baseBlockState.onUse(world, player, hand, hit);
	}

	public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
		this.baseBlock.onDestroyedByExplosion(world, pos, explosion);
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction direction = ctx.getSide();
		BlockPos blockPos = ctx.getBlockPos();
		FluidState fluidState = ctx.getWorld().getFluidState(blockPos);
		BlockState blockState = (BlockState) ((BlockState) ((BlockState) this.getDefaultState().with(FACING,
				ctx.getPlayerFacing())).with(
						HALF,
						direction != Direction.DOWN
								&& (direction == Direction.UP || ctx.getHitPos().y - (double) blockPos.getY() <= 0.5D)
										? BlockHalf.BOTTOM
										: BlockHalf.TOP)).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		return (BlockState) blockState.with(SHAPE, getStairShape(blockState, ctx.getWorld(), blockPos));
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
			WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if ((Boolean) state.get(WATERLOGGED)) {
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return direction.getAxis().isHorizontal() ? (BlockState) state.with(SHAPE, getStairShape(state, world, pos))
				: super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	private static StairShape getStairShape(BlockState state, BlockView world, BlockPos pos) {
		Direction direction = (Direction) state.get(FACING);
		BlockState blockState = world.getBlockState(pos.offset(direction));
		if (isStairs(blockState) && state.get(HALF) == blockState.get(HALF)) {
			Direction direction2 = (Direction) blockState.get(FACING);
			if (direction2.getAxis() != ((Direction) state.get(FACING)).getAxis()
					&& method_10678(state, world, pos, direction2.getOpposite())) {
				if (direction2 == direction.rotateYCounterclockwise()) {
					return StairShape.OUTER_LEFT;
				}

				return StairShape.OUTER_RIGHT;
			}
		}

		BlockState blockState2 = world.getBlockState(pos.offset(direction.getOpposite()));
		if (isStairs(blockState2) && state.get(HALF) == blockState2.get(HALF)) {
			Direction direction3 = (Direction) blockState2.get(FACING);
			if (direction3.getAxis() != ((Direction) state.get(FACING)).getAxis()
					&& method_10678(state, world, pos, direction3)) {
				if (direction3 == direction.rotateYCounterclockwise()) {
					return StairShape.INNER_LEFT;
				}

				return StairShape.INNER_RIGHT;
			}
		}

		return StairShape.STRAIGHT;
	}

	private static boolean method_10678(BlockState state, BlockView world, BlockPos pos, Direction dir) {
		BlockState blockState = world.getBlockState(pos.offset(dir));
		return !isStairs(blockState) || blockState.get(FACING) != state.get(FACING)
				|| blockState.get(HALF) != state.get(HALF);
	}

	public static boolean isStairs(BlockState state) {
		return state.getBlock() instanceof StairsBlock;
	}

	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	@SuppressWarnings("incomplete-switch")
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		Direction direction = (Direction) state.get(FACING);
		StairShape stairShape = (StairShape) state.get(SHAPE);
		switch (mirror) {
		case LEFT_RIGHT:
			if (direction.getAxis() == Direction.Axis.Z) {
				switch (stairShape) {
				case INNER_LEFT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
				case INNER_RIGHT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
				case OUTER_LEFT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
				case OUTER_RIGHT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
				default:
					return state.rotate(BlockRotation.CLOCKWISE_180);
				}
			}
			break;
		case FRONT_BACK:
			if (direction.getAxis() == Direction.Axis.X) {
				switch (stairShape) {
				case INNER_LEFT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
				case INNER_RIGHT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
				case OUTER_LEFT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
				case OUTER_RIGHT:
					return (BlockState) state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
				case STRAIGHT:
					return state.rotate(BlockRotation.CLOCKWISE_180);
				}
			}
		}

		return super.mirror(state, mirror);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF, SHAPE, WATERLOGGED);
	}

	public FluidState getFluidState(BlockState state) {
		return (Boolean) state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	static {
		FACING = HorizontalFacingBlock.FACING;
		HALF = Properties.BLOCK_HALF;
		SHAPE = Properties.STAIR_SHAPE;
		WATERLOGGED = Properties.WATERLOGGED;
		TOP_SHAPE = SlabBlock.TOP_SHAPE;
		BOTTOM_SHAPE = SlabBlock.BOTTOM_SHAPE;
		BOTTOM_NORTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 8.0D);
		BOTTOM_SOUTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 8.0D, 8.0D, 8.0D, 16.0D);
		TOP_NORTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 8.0D);
		TOP_SOUTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0D, 8.0D, 8.0D, 8.0D, 16.0D, 16.0D);
		BOTTOM_NORTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);
		BOTTOM_SOUTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
		TOP_NORTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
		TOP_SOUTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
		TOP_SHAPES = composeShapes(TOP_SHAPE, BOTTOM_NORTH_WEST_CORNER_SHAPE, BOTTOM_NORTH_EAST_CORNER_SHAPE,
				BOTTOM_SOUTH_WEST_CORNER_SHAPE, BOTTOM_SOUTH_EAST_CORNER_SHAPE);
		BOTTOM_SHAPES = composeShapes(BOTTOM_SHAPE, TOP_NORTH_WEST_CORNER_SHAPE, TOP_NORTH_EAST_CORNER_SHAPE,
				TOP_SOUTH_WEST_CORNER_SHAPE, TOP_SOUTH_EAST_CORNER_SHAPE);
		SHAPE_INDICES = new int[] { 12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8 };
	}
}

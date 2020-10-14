package net.ludocrypt.truerooms.items;

import net.ludocrypt.truerooms.blocks.CamoBlock;
import net.ludocrypt.truerooms.blocks.entity.CamoBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class StaffOfCamo extends Item {

	public StaffOfCamo(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {

		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		BlockState blockState = world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		ItemStack itemStack = context.getStack();
		Direction dir = context.getSide();

		if (block instanceof CamoBlock) {
			if (world.getBlockEntity(blockPos) instanceof CamoBlockEntity) {
				CamoBlockEntity camoBlockEntity = (CamoBlockEntity) world.getBlockEntity(blockPos);
				if (itemStack.hasTag()) {
					camoBlockEntity.setState(dir, getState(itemStack));
					camoBlockEntity.setDirection(dir, getDirection(itemStack));
				} else {
					BlockState stateAdjacent = camoBlockEntity.getState(dir);
					putStateAndDirection(itemStack, stateAdjacent, dir);
				}
				return ActionResult.SUCCESS;
			} else {
				return ActionResult.FAIL;
			}
		} else if (block != Blocks.AIR && blockState.isFullCube(world, blockPos)) {
			putStateAndDirection(itemStack, blockState, dir);
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.FAIL;
		}
	}

	private static void putStateAndDirection(ItemStack itemStack, BlockState state, Direction dir) {
		CompoundTag tag = itemStack.getOrCreateTag();
		tag.put("setState", NbtHelper.fromBlockState(state));
		tag.putString("setDirection", dir.name().toLowerCase());
	}

	private static BlockState getState(ItemStack itemStack) {
		return NbtHelper.toBlockState(itemStack.getOrCreateSubTag("setState"));
	}

	private static Direction getDirection(ItemStack itemStack) {

		CompoundTag tag = itemStack.getOrCreateTag();
		Direction tempDir = Direction.NORTH;

		if (tag.contains("setDirection", 8)) {
			tempDir = Direction.byName(tag.getString("setDirection"));
		}

		return tempDir;
	}

}

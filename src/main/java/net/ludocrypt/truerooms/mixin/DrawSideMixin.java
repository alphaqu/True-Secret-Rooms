package net.ludocrypt.truerooms.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ludocrypt.truerooms.blocks.CamoBlock;
import net.ludocrypt.truerooms.blocks.DoorBlock;
import net.ludocrypt.truerooms.blocks.HingeGateBlock;
import net.ludocrypt.truerooms.blocks.OneWayGlassBlock;
import net.ludocrypt.truerooms.blocks.TrapdoorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
@Mixin(Block.class)
public class DrawSideMixin {

	@Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
	private static void shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction facing,
			CallbackInfoReturnable<Boolean> ci) {
		BlockPos blockPos = pos.offset(facing);
		BlockState blockState = world.getBlockState(blockPos);

		if ((state.getBlock() instanceof CamoBlock)) {
			if (((blockState.getBlock() instanceof CamoBlock) && !(blockState.getBlock() instanceof DoorBlock)
					&& !(blockState.getBlock() instanceof TrapdoorBlock)
					&& !(blockState.getBlock() instanceof HingeGateBlock)
					&& !(blockState.getBlock() instanceof OneWayGlassBlock))) {
				ci.setReturnValue(false);
			} else if ((blockState.getBlock() instanceof DoorBlock || blockState.getBlock() instanceof TrapdoorBlock
					|| blockState.getBlock() instanceof HingeGateBlock)
					|| (blockState.getBlock() instanceof OneWayGlassBlock
							&& !(state.getBlock() instanceof OneWayGlassBlock))) {
				ci.setReturnValue(true);
			}
		} else {
			if ((blockState.getBlock() instanceof CamoBlock)) {
				ci.setReturnValue(false);
			}
		}

	}

}

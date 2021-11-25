/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.block.component;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IMergedBlock;
import net.malisis.core.block.ISmartCull;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * A WallComponent when applied to a {@link MalisisBlock} makes it behave like walls.<br>
 * Walls have a 3/16 thickness and can be made into a corner when placing a second wall into the same block.<br>
 * A {@link DirectionalComponent} is automatically added to the block.
 *
 * @author Ordinastie
 */
public class WallComponent implements IBlockComponent, IMergedBlock, ISmartCull
{
	public static BooleanProperty CORNER = BooleanProperty.create("corner");

	/**
	 * Gets the property to use for this {@link WallComponent}.
	 *
	 * @return the property
	 */
	@Override
	public BooleanProperty getProperty()
	{
		return CORNER;
	}

	/**
	 * Sets the default value to use for this {@link WallComponent}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the i block state
	 */
	@Override
	public BlockState setDefaultState(Block block, BlockState state)
	{
		return state.withProperty(getProperty(), false);
	}

	/**
	 * Gets the dependencies needed for this {@link WallComponent}.
	 *
	 * @return the dependencies
	 */
	@Override
	public List<IComponent> getDependencies()
	{
		return Lists.newArrayList(new DirectionalComponent());
	}

	/**
	 * Checks whether the block can be merged into a corner.
	 *
	 * @param itemStack the item stack
	 * @param player the player
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @return true, if successful
	 */
	@Override
	public boolean canMerge(ItemStack itemStack, PlayerEntity player, World world, BlockPos pos, Direction side)
	{
		BlockState state = world.getBlockState(pos);
		if (isCorner(state))
			return false;

		return EnumFacingUtils.getRealSide(state, side) != Direction.NORTH;
	}

	/**
	 * Merges the {@link BlockState} into a corner if possible.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param itemStack the item stack
	 * @param player the player
	 * @param side the side
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @return the i block state
	 */
	@Override
	public BlockState mergeBlock(World world, BlockPos pos, BlockState state, ItemStack itemStack, PlayerEntity player, Direction side, float hitX, float hitY, float hitZ)
	{
		Direction direction = DirectionalComponent.getDirection(state);
		Direction realSide = EnumFacingUtils.getRealSide(state, side);

		if (realSide == Direction.EAST && hitX == 1)
			return null;
		if (realSide == Direction.WEST && hitX == 0)
			return null;
		if (realSide == Direction.UP && hitY == 1)
			return null;
		if (realSide == Direction.DOWN && hitX == 0)
			return null;

		boolean rotate = false;
		switch (direction)
		{
			case SOUTH:
				rotate = hitX < 0.5F;
				break;
			case NORTH:
				rotate = hitX >= 0.5F;
				break;
			case WEST:
				rotate = hitZ < 0.5F;
				break;
			case EAST:
				rotate = hitZ >= 0.5F;
				break;
			default:
				break;
		}

		if (rotate)
			state = DirectionalComponent.rotate(state);

		return state.withProperty(getProperty(), true);
	}

	/**
	 * Gets the {@link BlockState} from <code>meta</code>.
	 *
	 * @param block the block
	 * @param state the state
	 * @param meta the meta
	 * @return the state from meta
	 */
	@Override
	public BlockState getStateFromMeta(Block block, BlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0);
	}

	/**
	 * Gets the metadata from the {@link BlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	@Override
	public int getMetaFromState(Block block, BlockState state)
	{
		return isCorner(state) ? 8 : 0;
	}

	/**
	 * Gets the bounding boxes for the block.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding boxes
	 */
	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		boolean corner = isCorner(state);
		if (type == BoundingBoxType.SELECTION && corner)
			return AABBUtils.identities();

		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 1, 3 / 16F);
		if (world == null)
			aabb = AABBUtils.rotate(aabb.offset(0, 0, 0.5F), -1);

		if (!corner)
			return new AxisAlignedBB[] { aabb };

		return new AxisAlignedBB[] { aabb, AABBUtils.rotate(aabb, -1) };
	}

	@Override
	public Boolean isOpaqueCube(Block block, BlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullBlock(Block block, BlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullCube(Block block, BlockState state)
	{
		return false;
	}

	/**
	 * Quantity the quantity dropped by the {@link Block} when broken.
	 *
	 * @param block the block
	 * @param state the state
	 * @param fortune the fortune
	 * @param random the random
	 * @return the integer
	 */
	@Override
	public Integer quantityDropped(Block block, BlockState state, int fortune, Random random)
	{
		return isCorner(state) ? 2 : 1;
	}

	/**
	 * Gets whether the wall is a corner or not.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the Direction, null if the block is not {@link DirectionalComponent}
	 */
	public static boolean isCorner(IBlockReader world, BlockPos pos)
	{
		return world != null && pos != null ? isCorner(world.getBlockState(pos)) : false;
	}

	/**
	 * Gets whether the wall is a corner or not.
	 *
	 * @param state the state
	 * @return the Direction, null if the block is not {@link DirectionalComponent}
	 */
	public static boolean isCorner(BlockState state)
	{
		WallComponent wc = IComponent.getComponent(WallComponent.class, state.getBlock());
		if (wc == null)
			return false;

		BooleanProperty property = wc.getProperty();
		if (property == null || !state.getProperties().contains(property))
			return false;

		return state.getValue(property);
	}

}

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

import com.google.common.collect.Lists;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.DirectionalComponent.IPlacement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class LadderComponent implements IBlockComponent
{
	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, 1, 0.125f);

	@Override
	public Property<?> getProperty()
	{
		return null;
	}

	@Override
	public BlockState setDefaultState(Block block, BlockState state)
	{
		return state;
	}

	@Override
	public List<IComponent> getDependencies()
	{
		return Lists.newArrayList(new DirectionalComponent(IPlacement.BLOCKSIDE));
	}

	protected boolean canBlockStay(World world, BlockPos pos, Direction side)
	{
		return world.isSideSolid(pos.offset(side.getOpposite()), side, true);
	}

	@Override
	public void onNeighborBlockChange(Block block, World world, BlockPos pos, BlockState state, Block neighborBlock, BlockPos neighborPos)
	{
		Direction dir = DirectionalComponent.getDirection(world, pos);

		if (!canBlockStay(world, pos, dir))
		{
			block.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(Block block, IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		return BOUNDING_BOX;
	}

	@Override
	public boolean canPlaceBlockOnSide(Block block, World world, BlockPos pos, Direction side)
	{
		if (side == Direction.UP || side == Direction.DOWN)
			return false;
		return world.isSideSolid(pos.offset(side.getOpposite()), side, true);
	}

	@Override
	public boolean canPlaceBlockAt(Block block, World world, BlockPos pos)
	{
		return world.isSideSolid(pos.west(), Direction.EAST, true) || world.isSideSolid(pos.east(), Direction.WEST, true)
				|| world.isSideSolid(pos.north(), Direction.SOUTH, true) || world.isSideSolid(pos.south(), Direction.NORTH, true);
	}

	@Override
	public Boolean isOpaqueCube(Block block, BlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullCube(Block block, BlockState state)
	{
		return false;
	}
}

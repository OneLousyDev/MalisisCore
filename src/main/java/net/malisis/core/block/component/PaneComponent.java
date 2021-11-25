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
import net.malisis.core.block.ISmartCull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * @author Ordinastie
 *
 */
public class PaneComponent implements IBlockComponent, ISmartCull
{
	public static final BooleanProperty NORTH = PaneBlock.NORTH;
	public static final BooleanProperty EAST = PaneBlock.EAST;
	public static final BooleanProperty SOUTH = PaneBlock.SOUTH;
	public static final BooleanProperty WEST = PaneBlock.WEST;

	@Override
	public Property<?> getProperty()
	{
		return null;
	}

	@Override
	public Property<?>[] getProperties()
	{
		return new Property[] { NORTH, EAST, SOUTH, WEST };
	}

	@Override
	public BlockState setDefaultState(Block block, BlockState state)
	{
		return state.withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false);
	}

	public BlockState getFullState(Block block, IBlockReader world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() != block)
			return block.defaultBlockState();
		return state.withProperty(NORTH, canPaneConnectTo(block, world, pos, Direction.NORTH))
					.withProperty(SOUTH, canPaneConnectTo(block, world, pos, Direction.SOUTH))
					.withProperty(WEST, canPaneConnectTo(block, world, pos, Direction.WEST))
					.withProperty(EAST, canPaneConnectTo(block, world, pos, Direction.EAST));
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		float f = 0.4375F;
		AxisAlignedBB base = new AxisAlignedBB(f, 0, f, 1 - f, 1, 1 - f);
		state = world != null ? getFullState(block, world, pos) : block.defaultBlockState();
		boolean north = state.getValue(NORTH);
		boolean south = state.getValue(SOUTH);
		boolean east = state.getValue(EAST);
		boolean west = state.getValue(WEST);

		if (world == null)
		{
			north = true;
			south = true;
		}

		if (!north && !south && !east && !west)
			return new AxisAlignedBB[] { base };

		List<AxisAlignedBB> list = Lists.newArrayList();
		if (north || south)
			list.add(base.expand(0, 0, north ? -f : 0).expand(0, 0, south ? f : 0));
		if (east || west)
			list.add(base.expand(west ? -f : 0, 0, 0).expand(east ? f : 0, 0, 0));

		return list.toArray(new AxisAlignedBB[0]);
	}

	@Override
	public Boolean isFullCube(Block block, BlockState state)
	{
		return false;
	}

	@Override
	public Boolean isOpaqueCube(Block block, BlockState state)
	{
		return false;
	}

	public final boolean canPaneConnectToBlock(Block block, BlockState state)
	{
		return state.isFullBlock() || block == Blocks.GLASS || block == Blocks.STAINED_GLASS || block == Blocks.STAINED_GLASS_PANE
				|| block instanceof PaneBlock;
	}

	public boolean canPaneConnectTo(Block block, IBlockReader world, BlockPos pos, Direction dir)
	{
		BlockPos offset = pos.offset(dir);
		BlockState state = world.getBlockState(offset);
		Block connected = state.getBlock();
		BlockFaceShape shape = state.getBlockFaceShape(world, pos, dir);
		return connected == block || canPaneConnectToBlock(state.getBlock(), state) || shape == BlockFaceShape.SOLID
				|| shape == BlockFaceShape.MIDDLE_POLE_THIN;
	}

	public static boolean isConnected(BlockState state, BooleanProperty property)
	{
		return state.getValue(property);
	}
}

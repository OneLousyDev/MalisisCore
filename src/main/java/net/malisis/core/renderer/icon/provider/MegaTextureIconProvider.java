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

package net.malisis.core.renderer.icon.provider;

import static net.minecraft.util.Direction.*;

import java.util.HashMap;

import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.util.MBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * @author Ordinastie
 *
 */
public class MegaTextureIconProvider extends SidesIconProvider
{
	private static HashMap<Direction, Direction[]> searchDirs = new HashMap<>();
	static
	{
		searchDirs.put(NORTH, new Direction[] { DOWN, EAST });
		searchDirs.put(SOUTH, new Direction[] { DOWN, WEST });
		searchDirs.put(EAST, new Direction[] { DOWN, SOUTH });
		searchDirs.put(WEST, new Direction[] { DOWN, NORTH });
		searchDirs.put(UP, new Direction[] { SOUTH, WEST });
		searchDirs.put(DOWN, new Direction[] { SOUTH, WEST });
	}

	private int[] numBlocks = new int[6];

	public MegaTextureIconProvider(Icon defaultIcon)
	{
		super(defaultIcon);
	}

	public void setMegaTexture(Direction side, Icon icon)
	{
		setMegaTexture(side, icon, -1);
	}

	public void setMegaTexture(Direction side, Icon icon, int numBlocks)
	{
		setSideIcon(side, icon);
		this.numBlocks[side.ordinal()] = numBlocks;
	}

	@Override
	public void setSideIcon(Direction side, Icon icon)
	{
		super.setSideIcon(side, icon);
		numBlocks[side.ordinal()] = 0;
	}

	private boolean isMegaTexture(Direction side)
	{
		return numBlocks[side.ordinal()] != 0;
	}

	private int getNumBlocks(Icon icon, Direction side)
	{
		if (this.numBlocks[side.ordinal()] == -1)
		{
			int w = icon.getIconWidth();
			this.numBlocks[side.ordinal()] = w / 16;
		}

		return this.numBlocks[side.ordinal()];
	}

	@Override
	public Icon getIcon(IBlockReader world, BlockPos pos, BlockState state, Direction side)
	{
		Icon icon = super.getIcon(world, pos, state, side);
		Direction blockDir = side;
		if (IComponent.getComponent(DirectionalComponent.class, state.getBlock()) != null)
			blockDir = DirectionalComponent.getDirection(state);
		int numBlocks = getNumBlocks(icon, side);
		if (!isMegaTexture(side))
			return icon;

		MBlockState baseState = getBaseState(world, new MBlockState(pos, state), blockDir);
		if (baseState == null)
			return icon;

		return getIconPart(icon, pos, baseState, blockDir, numBlocks);
	}

	private MBlockState getBaseState(IBlockReader world, MBlockState state, Direction side)
	{
		MBlockState baseState = state;
		MBlockState lastState = state;
		Direction[] dirs = searchDirs.get(side);
		if (dirs == null)
			return null;

		for (Direction dir : dirs)
		{
			lastState = baseState;
			while (lastState.getBlock() == state.getBlock()
					&& DirectionalComponent.getDirection(lastState.getBlockState()) == DirectionalComponent.getDirection(state.getBlockState()))
			{
				baseState = lastState;
				try
				{
					lastState = new MBlockState(world, baseState.getPos().offset(dir));
				}
				catch (Exception e)
				{
					break;
				}
			}
		}

		return baseState;
	}

	private Icon getIconPart(Icon icon, BlockPos pos, MBlockState state, Direction side, int numBlocks)
	{
		int u = 0;
		int v = ((pos.getY() - state.getY()) % numBlocks) + 1;
		if (side == NORTH || side == SOUTH)
			u = Math.abs(pos.getX() - state.getX()) % numBlocks;
		else if (side == EAST || side == WEST)
			u = Math.abs(pos.getZ() - state.getZ()) % numBlocks;
		else
		{
			u = Math.abs(pos.getX() - state.getX()) % numBlocks;
			v = (Math.abs(pos.getZ() - state.getZ()) % numBlocks) + 1;
		}

		float factor = 1.0F / numBlocks;
		Icon copy = new Icon();
		copy.copyFrom(icon);
		copy.clip(u * factor, 1 - v * factor, factor, factor);
		return copy;
	}
}

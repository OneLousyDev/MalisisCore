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

package net.malisis.core.util;

import net.malisis.core.block.component.DirectionalComponent;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

/**
 * @author Ordinastie
 *
 */
public class EnumFacingUtils
{
	/**
	 * Gets the rotation count for the facing.
	 *
	 * @param facing the facing
	 * @return the rotation count
	 */
	public static int getRotationCount(Direction facing)
	{
		if (facing == null)
			return 0;

		switch (facing)
		{
			case EAST:
				return 1;
			case NORTH:
				return 2;
			case WEST:
				return 3;
			case SOUTH:
			default:
				return 0;
		}
	}

	/**
	 * Gets the rotation count for the {@link IBlockState}
	 *
	 * @param state the state
	 * @return the rotation count
	 */
	public static int getRotationCount(BlockState state)
	{
		Direction direction = DirectionalComponent.getDirection(state);
		return EnumFacingUtils.getRotationCount(direction);
	}

	/**
	 * Rotates facing {@code count} times.
	 *
	 * @param facing the facing
	 * @param count the count
	 * @return the enum facing
	 */
	public static Direction rotateFacing(Direction facing, int count)
	{
		if (facing == null)
			return null;

		while (count-- > 0)
			facing = facing.rotateAround(Direction.Axis.Y);
		return facing;
	}

	/**
	 * Gets the real side of a rotated block.
	 *
	 * @param state the state
	 * @param side the side
	 * @return the real side
	 */
	public static Direction getRealSide(BlockState state, Direction side)
	{
		if (state == null || side == null)
			return side;

		Direction direction = DirectionalComponent.getDirection(state);
		if (direction == Direction.SOUTH)
			return side;

		if (direction == Direction.DOWN)
			return side.rotateAround(Axis.X);
		else if (direction == Direction.UP)
			switch (side)
			{
				case UP:
					return Direction.SOUTH;
				case DOWN:
					return Direction.NORTH;
				case NORTH:
					return Direction.UP;
				case SOUTH:
					return Direction.DOWN;
				default:
					return side;
			}

		int count = EnumFacingUtils.getRotationCount(direction);
		side = EnumFacingUtils.rotateFacing(side, count);

		return side;
	}
}

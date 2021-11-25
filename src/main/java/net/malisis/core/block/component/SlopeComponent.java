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

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.renderer.component.SlopeShapeComponent;
import net.malisis.core.util.AABBUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class SlopeComponent implements IBlockComponent
{
	private static final float[][] FX = new float[][] {
		{ 0f, 1f },
		{ 0f, 1f }
	};
	private static final float[][] FY = new float[][] {
		{ 0f, 1f },
		{ 0f, 1f }
	};
	private static final AxisAlignedBB[] BOUNDING_BOXES = AABBUtils.slice(
		8,
		FX,
		FY,
		new float[][] {
			{ 0f, 1f },
			{ 0f, 0f }
		},
		true
	);
	private static final AxisAlignedBB[] BOUNDING_BOXES_DOWN = AABBUtils.slice(
		8,
		FX,
		FY,
		new float[][] {
			{ 0f, 1f / 8f },
			{ 0f, 9f / 8f }
		},
		true
	);
	public static BooleanProperty DOWN = BooleanProperty.create("down");

	public SlopeComponent()
	{

	}

	@Override
	public BooleanProperty getProperty()
	{
		return DOWN;
	}

	@Override
	public BlockState setDefaultState(Block block, BlockState state)
	{
		return state.withProperty(getProperty(), false);
	}

	@Override
	public List<IComponent> getDependencies()
	{
		List<IComponent> deps = Lists.newArrayList(new DirectionalComponent());

		if (MalisisCore.isClient())
			deps.add(new SlopeShapeComponent());

		return deps;
	}

	@Override
	public BlockState getStateForPlacement(Block block, World world, BlockPos pos, BlockState state, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand)
	{
		boolean down = facing == Direction.DOWN || (facing != Direction.UP && hitY > 0.5F);
		return state.withProperty(getProperty(), down);
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		boolean down = isDown(state);
		return down ? BOUNDING_BOXES_DOWN : BOUNDING_BOXES;
	}

	@Override
	public int getMetaFromState(Block block, BlockState state)
	{
		return isDown(state) ? 8 : 0;
	}

	@Override
	public BlockState getStateFromMeta(Block block, BlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0);
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

	public static boolean isDown(IBlockReader world, BlockPos pos)
	{
		return world != null && pos != null ? isDown(world.getBlockState(pos)) : false;
	}

	public static boolean isDown(BlockState state)
	{
		SlopeComponent sc = IComponent.getComponent(SlopeComponent.class, state.getBlock());
		if (sc == null)
			return false;

		BooleanProperty property = sc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return state.getValue(property);
	}

}

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

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * A DirectionalComponent, when added to a {@link MalisisBlock} allows the block to automatically have an orientation.<br>
 * The state is handled automatically when the block is placed, as well as the {@link BlockState}<->metadata conversion.
 * {@link #HORIZONTAL} and {@link #ALL} properties are available by default, but the component can be used with any
 * {@link PropertyDirection}.
 *
 * @author Ordinastie
 */
public class DirectionalComponent implements IBlockComponent
{
	public static final PropertyDirection HORIZONTAL = PropertyDirection.create("direction", Direction.Plane.HORIZONTAL);
	public static final PropertyDirection ALL = PropertyDirection.create("direction");

	/** The type of placement to use. */
	private IPlacement placement = IPlacement.PLACER;
	/** The property used for this {@link DirectionalComponent}. */
	private PropertyDirection property = HORIZONTAL;

	/**
	 * Instantiates a new {@link DirectionalComponent}.
	 *
	 * @param property the property
	 * @param placement the placement
	 */
	public DirectionalComponent(PropertyDirection property, IPlacement placement)
	{
		this.property = property;
		this.placement = placement;
	}

	/**
	 * Instantiates a new {@link DirectionalComponent} with {@link #HORIZONTAL} property and {@link IPlacement#PLACER} by default.
	 */
	public DirectionalComponent()
	{
		this(HORIZONTAL, IPlacement.PLACER);
	}

	/**
	 * Instantiates a new {@link DirectionalComponent} with specified property and {@link IPlacement#PLACER} by default.
	 *
	 * @param property the property
	 */
	public DirectionalComponent(PropertyDirection property)
	{
		this(property, IPlacement.PLACER);
	}

	/**
	 * Instantiates a new {@link DirectionalComponent} with specified placement and {@link #HORIZONTAL} property by default.
	 *
	 * @param placement the placement
	 */
	public DirectionalComponent(IPlacement placement)
	{
		this(HORIZONTAL, placement);
	}

	/**
	 * Gets the property direction to use for this {@link DirectionalComponent}.
	 *
	 * @return the property direction
	 */
	@Override
	public PropertyDirection getProperty()
	{
		return property;
	}

	/**
	 * Sets the default value to use for the {@link BlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the i block state
	 */
	@Override
	public BlockState setDefaultState(Block block, BlockState state)
	{
		return state.withProperty(getProperty(), Direction.SOUTH);
	}

	public BlockState placedState(BlockState state, Direction facing, LivingEntity placer)
	{
		return state.withProperty(getProperty(), placement.getPlacement(state, facing, placer));
	}

	/**
	 * Automatically gets the right {@link BlockState} based on the <code>placer</code> facing.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param facing the facing
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @param meta the meta
	 * @param placer the placer
	 * @return the i block state
	 */
	@Override
	public BlockState getStateForPlacement(Block block, World world, BlockPos pos, BlockState state, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, EnumHand hand)
	{
		return placedState(state, facing, placer);
	}

	/**
	 * Gets the {@link BlockState} from <code>meta</code>.
	 *
	 * @param block the block
	 * @param meta the meta
	 * @return the state from meta
	 */
	@Override
	public BlockState getStateFromMeta(Block block, BlockState state, int meta)
	{
		Direction facing = null;
		if (getProperty() == HORIZONTAL)
			facing = Direction.getHorizontal(meta & 3);
		else
			facing = Direction.getFront(meta & 7);
		return state.withProperty(getProperty(), facing);
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
		if (getProperty() == HORIZONTAL)
			return getDirection(state).getHorizontalIndex();
		else
			return getDirection(state).getIndex();
	}

	/**
	 * Gets the {@link Direction direction} for the {@link Block} at world coords.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the Direction, null if the block is not {@link DirectionalComponent}
	 */
	public static Direction getDirection(IBlockReader world, BlockPos pos)
	{
		return world != null && pos != null ? getDirection(world.getBlockState(pos)) : Direction.SOUTH;
	}

	/**
	 * Gets the {@link Direction direction} for the {@link BlockState}
	 *
	 * @param state the state
	 * @return the Direction, null if the block is not {@link DirectionalComponent}
	 */
	public static Direction getDirection(BlockState state)
	{
		DirectionalComponent dc = IComponent.getComponent(DirectionalComponent.class, state.getBlock());
		if (dc == null)
			return Direction.SOUTH;

		PropertyDirection property = dc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return Direction.SOUTH;

		return state.getValue(property);
	}

	/**
	 * Gets the {@link PropertyDirection} used by the block.
	 *
	 * @param block the block
	 * @return the property
	 */
	public static PropertyDirection getProperty(Block block)
	{
		DirectionalComponent dc = IComponent.getComponent(DirectionalComponent.class, block);
		return dc != null ? dc.getProperty() : null;
	}

	/**
	 * Rotates the {@link BlockState} by 90 degrees counter-clockwise.
	 *
	 * @param state the state
	 * @return the i block state
	 */
	public static BlockState rotate(BlockState state)
	{
		return rotate(state, 1);
	}

	/**
	 * Rotates the {@link BlockState} by a factor of 90 degrees counter-clockwise.
	 *
	 * @param state the state
	 * @param angle the angle
	 * @return the i block state
	 */
	public static BlockState rotate(BlockState state, int angle)
	{
		int a = -angle & 3;
		if (a == 0)
			return state;

		PropertyDirection property = DirectionalComponent.getProperty(state.getBlock());
		if (property == null || !state.getProperties().containsKey(property))
			return state;

		return state.withProperty(property, EnumFacingUtils.rotateFacing(state.getValue(property), a));
	}

	public static BlockState getPlacedState(BlockState state, Direction facing, LivingEntity placer)
	{
		DirectionalComponent dc = IComponent.getComponent(DirectionalComponent.class, state.getBlock());
		return dc != null ? dc.placedState(state, facing, placer) : state;
	}

	public static interface IPlacement
	{
		/** Direction is determined by the side of the block clicked */
		public static final IPlacement BLOCKSIDE = (state, side, placer) -> side;
		/** Direction is determined by the facing of the entity placing the block. */
		public static final IPlacement PLACER = (state, side, placer) -> {
			Direction facing = EntityUtils.getEntityFacing(placer, DirectionalComponent.getProperty(state.getBlock()) == ALL);
			return placer.isShiftKeyDown() ? facing : facing.getOpposite();
		};

		public Direction getPlacement(BlockState state, Direction side, LivingEntity placer);
	}
}

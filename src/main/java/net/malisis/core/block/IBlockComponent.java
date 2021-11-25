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

package net.malisis.core.block;

import java.util.Random;

import net.malisis.core.item.MalisisItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public interface IBlockComponent extends IComponent
{
	/**
	 * Gets the {@link IProperty} used by this {@link IBlockComponent}.<br>
	 * Only use this if the component only has one property.
	 *
	 * @return the property
	 */
	public default Property<?> getProperty()
	{
		return null;
	}

	/**
	 * Gets the all the {@link IProperty properties} used by this {@link IBlockComponent}.
	 *
	 * @return the properties
	 */
	public default Property<?>[] getProperties()
	{
		if (getProperty() == null)
			return new Property[0];
		return new Property[] { getProperty() };
	}

	/**
	 * Sets the default values for the {@link BlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 */
	public default BlockState setDefaultState(Block block, BlockState state)
	{
		return state;
	}

	public default Item getItem(Block block)
	{
		if (block instanceof MalisisBlock)
			return new MalisisItemBlock((MalisisBlock) block);

		return new BlockItem(block);
	}

	/**
	 * Gets the unlocalized name for the specific {@link BlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the unlocalized name
	 */
	public default String getUnlocalizedName(Block block, BlockState state)
	{
		return null;
	}

	//#region Events
	/**
	 * Called when the block is added to the {@link World}
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 */
	public default void onBlockAdded(Block block, World world, BlockPos pos, BlockState state)
	{}

	/**
	 * Called when the {@link Block} is placed in the {@link World}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param facing the facing
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @param meta the meta
	 * @param placer the placer
	 * @param hand the hand
	 * @return the i block state
	 */
	public default BlockState getStateForPlacement(Block block, World world, BlockPos pos, BlockState state, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand)
	{
		return state;
	}

	/**
	 * Called when the {@link Block} is placed by an {@link LivingEntity} in the {@link World}
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param placer the placer
	 * @param stack the stack
	 */
	public default void onBlockPlacedBy(Block block, World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{}

	/**
	 * Called when the {@link Block} is right-clicked by the player.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param player the player
	 * @param hand
	 * @param side the side
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @return true, if successful
	 */
	public default boolean onBlockActivated(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	/**
	 * Called when a neighboring {@link Block} changes.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param neighborBlock the neighbor block
	 * @param neighborPos TODO
	 */
	public default void onNeighborBlockChange(Block block, World world, BlockPos pos, BlockState state, Block neighborBlock, BlockPos neighborPos)
	{}

	/**
	 * Called when the {@link Block} is broken.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 */
	public default void breakBlock(Block block, World world, BlockPos pos, BlockState state)
	{}

	//#end Events

	/**
	 * Gets the bounding box for the {@link Block}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding box
	 */
	public default AxisAlignedBB getBoundingBox(Block block, IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		return null;
	}

	/**
	 * Gets the bounding boxes for the {@link Block}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding boxes
	 */
	public default AxisAlignedBB[] getBoundingBoxes(Block block, IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		return null;
	}

	/**
	 * Whether the {@link Block} can be placed on the side of another block.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @return true, if successful
	 */
	public default boolean canPlaceBlockOnSide(Block block, World world, BlockPos pos, Direction side)
	{
		return true;
	}

	/**
	 * Whether the {@link Block} can be placed at the position.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return true, if successful
	 */
	public default boolean canPlaceBlockAt(Block block, World world, BlockPos pos)
	{
		return true;
	}

	//#region Sub-Blocks
	/**
	 * Gets the damage value for the item when the {@link Block} is dropped.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the int
	 */
	public default int damageDropped(Block block, BlockState state)
	{
		return 0;
	}

	/**
	 * Checks whether the Item has subtypes.
	 *
	 * @return true, if successful
	 */
	public default boolean getHasSubtypes(Block block, Item item)
	{
		return false;
	}

	/**
	 * Fills the list with the sub-blocks associated with this {@link Block}.
	 *
	 * @param block the block
	 * @param tab the tab
	 * @param list the list
	 */
	/*
	 * public default void getSubBlocks(Block block, CreativeTabs tab,
	 * NonNullList<ItemStack> list) {}
	 */

	//#end Sub-blocks

	//#region Colors
	/**
	 * Get the {@link MapColor} for this {@link Block} and the given {@link BlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @param world TODO
	 * @param pos TODO
	 * @return the map color
	 */
	public default MapColor getMapColor(Block block, BlockState state, IBlockReader world, BlockPos pos)
	{
		return null;
	}

	//#end Colors

	//#region State<->Meta
	/**
	 * Gets the {@link BlockState} from <code>meta</code>.
	 *
	 * @param block the block
	 * @param meta the meta
	 * @return the state from meta
	 */
	public default BlockState getStateFromMeta(Block block, BlockState state, int meta)
	{
		return state;
	}

	/**
	 * Gets the metadata from the {@link BlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	public default int getMetaFromState(Block block, BlockState state)
	{
		return 0;
	}

	//#end State<->Meta

	//#region Fullness
	/**
	 * Checks whether a side should be rendered.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param side the side
	 * @return the boolean
	 */
	public default Boolean shouldSideBeRendered(Block block, IBlockReader world, BlockPos pos, BlockState state, Direction side)
	{
		return null;
	}

	/**
	 * Checks whether this {@link IBlockComponent} represents a full {@link Block}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the boolean
	 */
	public default Boolean isFullBlock(Block block, BlockState state)
	{
		return null;
	}

	/**
	 * Checks whether this {@link IBlockComponent} represents a full cube.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the boolean
	 */
	public default Boolean isFullCube(Block block, BlockState state)
	{
		return null;
	}

	/**
	 * Checks whether this {@link IBlockComponent} represents an opaque cube.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the boolean
	 */
	public default Boolean isOpaqueCube(Block block, BlockState state)
	{
		return null;
	}

	//#end Fullness

	//#region Other
	public default boolean canProvidePower(Block block, BlockState state)
	{
		return false;
	}

	/**
	 * Gets the mixed brightness for the {@link Block}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state
	 * @return the mixed brightness for block
	 */
	public default Integer getPackedLightmapCoords(Block block, IBlockReader world, BlockPos pos, BlockState state)
	{
		return null;
	}

	/**
	 * Gets the item dropped by the {@link Block} when broken.
	 *
	 * @param state the state
	 * @param rand the rand
	 * @param fortune the fortune
	 * @return the item dropped
	 */
	public default Item getItemDropped(Block block, BlockState state, Random rand, int fortune)
	{
		return null;
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
	public default Integer quantityDropped(Block block, BlockState state, int fortune, Random random)
	{
		return null;
	}

	/**
	 * Gets the light opacity for the {@link Block}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state
	 * @return the light opacity
	 */
	public default Integer getLightOpacity(Block block, IBlockReader world, BlockPos pos, BlockState state)
	{
		return null;
	}

	//#end Other
}

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

import java.lang.ref.WeakReference;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * @author Ordinastie
 *
 */
public class MBlockState
{
	private static BlockStateFunction toBlockState = new BlockStateFunction();
	private static BlockPredicate blockFilter = new BlockPredicate();

	protected BlockPos pos;
	protected Block block;
	protected BlockState state;

	public MBlockState(BlockPos pos, BlockState state)
	{
		this.pos = pos;
		this.block = state.getBlock();
		this.state = state;
	}

	public MBlockState(BlockPos pos, Block block)
	{
		this.pos = pos;
		this.block = block;
		this.state = block.getDefaultState();
	}

	public MBlockState(BlockState state)
	{
		this.block = state.getBlock();
		this.state = state;
	}

	public MBlockState(Block block)
	{
		this.block = block;
		this.state = block.getDefaultState();
	}

	public MBlockState(IBlockReader world, BlockPos pos)
	{
		this.pos = pos;
		this.state = world.getBlockState(pos);
		this.block = state.getBlock();
		this.state = state.getActualState(world, pos);
	}

	public MBlockState(IBlockReader world, long coord)
	{
		this(world, BlockPos.fromLong(coord));
	}

	public MBlockState(BlockPos pos, MBlockState state)
	{
		this(pos, state.getBlockState());
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public Block getBlock()
	{
		return block;
	}

	public BlockState getBlockState()
	{
		return state;
	}

	public int getX()
	{
		return pos.getX();
	}

	public int getY()
	{
		return pos.getY();
	}

	public int getZ()
	{
		return pos.getZ();
	}

	public boolean isAir()
	{
		return getBlockState().getMaterial() == Material.AIR;
	}

	public MBlockState offset(BlockPos pos)
	{
		return new MBlockState(this.pos.add(pos), this);
	}

	public MBlockState rotate(int count)
	{
		BlockState newState = state;
		for (IProperty<?> prop : state.getProperties().keySet())
		{
			if (prop instanceof PropertyDirection)
			{
				Direction facing = EnumFacingUtils.rotateFacing((Direction) state.getValue(prop), 4 - count);
				newState = newState.withProperty((PropertyDirection) prop, facing);
			}
		}

		return new MBlockState(BlockPosUtils.rotate(pos, count), newState);
	}

	public void placeBlock(World world)
	{
		world.setBlockState(pos, state);
	}

	public void placeBlock(World world, int flag)
	{
		world.setBlockState(pos, state, flag);
	}

	public void breakBlock(World world, int flag)
	{
		world.setBlockState(pos, Blocks.AIR.defaultBlockState(), flag);
	}

	public boolean matchesWorld(IBlockReader world)
	{
		MBlockState mstate = new MBlockState(world, pos);
		return mstate.getBlock() == getBlock()
				&& getBlock().getMetaFromState(mstate.getBlockState()) == getBlock().getMetaFromState(getBlockState());
	}

	public static Iterable<MBlockState> getAllInBox(IBlockReader world, BlockPos from, BlockPos to, Block block, boolean skipAir)
	{
		FluentIterable<MBlockState> it = FluentIterable.from(BlockPos.getAllInBox(from, to)).transform(toBlockState.set(world));
		if (block != null || skipAir)
			it.filter(blockFilter.set(block, skipAir));

		return it;
	}

	public static BlockState fromNBT(CompoundNBT nbt)
	{
		return fromNBT(nbt, "block", "metadata");
	}

	@SuppressWarnings("deprecation")
	public static BlockState fromNBT(CompoundNBT nbt, String blockName, String metadataName)
	{
		if (nbt == null)
			return null;

		Block block = null;
		if (nbt.contains(blockName, NBT.TAG_INT))
			block = Block.getBlockById(nbt.getInteger(blockName));
		else if (nbt.contains(blockName))
			block = Block.getBlockFromName(nbt.getString(blockName));

		if (block == null)
			return null;

		int metadata = nbt.getInt(metadataName);
		return block.getStateFromMeta(metadata);
	}

	public static CompoundNBT toNBT(CompoundNBT nbt, BlockState state)
	{
		return toNBT(nbt, state, "block", "metadata");
	}

	public static CompoundNBT toNBT(CompoundNBT nbt, BlockState state, String blockName, String metadataName)
	{
		if (state == null)
			return nbt;

		nbt.putString(blockName, Block.REGISTRY.getNameForObject(state.getBlock()).toString());
		nbt.putInt(metadataName, state.getBlock().getMetaFromState(state));
		return nbt;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MBlockState))
			return false;

		MBlockState bs = (MBlockState) obj;
		return pos.equals(bs.pos) && block == bs.block && state == bs.state;
	}

	@Override
	public String toString()
	{
		return "[" + pos + "] " + state;
	}

	public static class BlockStateFunction implements Function<BlockPos, MBlockState>
	{
		public WeakReference<IBlockReader> world;

		public BlockStateFunction set(IBlockReader world)
		{
			this.world = new WeakReference<>(world);
			return this;
		}

		@Override
		public MBlockState apply(BlockPos pos)
		{
			return new MBlockState(world.get(), pos);
		}
	}

	public static class BlockPredicate implements Predicate<MBlockState>
	{
		public Block block;
		public boolean skipAir;

		public BlockPredicate set(Block block, boolean skipAir)
		{
			this.block = block;
			this.skipAir = skipAir;
			return this;
		}

		@Override
		public boolean apply(MBlockState state)
		{
			if (block == null)
				return state.getBlock() != Blocks.AIR;
			else
				return state.getBlock() == block;
		}

	}
}

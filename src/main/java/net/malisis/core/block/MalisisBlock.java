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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.block.component.ITickableComponent.PeriodicTickableComponent;
import net.malisis.core.block.component.ITickableComponent.RandomTickableComponent;
import net.malisis.core.block.component.LadderComponent;
import net.malisis.core.inventory.MalisisTab;
import net.malisis.core.item.MalisisItemBlock;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(DefaultRenderer.Block.class)
@SuppressWarnings("deprecation")
public class MalisisBlock extends Block implements IBoundingBox, IRegisterable<Block>, IComponentProvider
{
	protected AxisAlignedBB boundingBox;
	protected final List<IBlockComponent> blockComponents = Lists.newArrayList();
	protected final List<IComponent> components = Lists.newArrayList();

	protected MalisisBlock(Material material)
	{
		super(Block.Properties.of(material));
	}

	protected List<Property<?>> getProperties()
	{
		return Lists.newArrayList();
	}

	protected void buildBlockState()
	{
		List<Property<?>> properties = getProperties();
		for (IBlockComponent component : getBlockComponents())
			properties.addAll(Arrays.asList(component.getProperties()));

		try
		{
			blockStateField.set(this, new BlockStateContainer(this, properties.toArray(new IProperty[0])));
		}
		catch (ReflectiveOperationException e)
		{
			MalisisCore.log.error("[MalisisBlock] Failed to set the new BlockState for {}.", this.getClass().getSimpleName(), e);
		}
	}

	private void buildDefaultState()
	{
		BlockState state = blockState.getBaseState();
		for (IBlockComponent component : getBlockComponents())
			state = component.setDefaultState(this, state);

		setDefaultState(state);
	}

	public List<IBlockComponent> getBlockComponents()
	{
		return blockComponents;
	}

	@Override
	public List<IComponent> getComponents()
	{
		return Stream.concat(blockComponents.stream(), components.stream()).collect(Collectors.toList());
	}

	@Override
	public void addComponent(IComponent component)
	{
		if (component.isClientComponent() && !MalisisCore.isClient())
			throw new IllegalStateException("Trying to add component " + component.getClass().getSimpleName() + " on server.");

		if (component instanceof IBlockComponent)
		{
			blockComponents.add((IBlockComponent) component);
			for (IComponent dep : ((IBlockComponent) component).getDependencies())
				addComponent(dep);

			buildBlockState();
			buildDefaultState();
		}
		else
			components.add(component);

		component.onComponentAdded(this);

		lightOpacity = getDefaultState().isOpaqueCube() ? 255 : 0;
	}

	@Override
	public MalisisBlock setName(String name)
	{
		IRegisterable.super.setName(name);
		setUnlocalizedName(name);
		return this;
	}

	public String getUnlocalizedName(BlockState state)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			String name = component.getUnlocalizedName(this, state);
			if (name != null)
				return name;
		}
		return getUnlocalizedName();
	}

	@Override
	public Item getItem(Block block)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			Item item = component.getItem(this);
			if (item == null || item.getClass() != MalisisItemBlock.class)
				return item;
		}

		return IRegisterable.super.getItem(this);
	}

	public boolean hasItemSubtypes(Item item)
	{
		for (IBlockComponent component : getBlockComponents())
			if (component.getHasSubtypes(this, item))
				return true;
		return false;
	}

	public void setTexture(String textureName)
	{
		if (!StringUtils.isEmpty(textureName) && MalisisCore.isClient())
		{
			Icon icon = Icon.from(textureName);
			addComponent((IIconProvider) () -> icon);
		}
	}

	public void setTexture(Item item)
	{
		if (item != null && MalisisCore.isClient())
		{
			Icon icon = Icon.from(item);
			addComponent((IIconProvider) () -> icon);
		}
	}

	public void setTexture(Block block)
	{
		if (block != null)
			setTexture(block.getDefaultState());
	}

	public void setTexture(BlockState state)
	{
		if (state != null && MalisisCore.isClient())
		{
			Icon icon = Icon.from(state);
			addComponent((IIconProvider) () -> icon);
		}
	}

	public BlockState getStateFromItemStack(ItemStack itemStack)
	{
		return getStateFromMeta(itemStack.getItem().getMetadata(itemStack.getMetadata()));
	}

	//EVENTS
	@Override
	public void onBlockAdded(World world, BlockPos pos, BlockState state)
	{
		getBlockComponents().forEach(c -> c.onBlockAdded(this, world, pos, state));
	}

	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, EnumHand hand)
	{
		BlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
		for (IBlockComponent component : getBlockComponents())
			state = component.getStateForPlacement(this, world, pos, state, facing, hitX, hitY, hitZ, meta, placer, hand);

		return state;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		getBlockComponents().forEach(c -> c.onBlockPlacedBy(this, world, pos, state, placer, stack));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, EnumHand hand, Direction side, float hitX, float hitY, float hitZ)
	{
		boolean b = false;
		for (IBlockComponent component : getBlockComponents())
			b |= component.onBlockActivated(this, world, pos, state, player, hand, side, hitX, hitY, hitZ);

		return b;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos)
	{
		getBlockComponents().forEach(c -> c.onNeighborBlockChange(this, world, pos, state, neighborBlock, neighborPos));
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		boolean b = false;
		for (IBlockComponent component : getBlockComponents())
			b |= component.canProvidePower(this, state);

		return b;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state)
	{
		getBlockComponents().forEach(c -> c.breakBlock(this, world, pos, state));

		super.breakBlock(world, pos, state);
	}

	//UPDATE
	@Override
	public void randomTick(World world, BlockPos pos, BlockState state, Random rand)
	{
		RandomTickableComponent rtc = IComponent.getComponent(RandomTickableComponent.class, this);
		if (rtc != null)
			rtc.update(this, world, pos, state, rand);
	}

	@Override
	public void updateTick(World world, BlockPos pos, BlockState state, Random rand)
	{
		PeriodicTickableComponent ptc = IComponent.getComponent(PeriodicTickableComponent.class, this);
		if (ptc == null)
			return;

		int nextTick = ptc.update(this, world, pos, state, rand);
		if (nextTick > 0)
			world.scheduleBlockUpdate(pos, this, nextTick, nextTick);
	}

	//BOUNDING BOX
	@Override
	public AxisAlignedBB getBoundingBox(IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			AxisAlignedBB aabb = component.getBoundingBox(this, world, pos, state, type);
			if (aabb != null)
				return aabb;
		}

		return FULL_BLOCK_AABB;
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(IBlockReader world, BlockPos pos, BlockState state, BoundingBoxType type)
	{
		List<AxisAlignedBB> list = Lists.newArrayList();
		for (IBlockComponent component : getBlockComponents())
		{
			AxisAlignedBB[] aabbs = component.getBoundingBoxes(this, world, pos, state, type);
			if (aabbs != null)
				Collections.addAll(list, aabbs);
		}

		return list.size() != 0 ? list.toArray(new AxisAlignedBB[0]) : IBoundingBox.super.getBoundingBoxes(world, pos, state, type);
	}

	@Override
	public void addCollisionBoxToList(BlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, @Nullable Entity collidingEntity, boolean useActualState)
	{
		IBoundingBox.super.addCollisionBoxToList(state, world, pos, mask, list, collidingEntity, false);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(BlockState state, World world, BlockPos pos)
	{
		return IBoundingBox.super.getSelectedBoundingBox(state, world, pos);
	}

	@Override
	public RayTraceResult collisionRayTrace(BlockState state, World world, BlockPos pos, Vector3d src, Vector3d dest)
	{
		return IBoundingBox.super.collisionRayTrace(state, world, pos, src, dest);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, Direction side)
	{
		for (IBlockComponent component : getBlockComponents())
			if (!component.canPlaceBlockOnSide(this, world, pos, side))
				return false;

		return super.canPlaceBlockOnSide(world, pos, side);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		for (IBlockComponent component : getBlockComponents())
			if (!component.canPlaceBlockAt(this, world, pos))
				return false;

		return super.canPlaceBlockAt(world, pos);
	}

	//SUB BLOCKS
	@Override
	public int damageDropped(BlockState state)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			int damage = component.damageDropped(this, state);
			if (damage != 0)
				return damage;
		}

		return getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		NonNullList<ItemStack> l = NonNullList.<ItemStack> create();
		for (IBlockComponent component : getBlockComponents())
			component.getSubBlocks(this, tab, l);

		//only add default itemStack if components don't add itemStacks
		if (l.isEmpty())
			super.getSubBlocks(tab, list);
		else
			list.addAll(l);
	}

	//COLORS

	@Override
	public MapColor getMapColor(BlockState state, IBlockReader world, BlockPos pos)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			MapColor color = component.getMapColor(this, state, world, pos);
			if (color != null)
				return color;
		}

		return super.getMapColor(state, world, pos);
	}

	@Override
	public BlockState getStateFromMeta(int meta)
	{
		BlockState state = getDefaultState();
		for (IBlockComponent component : getBlockComponents())
			state = component.getStateFromMeta(this, state, meta);

		return state;
	}

	@Override
	public int getMetaFromState(BlockState state)
	{
		int meta = 0;
		for (IBlockComponent component : getBlockComponents())
			meta += component.getMetaFromState(this, state);

		return meta;
	}

	//FULLNESS

	@Override
	public boolean shouldSideBeRendered(BlockState state, IBlockReader world, BlockPos pos, Direction side)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			Boolean render = component.shouldSideBeRendered(this, world, pos, state, side);
			if (render != null)
				return render;
		}
		return super.shouldSideBeRendered(state, world, pos, side);
	}

	@Override
	public boolean isFullBlock(BlockState state)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			Boolean full = component.isFullBlock(this, state);
			if (full != null)
				return full;
		}

		return super.isFullBlock(state);
	}

	@Override
	public boolean isFullCube(BlockState state)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			Boolean full = component.isFullCube(this, state);
			if (full != null)
				return full;
		}

		return super.isFullCube(state);
	}

	@Override
	public boolean isOpaqueCube(BlockState state)
	{
		//parent constructor call
		if (getBlockComponents() == null)
			return super.isOpaqueCube(state);

		for (IBlockComponent component : getBlockComponents())
		{
			Boolean opaque = component.isOpaqueCube(this, state);
			if (opaque != null)
				return opaque;
		}
		return super.isOpaqueCube(state);
	}

	//OTHER

	@Override
	@SideOnly(Side.CLIENT)
	public int getPackedLightmapCoords(BlockState state, IBlockReader world, BlockPos pos)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			//TODO: use max light value
			Integer light = component.getPackedLightmapCoords(this, world, pos, state);
			if (light != null)
				return light;
		}
		return super.getPackedLightmapCoords(state, world, pos);
	}

	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			Item item = component.getItemDropped(this, state, rand, fortune);
			if (item != null)
				return item;
		}

		return super.getItemDropped(state, rand, fortune);

	}

	@Override
	public int quantityDropped(BlockState state, int fortune, Random random)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			Integer quantity = component.quantityDropped(this, state, fortune, random);
			if (quantity != null)
				return quantity;
		}

		return super.quantityDropped(state, fortune, random);
	}

	@Override
	public int getLightOpacity(BlockState state, IBlockReader world, BlockPos pos)
	{
		for (IBlockComponent component : getBlockComponents())
		{
			Integer quantity = component.getLightOpacity(this, world, pos, state);
			if (quantity != null)
				return quantity;
		}
		return super.getLightOpacity(state, world, pos);
	}

	@Override
	public boolean isLadder(BlockState state, IBlockReader world, BlockPos pos, LivingEntity entity)
	{
		return IComponent.getComponent(LadderComponent.class, this) != null;
	}

	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public MalisisBlock setCreativeTab(CreativeTabs tab)
	{
		super.setCreativeTab(tab);
		if (tab instanceof MalisisTab)
			((MalisisTab) tab).addItem(this);
		return this;
	}
}

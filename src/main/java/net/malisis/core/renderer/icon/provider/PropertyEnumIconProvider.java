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

import java.util.EnumMap;

import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider.IStatesIconProvider;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

/**
 * @author Ordinastie
 *
 */
public class PropertyEnumIconProvider<T extends Enum<T> & IStringSerializable> implements IStatesIconProvider
{
	private EnumProperty<T> property;
	private Icon defaultIcon;
	private EnumMap<T, Icon> icons;

	public PropertyEnumIconProvider(EnumProperty<T> property, Class<T> enumClass, String defaultName)
	{
		this.property = property;
		this.icons = new EnumMap<>(enumClass);
		this.defaultIcon = Icon.from(defaultName);
	}

	public PropertyEnumIconProvider(EnumProperty<T> property, Class<T> enumClass, Icon defaultIcon)
	{
		this.property = property;
		this.icons = new EnumMap<>(enumClass);
		this.defaultIcon = defaultIcon;
	}

	public PropertyEnumIconProvider(EnumProperty<T> property, Class<T> enumClass)
	{
		this(property, enumClass, (Icon) null);
	}

	public void setIcon(T enumValue, Icon icon)
	{
		icons.put(enumValue, icon);
	}

	public void setIcon(T enumValue, String iconName)
	{
		icons.put(enumValue, Icon.from(iconName));
	}

	@Override
	public Icon getIcon()
	{
		return defaultIcon;
	}

	public Icon getIcon(T value)
	{
		return icons.getOrDefault(value, getIcon());
	}

	@Override
	public Icon getIcon(BlockState state)
	{
		return state != null ? getIcon(state.getValue(property)) : getIcon();
	}
}

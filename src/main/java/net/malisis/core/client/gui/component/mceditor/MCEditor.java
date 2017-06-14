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

package net.malisis.core.client.gui.component.mceditor;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class MCEditor extends UIContainer<MCEditor> implements IGuiText<MCEditor>
{
	private UITextField tf;
	private EcfSelect sel;
	private UICheckBox cb;

	private MalisisFont font = MalisisFont.minecraftFont;
	private FontOptions fontOptions = FontOptions.builder().build();

	public MCEditor(MalisisGui gui)
	{
		super(gui);
		tf = new UITextField(gui, true);
		tf.setSize(0, -14).setAnchor(Anchor.BOTTOM);

		sel = new EcfSelect(gui, this);

		cb = new UICheckBox(gui, "Use litteral formatting");
		cb.setPosition(85, 0).register(this);

		add(tf, sel, cb);
	}

	public MCEditor(MalisisGui gui, int width, int height)
	{
		this(gui);
		setSize(width, height);
	}

	public UITextField getTextfield()
	{
		return tf;
	}

	public UISelect<TextFormatting> getSelect()
	{
		return sel;
	}

	//#region IGuiText
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public MCEditor setFont(MalisisFont font)
	{
		this.font = font;
		return this;
	}

	@Override
	public FontOptions getFontOptions()
	{
		return fontOptions;
	}

	@Override
	public MCEditor setFontOptions(FontOptions fro)
	{
		this.fontOptions = fro;
		return this;
	}

	//#end IGuiText

	@Subscribe
	public void onChecked(UICheckBox.CheckEvent event)
	{
		//tf.getFontOptions().disableECF = event.isChecked();
		tf.buildLines();
	}
}

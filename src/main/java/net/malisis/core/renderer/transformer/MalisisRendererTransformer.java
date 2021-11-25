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

package net.malisis.core.renderer.transformer;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.MalisisClassTransformer;
import net.malisis.core.asm.mappings.McpMethodMapping;

/**
 * @author Ordinastie
 *
 */
public class MalisisRendererTransformer extends MalisisClassTransformer
{
	@Override
	public void registerHooks()
	{
		register(blockHook());
		register(itemHook());
		register(particleHook());
	}

	private AsmHook blockHook()
	{
		McpMethodMapping renderBlock = new McpMethodMapping("renderBlock",
															"func_175018_a",
															"net/minecraft/client/renderer/BlockRendererDispatcher",
															"(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z");
		AsmHook ah = new AsmHook(renderBlock);

		//int i = state.getRenderType();
		//	    ALOAD 1
		//	    INVOKEINTERFACE net/minecraft/block/state/IBlockState.getRenderType ()Lnet/minecraft/util/EnumBlockRenderType;
		//	    ASTORE 5
		McpMethodMapping getRenderType = new McpMethodMapping(	"getRenderType",
																"func_185901_i",
																"net/minecraft/block/state/IBlockState",
																"()Lnet/minecraft/util/EnumBlockRenderType;");

		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 1));
		match.add(getRenderType.getInsnNode(INVOKEINTERFACE));
		match.add(new VarInsnNode(ASTORE, 5));

		//		CallbackResult<Boolean> cb = Registries.processRenderBlockCallbacks(worldRendererIn, blockAccess, pos, state);
		//		if (cb.shouldReturn())
		//			return cb.getValue();
		//		ALOAD 4
		//	    ALOAD 3
		//	    ALOAD 2
		//	    ALOAD 1
		//		INVOKESTATIC net/malisis/core/registry/Registries.processRenderBlockCallbacks (Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/malisis/core/util/callback/CallbackResult;
		//		ASTORE 8
		//		L2
		//		LINENUMBER 69 L2
		//		ALOAD 8
		//		INVOKEVIRTUAL net/malisis/core/util/callback/CallbackResult.shouldReturn ()Z
		//		IFEQ L3
		//		L4
		//		LINENUMBER 70 L4
		//		ALOAD 8
		//		INVOKEVIRTUAL net/malisis/core/util/callback/CallbackResult.getValue ()Ljava/lang/Object;
		//		CHECKCAST java/lang/Boolean
		//		INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z
		//		IRETURN

		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 4));
		insert.add(new VarInsnNode(ALOAD, 3));
		insert.add(new VarInsnNode(ALOAD, 2));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new MethodInsnNode(	INVOKESTATIC,
                "broken/core/registry/Registries",
										"processRenderBlockCallbacks",
										"(Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/malisis/core/util/callback/CallbackResult;",
										false));
		insert.add(new VarInsnNode(ASTORE, 8));
		insert.add(new VarInsnNode(ALOAD, 8));
		insert.add(new MethodInsnNode(INVOKEVIRTUAL, "broken/core/util/callback/CallbackResult", "shouldReturn", "()Z", false));
		insert.add(new JumpInsnNode(IFEQ, falseLabel));
		insert.add(new VarInsnNode(ALOAD, 8));
		insert.add(new MethodInsnNode(	INVOKEVIRTUAL,
                "broken/core/util/callback/CallbackResult",
										"getValue",
										"()Ljava/lang/Object;",
										false));
		insert.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
		insert.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
		insert.add(new InsnNode(IRETURN));
		insert.add(falseLabel);

		ah.jumpAfter(match).insert(insert);
		return ah;
	}

	private AsmHook itemHook()
	{
		McpMethodMapping renderModel = new McpMethodMapping("renderModel",
															"func_191967_a",
															"net/minecraft/client/renderer/RenderItem",
															"(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V");
		AsmHook ah = new AsmHook(renderModel);

		//		if (Registries.renderItem(itemStack))
		//			return;

		//		ALOAD 3
		//	    INVOKESTATIC net/malisis/core/registry/Registries.renderItem (Lnet/minecraft/item/ItemStack;)Z
		//	    IFEQ L6
		//	   L7
		//	    LINENUMBER 119 L7
		//	    ACONST_NULL
		//	    RETURN
		//	   L6

		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 3));
		insert.add(new MethodInsnNode(	INVOKESTATIC,
                "broken/core/registry/Registries",
										"renderItem",
										"(Lnet/minecraft/item/ItemStack;)Z",
										false));
		insert.add(new JumpInsnNode(IFEQ, falseLabel));
		insert.add(new InsnNode(RETURN));
		insert.add(falseLabel);

		ah.insert(insert);
		return ah;
	}

	private AsmHook particleHook()
	{
		McpMethodMapping getTexture = new McpMethodMapping(	"getTexture",
															"func_178122_a",
															"net/minecraft/client/renderer/BlockModelShapes",
															"(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;");
		AsmHook ah = new AsmHook(getTexture);

		//		if (IComponent.getComponent(IIconProvider.class, state.getBlock()) != null)
		//			return MalisisRegistry.getParticleIcon(state);
		//		LDC Lnet/malisis/core/renderer/icon/provider/IIconProvider;.class
		//		ALOAD 1
		//	    INVOKEINTERFACE net/minecraft/block/state/IBlockState.getBlock ()Lnet/minecraft/block/Block;
		//	    INVOKESTATIC net/malisis/core/block/IComponent.getComponent (Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;
		//	    IFNULL L6
		//	    ALOAD 1
		//	    INVOKESTATIC net/malisis/core/registry/Registries.getParticleIcon (Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;
		//		ARETURN

		McpMethodMapping getBlock = new McpMethodMapping(	"getBlock",
															"func_177230_c",
															"net/minecraft/block/state/IBlockState",
															"()Lnet/minecraft/block/Block;");

		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new LdcInsnNode(Type.getObjectType("broken/core/renderer/icon/provider/IIconProvider")));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(getBlock.getInsnNode(INVOKEINTERFACE));
		insert.add(new MethodInsnNode(	INVOKESTATIC,
                "broken/core/block/IComponent",
										"getComponent",
										"(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",
										false));
		insert.add(new JumpInsnNode(IFNULL, falseLabel));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new MethodInsnNode(	INVOKESTATIC,
                "broken/core/registry/Registries",
										"getParticleIcon",
										"(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
										false));
		insert.add(new InsnNode(ARETURN));
		insert.add(falseLabel);

		ah.insert(insert);

		return ah;
	}
}

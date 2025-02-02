/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

package net.malisis.core.asm.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.malisis.core.util.clientnotif.ClientNotificationManager;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * @author Ordinastie
 *
 */
public class MixinClientNotif
{

	@Mixin(World.class)
	public static class MixinWorld
	{
		//keep track of neighbor changes on the server to they can be sent to clients
		@Inject(method = "neighborChanged", at = @At("HEAD"))
		private void onNeighborChanged(BlockPos pos, Block neighborBlock, BlockPos neighborPos, CallbackInfo ci)
		{
			ClientNotificationManager.notify((World) (Object) this, pos, neighborBlock, neighborPos);
		}
	}

	@Mixin(ServerWorld.class)
	public static class MixinWorldServer
	{
		//at the end of server tick, send neighbor changes to clients
		@Inject(method = "tick", at = @At("TAIL"))
		private void onTick(CallbackInfo ci)
		{
			ClientNotificationManager.sendNeighborNotification((ServerWorld) (Object) this);
		}

	}
}

package com.duggernaut.qlicious;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import com.duggernaut.qlicious.music.net.PlayInstrumentPacket;
import com.duggernaut.qlicious.net.AbstractPacket;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;


public class DebugKeyHandler
{
	public static final int MUTE_SOUND_KEY = 0;
	public static final String[] desc = {"key.qlicious.mutesound"};
	private static final int[] keyValues = {Keyboard.KEY_K};
	
	private final KeyBinding[] keys;
	public DebugKeyHandler()
	{
		keys = new KeyBinding[desc.length];
		for(int i = 0; i < desc.length; ++i)
		{
			keys[i] = new KeyBinding(desc[i], keyValues[i], "key.qlicious.debug");
			//ClientRegistry.registerKeyBinding(keys[i]);
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event)
	{
		if(!FMLClientHandler.instance().isGUIOpen(GuiChat.class))
		{
			if(keys[MUTE_SOUND_KEY].isPressed())
			{
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				Entity entity = null;
				for(Object entObj : player.worldObj.loadedEntityList)
				{
					if(!entObj.equals(player) && entObj instanceof Entity)
					{
						entity = (Entity)entObj;
						break;
					}
				}
				
				if(entity != null)
				{
					Logger.log(String.format("Entity %s is now playing a song!", entity.toString()));
					AbstractPacket packet = new PlayInstrumentPacket(entity.getEntityId(), 0, 0, 1, PlayInstrumentPacket.PLAY_COMMAND);
					QliciousMod.packetPipeline.sendToServer(packet);
				}
			}
		}
	}
}

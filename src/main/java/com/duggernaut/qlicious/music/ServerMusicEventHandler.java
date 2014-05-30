package com.duggernaut.qlicious.music;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ServerMusicEventHandler {
	
	@SubscribeEvent
	public void onTick(TickEvent.WorldTickEvent event)
	{
		SongSystem.server.updateSpells();
	}
}

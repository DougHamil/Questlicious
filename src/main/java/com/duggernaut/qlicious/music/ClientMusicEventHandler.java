package com.duggernaut.qlicious.music;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientMusicEventHandler
{
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event)
	{
		SongSystem.client.updateSpells();
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.theWorld == null)
			MusicSystem.client.purge();
		else
			this.updateForPlayer(Minecraft.getMinecraft().thePlayer);
	}
	
	public void updateForPlayer(EntityPlayer player)
	{
		if(player == null)
			return;
		
		Vec3 playerPos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
		
		Song activeSong = MusicSystem.client.getActiveSong();
		
		if(activeSong != null)
		{
			for(Entity p : MusicSystem.client.getEntitiesForActiveSong())
			{
				if(p != null && p != player && p.dimension == player.dimension)
				{
					Vec3 pPos = Vec3.createVectorHelper(p.posX,  p.posY, p.posZ);
					Vec3 delta = playerPos.subtract(pPos);
					double dist = delta.lengthVector();
					
					float volume = dist > MusicSystem.SONG_HEARING_DISTANCE ? 0f : 1f - (float)(dist / MusicSystem.SONG_HEARING_DISTANCE);
					activeSong.setEntityVolume(p, volume);
				}
				else if(p != null && p == player)
					activeSong.setEntityVolume(p, 1.0f);
			}
		}
	}
	
	@SubscribeEvent
	public void onLeave(WorldEvent.Unload event)
	{
		MusicSystem.client.purge();
	}
}

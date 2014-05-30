package com.duggernaut.qlicious.music;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import com.duggernaut.qlicious.music.instruments.Instrument;

/*
 * Common base class for all Song Spells
 */
public class SongSpell 
{
	protected Song song;
	protected EntityPlayer player;
	protected final boolean isClientSide;
	protected final boolean isServerSide;
	private int id;
	
	public SongSpell(Song song, EntityPlayer clientPlayer)
	{
		this.id = song.getId();
		this.song = song;
		this.player = clientPlayer;
		this.isClientSide = this.player != null;
		this.isServerSide = !this.isClientSide;
	}
	
	public int getId()
	{
		return this.id;
	}

	public void onEntityJoined(Entity entity, Instrument instrument)
	{
		
	}
	
	public void onEntityLeft(Entity entity)
	{
		
	}
	
	public void onSongTick(long elapsedSongTimeMillis)
	{
		
	}
	
	public void onSongStopped()
	{
		
	}
}

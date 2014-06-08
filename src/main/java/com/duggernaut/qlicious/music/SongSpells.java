package com.duggernaut.qlicious.music;

import java.lang.reflect.Constructor;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;

public enum SongSpells
{
	CROP_GROWTH(1, "Grow Crops", "Lessons.mid", "CropGrowthSongSpell");
	
	private static final String SPELL_PACKAGE = "com.duggernaut.qlicious.music.spell";

	private final String name;
	private final String midiFilename;
	private final String className;
	private final int id;
	
	private SongSpells(int id, String name, String midi, String clazz)
	{
		this.id = id;
		this.name = name;
		this.midiFilename = midi;
		this.className = clazz;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getMidiFilename()
	{
		return this.midiFilename;
	}
	
	public String getClassName()
	{
		return this.className;
	}
	
	public static SongSpells fromId(int id)
	{
		for(SongSpells s : SongSpells.values())
		{
			if(s.getId() == id)
				return s;
		}
		return null;
	}
	
	public static SongSpells getSongSpellByMidiFilename(String filename)
	{
		for(SongSpells s : SongSpells.values())
		{
			if(s.getMidiFilename().equals(filename))
				return s;
		}
		
		return null;
	}
	
	
	public static SongSpells getRandomSongSpell()
	{
		return SongSpells.values()[(new Random()).nextInt(SongSpells.values().length)];
	}
	
	public static SongSpell instantiateForSong(Song song, EntityPlayer clientPlayer)
	{
		SongSpells spellIdentifier = SongSpells.fromId(song.getSongSpellId());
		
		if(spellIdentifier != null)
		{
			String spellClassName = spellIdentifier.getClassName();
			if(spellClassName != null)
			{
				spellClassName = spellClassName + (clientPlayer != null ? "Client" : "Server");
				
				try {
					Class spellSongClass = Class.forName(SPELL_PACKAGE + "." + spellClassName);
					Constructor spellConstructor = spellSongClass.getConstructor(Song.class, EntityPlayer.class);
					return (SongSpell)spellConstructor.newInstance(song, clientPlayer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		}
		
		return null;
	}
}

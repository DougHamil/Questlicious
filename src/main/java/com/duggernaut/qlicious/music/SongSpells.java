package com.duggernaut.qlicious.music;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.collect.ImmutableMap;

public class SongSpells
{
	private static final String SPELL_PACKAGE = "com.duggernaut.qlicious.music.spell";
	
	// Mapping of midi file names to the SongSpell class that should be instantiated
	private static final ImmutableMap<String, String> FILENAME_TO_SPELL_CLASS = ImmutableMap.<String, String>builder()
			.put("Lessons.mid", "CropGrowthSongSpell")
			.build(); 

	public static SongSpell instantiateForSong(Song song, EntityPlayer clientPlayer)
	{
		String spellClassName = FILENAME_TO_SPELL_CLASS.get(song.getFileName());
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
		
		return null;
	}
}

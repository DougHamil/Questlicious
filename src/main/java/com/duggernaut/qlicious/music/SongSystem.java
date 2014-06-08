package com.duggernaut.qlicious.music;

import java.util.Collection;
import java.util.Map;

import com.duggernaut.qlicious.Logger;
import com.duggernaut.qlicious.QliciousMod;
import com.duggernaut.qlicious.music.net.RegisterSongPacket;
import com.duggernaut.qlicious.net.AbstractPacket;
import com.google.common.collect.Maps;

// Responsible for synchronizing the active songs across the server and clients
public class SongSystem 
{
	public static SongSystem server;
	public static SongSystem client;
	
	private Map<Integer, Song> songsById = Maps.newHashMap();
	private Map<Integer, SongSpell> songSpellsById = Maps.newHashMap();
	private Map<Integer, Long> songStartTimes = Maps.newHashMap();
	
	public SongSpell getSongSpellById(int id)
	{
		return this.songSpellsById.get(id);
	}
	
	public Song getSongById(int id)
	{
		return songsById.get(id);
	}
	
	public Collection<SongSpell> getSongSpells()
	{
		return this.songSpellsById.values();
	}
	
	public void updateSpells()
	{
		long currentTime = System.currentTimeMillis();
		for(SongSpell spell : this.getSongSpells())
		{
			long songStartTime = this.songStartTimes.get(spell.getId());
			spell.onSongTick(currentTime - songStartTime);
		}
	}
	
	// Server - Initialize a new random song
	public Song initializeRandomSong()
	{
		SongSpells songSpell = SongSpells.getRandomSongSpell();
		Song newSong = new Song(songSpell.getId(), MIDIBank.instance.getSequenceForSong(songSpell.getMidiFilename()));
		this.initializeSong(newSong);
		return newSong;
	}
	
	// Server - Initialize a new song with the midi file provided
	public Song initializeNewSong(int songSpellId)
	{
		SongSpells spell = SongSpells.fromId(songSpellId);
		Logger.log("Initializing song for spell: "+songSpellId);
		Song newSong = new Song(spell.getId(), MIDIBank.instance.getSequenceForSong(spell.getMidiFilename()));
		this.initializeSong(newSong);
		return newSong;
	}
	
	// Server - tell clients about the new song
	private void initializeSong(Song song)
	{
		Logger.log("Server initialized song: "+song.getSongSpellName());
		this.songsById.put(song.getId(), song);
		SongSpell spell = QliciousMod.proxy.createSongSpell(song);
		if(spell != null)
		{
			this.songSpellsById.put(song.getId(), spell);
			this.songStartTimes.put(song.getId(), System.currentTimeMillis());
		}
		else
		{
			Logger.log("Server - could not create spell for song "+song.getSongSpellName());
		}
		AbstractPacket packet = new RegisterSongPacket(song, false);
		QliciousMod.packetPipeline.sendToAll(packet);
	}
	
	// Client - register a new song to the mapping
	public void registerSong(Song song)
	{
		Logger.log("Client registered song: "+song.getSongSpellName());
		this.songsById.put(song.getId(),  song);
		SongSpell spell = QliciousMod.proxy.createSongSpell(song);
		if(spell != null)
		{
			this.songSpellsById.put(song.getId(), spell);
			this.songStartTimes.put(song.getId(), System.currentTimeMillis());
		}
	}
	
	public void unregisterSong(Song song)
	{
		this.unregisterSong(song.getId());
	}
	
	public void unregisterSong(int id)
	{
		this.songsById.remove(id);
		this.songSpellsById.remove(id);
	}
}

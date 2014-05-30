package com.duggernaut.qlicious.music;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import com.duggernaut.qlicious.Logger;
import com.duggernaut.qlicious.QliciousMod;
import com.duggernaut.qlicious.music.instruments.Fiddle;
import com.duggernaut.qlicious.music.instruments.Flute;
import com.duggernaut.qlicious.music.instruments.Kalimba;
import com.duggernaut.qlicious.music.instruments.Ocarina;
import com.duggernaut.qlicious.music.net.PlayInstrumentPacket;
import com.duggernaut.qlicious.music.net.RegisterSongPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.registry.GameRegistry;

public class MusicSystem
{
	public static final float SONG_HEARING_DISTANCE = 20f;
	// - Instruments
	private static final Flute flute = new Flute();
	private static final Fiddle fiddle = new Fiddle();
	private static final Kalimba kalimba = new Kalimba();
	private static final Ocarina ocarina = new Ocarina();
	
	private Song activeSong;
	public static MusicSystem server;
	public static MusicSystem client;
	public static MidiChannel[] channels;
	public static int soundbankId;
	public Multimap<Song, Entity> songs = ArrayListMultimap.create();
	public Map<Song, SongSpell> songSpells = Maps.newHashMap();
	
	public static Map<String, Instrument> instrumentByName = Maps.newHashMap();
	public static Synthesizer synth;
	
	public static void initialize(File sourceFile)
	{
		GameRegistry.registerItem(flute, "flute");
		GameRegistry.registerItem(fiddle,  "fiddle");
		GameRegistry.registerItem(ocarina, "ocarina");
		GameRegistry.registerItem(kalimba, "kalimba");
		new MIDIBank(sourceFile);
		SongSystem.server = new SongSystem();
		SongSystem.client = new SongSystem();
		server = new MusicSystem();
		client = new MusicSystem();
		
		try
		{
			InputStream is = new BufferedInputStream(MusicSystem.class.getClassLoader().getResourceAsStream("midi/soundbanks/soundsynth.sf2"));
			Soundbank soundbank = MidiSystem.getSoundbank(is);
			synth = MidiSystem.getSynthesizer();
			synth.open();
			channels = synth.getChannels();
			Logger.log("Is soundbank supported? "+synth.isSoundbankSupported(soundbank));
			Logger.log("Soundbank instruments: "+soundbank.getInstruments().length);
			Logger.log("Loading instruments from soundbank: "+synth.loadAllInstruments(soundbank));
			Logger.log("Available instruments: "+synth.getLoadedInstruments().length);
			for(Instrument i : soundbank.getInstruments())
				instrumentByName.put(i.getName(), i);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		QliciousMod.packetPipeline.registerPacket(PlayInstrumentPacket.class);
		QliciousMod.packetPipeline.registerPacket(RegisterSongPacket.class);
	}
	
	public void purge()
	{
		if(this.activeSong != null)
		{
			this.activeSong.stopAll();
			this.activeSong = null;
		}
		this.songs.clear();
	}
	
	public Song findNearbySong(Entity entity)
	{
		for(Map.Entry<Song, Collection<Entity>> entry : this.songs.asMap().entrySet())
		{
			for(Entity e : entry.getValue())
			{
				if(e != entity && e.dimension == entity.dimension && getDistanceBetweenEntities(e, entity) <= SONG_HEARING_DISTANCE)
				{
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	public void removeEntityFromSong(Entity entity)
	{
		Logger.log(String.format("Setting entity %d as stopped playing song", entity.getEntityId()));
		for(Map.Entry<Song, Collection<Entity>> entry : this.songs.asMap().entrySet())
		{
			if(entry.getValue().contains(entity))
			{
				this.songs.get(entry.getKey()).remove(entity);
				if(entry.getKey().isEmpty() && this.activeSong == entry.getKey())
					this.setActiveSong(null);
				break;
			}
		}
	}
	
	public void setActiveSong(Song song)
	{
		if(activeSong == song)
			return;
		
		if(activeSong != null)
			this.activeSong.stopAll();
		this.activeSong = song;
		if(this.activeSong != null)
			this.activeSong.play();
		Logger.log(String.format("Set %s as the active song", this.activeSong == null ? "NULL" : this.activeSong.getFileName()));
	}
	
	public Song getSongForEntity(Entity entity)
	{
		for(Map.Entry<Song, Collection<Entity>> entry : this.songs.asMap().entrySet())
		{
			if(entry.getValue().contains(entity))
			{
				return entry.getKey();
			}
		}
		return null;
	}
	
	// Server/Client - set mapping for entity to song and instrument
	public void setEntitySong(Entity entity, Song song)
	{
		Logger.log(String.format("Setting entity %d as playing song %s", entity.getEntityId(), song.getFileName()));
		this.songs.put(song, entity);
	}
	
	private float getDistanceBetweenEntities(Entity a, Entity b)
	{
		Vec3 av = Vec3.createVectorHelper(a.posX, a.posY, a.posZ);
		Vec3 bv = Vec3.createVectorHelper(b.posX, b.posY, b.posZ);
		return (float)av.subtract(bv).lengthVector();
	}
	
	public Song getActiveSong()
	{
		return this.activeSong;
	}
	
	public Collection<Entity> getEntitiesForActiveSong()
	{
		if(this.activeSong != null)
			return this.songs.get(this.activeSong);
		return Collections.EMPTY_LIST;
	}
}

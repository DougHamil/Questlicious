package com.duggernaut.qlicious.music;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MIDIBank {
	public static MIDIBank instance;
	
	private Map<String, Sequence> sequences = Maps.newHashMap();

	private boolean isZipped;
	private File sourceFile;
	
	public MIDIBank(File sourceFile) {
		
		instance = this;
		this.sourceFile = sourceFile;
		this.isZipped = !sourceFile.isDirectory();
		System.out.println("IS ZIPPED: "+this.isZipped);
		if (sourceFile.isDirectory())
			this.loadDev(sourceFile);
		else
			this.loadZip(sourceFile);
	}

	public String getRandomSongName()
	{
		List<String> songNames = Lists.newArrayList(sequences.keySet());
		return songNames.get((new Random()).nextInt(songNames.size()));
	}
	
	public Sequence getSequenceForSong(String song)
	{
		return copySequence(this.sequences.get(song));
	}
	
	private Sequence copySequence(Sequence s)
	{
		try {
			Sequence c = new Sequence(s.getDivisionType(), s.getResolution(), s.getTracks().length);
			Track[] sourceTracks = s.getTracks();
			for(int i = 0; i < sourceTracks.length; i++)
			{
				Track st = sourceTracks[i];
				Track ct = c.createTrack();
				for(int j = 0; j < st.size(); j++)
				{
					ct.add(st.get(j));
				}
			}
			return c;
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void loadDev(File sourceFile) {
		File songsDir = new File(sourceFile.getAbsolutePath() + "/midi/songs");
		for(File songFile : songsDir.listFiles())
		{
			if(!songFile.isDirectory())
			{
				try {
					this.sequences.put(songFile.getName(), MidiSystem.getSequence(songFile));
				} catch (InvalidMidiDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void loadZip(File sourceFile) {
		try {
			ZipFile sourceZip = new ZipFile(sourceFile);
			Enumeration<? extends ZipEntry> entries = sourceZip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!entry.isDirectory() && entry.getName().startsWith("midi/songs")) 
				{
					String songName = entry.getName().substring("midi/songs/".length());
					System.out.println("Found song: "+songName);
					this.sequences.put(songName, MidiSystem.getSequence(sourceZip.getInputStream(entry)));
				}
			}
			sourceZip.close();
		} catch (Exception ex) {
			System.out.println("Error finding song files: "+sourceFile.getAbsolutePath());
			System.out.println(ex);
		}
	}
}

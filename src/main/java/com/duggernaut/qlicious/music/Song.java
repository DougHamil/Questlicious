package com.duggernaut.qlicious.music;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import net.minecraft.entity.Entity;

import com.duggernaut.qlicious.Logger;
import com.duggernaut.qlicious.music.instruments.Instrument;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Song implements MetaEventListener {
	private static int songGUID = 0;

	private final int id;
	private Sequence sequence;
	private Sequencer sequencer;
	private SongSpells songSpell;
	private int songSpellId;
	private long tickPosition;
	private Map<Integer, Integer> entityToTrackIndex = Maps.newHashMap();
	private Map<Integer, Integer> entityToInstrument = Maps.newHashMap();
	private Map<MidiChannel, Integer> channelToTrack = Maps.newHashMap();
	private Multimap<Integer, MidiEvent> volumeEvents = ArrayListMultimap.create();
	private Multimap<Track, MidiEvent> trackEvents = ArrayListMultimap.create();
	
	private List<IndexedTrack> orderedTracks;
	private boolean isServerSide;

	public Song(int songSpellId, Sequence sequence) {
		this(songGUID++, songSpellId, sequence, 0, true);
	}

	public Song(int id, int songSpellId, Sequence sequence, long tickPosition, boolean isServerSide) 
	{
		this.id = id;
		this.sequence = sequence;
		this.songSpellId = songSpellId;
		this.tickPosition = tickPosition;
		this.isServerSide = isServerSide;
		this.songSpell = SongSpells.fromId(this.songSpellId);
		Track[] tracks = this.sequence.getTracks();
		this.orderedTracks = Lists.newArrayList();
		Set<Integer> claimedChannels = Sets.newHashSet();
		for (int i = 0; i < tracks.length; i++) {
			Track track = tracks[i];
			Integer channel = this.getChannelForTrack(track, claimedChannels);
			if(channel != null)
			{
				claimedChannels.add(channel);
				this.orderedTracks.add(new IndexedTrack(track, i, channel));
			}
		}
		// Order tracks by number of notes
		Collections.sort(this.orderedTracks, new Comparator<IndexedTrack>() {
			@Override
			public int compare(IndexedTrack arg0, IndexedTrack arg1) {
				return getNoteCount(arg1) - getNoteCount(arg0);
			}

			private int getNoteCount(IndexedTrack t) {
				return t.track.size();
			}
		});
		
		// Don't use percussion as leading track
		IndexedTrack firstTrack = this.orderedTracks.get(0);
		if(firstTrack.channel == 9)
		{
			this.orderedTracks.remove(0);
			this.orderedTracks.add(1, firstTrack);
		}
		

		for(IndexedTrack track : this.orderedTracks)
		{
			if(!this.isServerSide)
			{
				System.out.println(String.format("Track %d is on channel %d", track.trackIndex, track.channel));
				this.normalizeTrack(track.track, track.channel);
			}
		}
	}
	
	public void play()
	{
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequencer.getTransmitter().setReceiver(MusicSystem.synth.getReceiver());
			sequencer.setSequence(sequence);
			sequencer.setTickPosition(this.tickPosition);
			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			
			Track[] tracks = this.sequence.getTracks();
			for(int i = 0; i < tracks.length; i++)
			{
				sequencer.setTrackMute(i, true);
				sequencer.setTrackSolo(i, false);
			}
			
			// Play all of the active tracks
			for(Map.Entry<Integer, Integer> entry : this.entityToTrackIndex.entrySet())
			{
				this.setupEntityTrack(entry.getKey());
				IndexedTrack indexedTrack = this.orderedTracks.get(entry.getValue());
				sequencer.setTrackMute(indexedTrack.trackIndex, false);
				sequencer.setTrackSolo(indexedTrack.trackIndex, true);
			}
			
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

	}

	public void stopAll() {
		if (this.sequencer != null) {
			this.sequencer.stop();
			this.sequencer.close();
			this.sequencer = null;
		}
	}

	private Integer getChannelForTrack(Track t, Set<Integer> claimedChannels)
	{
		for (int i = 0; i < t.size(); i++) {
			MidiEvent event = t.get(i);
			MidiMessage msg = event.getMessage();
			if (msg instanceof ShortMessage) {
				ShortMessage message = (ShortMessage) msg;
				if(message.getCommand() == ShortMessage.NOTE_ON && !claimedChannels.contains(message.getChannel()))
					return message.getChannel();
			}
		}
		return null;
	}
	
	private void normalizeTrack(Track t, int channelId) {
		List<MidiEvent> eventsToRemove = Lists.newArrayList();
		for (int i = 0; i < t.size(); i++) {
			MidiEvent event = t.get(i);
			MidiMessage msg = event.getMessage();
			if (msg instanceof ShortMessage) {
				ShortMessage message = (ShortMessage) msg;
				if (message.getCommand() == ShortMessage.PROGRAM_CHANGE 
						|| (message.getCommand() == ShortMessage.CONTROL_CHANGE && (message.getData1() == 7 || message.getData1() == 0))) {
					//eventsToRemove.add(event);
				}
			}
		}
		for(MidiEvent e : eventsToRemove)
			t.remove(e);
		
		// Insert full volume event
		try {
			MidiEvent volEvent = new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channelId, 7, 127), 0);
			t.add(volEvent);
		} catch (InvalidMidiDataException e1) {
			e1.printStackTrace();
		}
	}

	public Integer getInstrumentIdForEntity(Entity entity) {
		return this.entityToInstrument.get(entity.getEntityId());
	}

	public Integer getTrackIndexForEntity(Entity entity) {
		return this.entityToTrackIndex.get(entity.getEntityId());
	}

	// Used for client-side where track ID is already selected
	public boolean addEntity(Entity entity, int instrumentId, int trackIndex) {
		this.entityToTrackIndex.put(entity.getEntityId(), trackIndex);
		this.entityToInstrument.put(entity.getEntityId(), instrumentId);
		return true;
	}

	public boolean addEntity(Entity entity, int instrumentId) {
		Collection<Integer> usedTrackIndices = entityToTrackIndex.values();
		Integer nextId = null;
		
		for (int i = 0; i < this.orderedTracks.size(); i++)
		{
			if (!usedTrackIndices.contains(i)) {
				nextId = i;
				break;
			}
		}
		
		if (nextId == null)
			return false;

		return this.addEntity(entity, instrumentId, nextId);
	}

	public void stopEntity(Entity entity) {
		Integer trackIndex = this.entityToTrackIndex.remove(entity.getEntityId());
		if (trackIndex != null && this.sequencer != null) {
			sequencer.setTrackMute(this.orderedTracks.get(trackIndex).trackIndex, true);
			sequencer.setTrackSolo(this.orderedTracks.get(trackIndex).trackIndex, false);
			if (this.entityToTrackIndex.isEmpty())
				sequencer.stop();
		}
	}
	
	private void setupEntityTrack(Integer entityId)
	{
		int trackIndex = this.entityToTrackIndex.get(entityId);
		int instrumentId = this.entityToInstrument.get(entityId);
		Instrument instrument = Instrument.instrumentsById.get(instrumentId);
		int instrumentProgramId = MusicSystem.instrumentByName.get(instrument.getInstrumentName()).getPatch().getProgram();
		IndexedTrack indexedTrack = this.orderedTracks.get(trackIndex);
		this.clearTrackEvents(indexedTrack.track);
		
		try {
			MidiEvent instrumentChangeEvent = new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, indexedTrack.channel, instrumentProgramId, 0), this.getTickPosition());
			this.addTrackEvent(indexedTrack.track, instrumentChangeEvent);
			instrumentChangeEvent = new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, indexedTrack.channel, instrumentProgramId, 0), this.getTickPosition()-1);
			this.addTrackEvent(indexedTrack.track, instrumentChangeEvent);
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		Logger.log(String.format(
				"Client - track %d (channel %d) is being played with instrument %s (program %d) by entity %d",
				indexedTrack.trackIndex, indexedTrack.channel, instrument.getInstrumentName(), instrumentProgramId, entityId));
	}
	
	public void playEntity(Entity entity)
	{
		int trackIndex = this.entityToTrackIndex.get(entity.getEntityId());
		try {
			this.setupEntityTrack(entity.getEntityId());
			IndexedTrack indexedTrack = this.orderedTracks.get(trackIndex);
			if(this.sequencer != null)
			{
				sequencer.setTrackMute(indexedTrack.trackIndex, false);
				sequencer.setTrackSolo(indexedTrack.trackIndex, true);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public long getTickPosition()
	{
		if(this.sequencer != null)
			return this.sequencer.getTickPosition();
		return this.tickPosition;
	}
	
	private void addTrackEvent(Track t, MidiEvent e)
	{
		t.add(e);
		this.trackEvents.put(t, e);
	}

	private void clearTrackEvents(Track t)
	{
		Collection<MidiEvent> events = this.trackEvents.get(t);
		long minTick = this.getTickPosition();
		if(events != null)
		{
			List<MidiEvent> eventsToRemove = Lists.newArrayList();
			for(MidiEvent e : events)
				if(e.getTick() < minTick)
					eventsToRemove.add(e);
			for(MidiEvent e : eventsToRemove)
				t.remove(e);
		}
	}

	public void setEntityVolume(Entity entity, float volume) {
		try {
			if (this.sequencer != null) {
				Integer channelId = this.entityToTrackIndex.get(entity.getEntityId());
				if (channelId != null) {
					IndexedTrack indexedTrack = this.orderedTracks.get(channelId);
					Track track = indexedTrack.track;
					try {
						MidiEvent volEvent = new MidiEvent(
								new ShortMessage(
										ShortMessage.CONTROL_CHANGE,
										indexedTrack.channel,
										7, (int) (volume * 127f)),
								this.getTickPosition());
						this.addTrackEvent(track,  volEvent);
						//System.out.println("Set volume to "+volume);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
		}// consume
	}

	public String getSongSpellName()
	{
		return this.songSpell.getName();
	}
	
	public boolean isEmpty()
	{
		return this.entityToTrackIndex.isEmpty();
	}
	
	public int getSongSpellId() {
		return this.songSpellId;
	}

	public int getId() {
		return this.id;
	}

	private static class IndexedTrack {
		public int channel;
		public int trackIndex;
		public Track track;

		public IndexedTrack(Track t, int trackIndex, int channelId) {
			this.track = t;
			this.channel = channelId;
			this.trackIndex = trackIndex;
		}
	}

	@Override
	public void meta(MetaMessage msg) {
		if(msg.getType() == 47) // End of stream
		{
			System.out.println("SONG OVER");
			for(IndexedTrack c : this.orderedTracks)
			{
				this.normalizeTrack(c.track, c.channel);
			}
		}
	}
}

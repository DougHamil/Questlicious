package com.duggernaut.qlicious.music.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import com.duggernaut.qlicious.QliciousMod;
import com.duggernaut.qlicious.music.MusicSystem;
import com.duggernaut.qlicious.music.Song;
import com.duggernaut.qlicious.music.SongSpell;
import com.duggernaut.qlicious.music.SongSystem;
import com.duggernaut.qlicious.music.instruments.Instrument;
import com.duggernaut.qlicious.net.AbstractPacket;

/**
 * Packet sent to server when a player uses an instrument
 * @author Doug
 */
public class PlayInstrumentPacket extends AbstractPacket
{
	public static final byte PLAY_COMMAND = 1;
	public static final byte STOP_COMMAND = 2;
	private int entityId;
	private int channelId;
	private int instrumentId;
	private int songId;
	private byte command;
	
	public PlayInstrumentPacket(){}
	public PlayInstrumentPacket(int entityId, int songId, int channelId, int instrumentId, byte command)
	{
		this.entityId = entityId;
		this.songId = songId;
		this.channelId = channelId;
		this.instrumentId = instrumentId;
		this.command = command;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeInt(songId);
		buffer.writeByte((byte)this.channelId);
		buffer.writeByte((byte)this.instrumentId);
		buffer.writeByte(this.command);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		this.entityId = buffer.readInt();
		this.songId = buffer.readInt();
		this.channelId = (int)buffer.readByte();
		this.instrumentId = (int)buffer.readByte();
		this.command = buffer.readByte();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		Entity entity = player.worldObj.getEntityByID(this.entityId);
		if(this.command == PLAY_COMMAND)
		{
			Song song = SongSystem.client.getSongById(this.songId);
			SongSpell spell = SongSystem.client.getSongSpellById(song.getId());
			song.addEntity(entity, this.instrumentId, this.channelId);
			if(spell != null)
				spell.onEntityJoined(entity, Instrument.instrumentsById.get(instrumentId));
			MusicSystem.client.setEntitySong(entity, song);
			song.playEntity(entity);
			if(entity == player || MusicSystem.client.getActiveSong() == null)
			{
				MusicSystem.client.setActiveSong(song);
			}
		}
		else if(this.command == STOP_COMMAND)
		{
			Song song = MusicSystem.client.getSongForEntity(entity);
			if(song != null)
			{
				SongSpell spell = SongSystem.client.getSongSpellById(song.getId());
				song.stopEntity(entity);
				MusicSystem.client.removeEntityFromSong(entity);
				
				if(spell != null)
				{
					spell.onEntityLeft(entity);
					if(song.isEmpty())
						spell.onSongStopped();
				}
			}
		}
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		Entity entity = player.worldObj.getEntityByID(this.entityId);
		
		if(this.command == PLAY_COMMAND)
		{
			Song song = MusicSystem.server.findNearbySong(entity);
			if(song == null)
				song = SongSystem.server.initializeRandomSong();
			
			// Add the player to the song
			if(song.addEntity(entity,  instrumentId))
			{
				MusicSystem.server.setEntitySong(entity, song);
				SongSpell spell = SongSystem.server.getSongSpellById(song.getId());
				if(spell != null)
					spell.onEntityJoined(entity, Instrument.instrumentsById.get(instrumentId));
				
				// Tell the world that this player is now playing the song
				AbstractPacket packet = new PlayInstrumentPacket(entity.getEntityId(), song.getId(), song.getTrackIndexForEntity(entity), instrumentId, PlayInstrumentPacket.PLAY_COMMAND);
				QliciousMod.packetPipeline.sendToAll(packet);
			}
		}
		else if(this.command == STOP_COMMAND)
		{
			Song song = MusicSystem.server.getSongForEntity(entity);
			if(song != null)
			{
				SongSpell spell = SongSystem.server.getSongSpellById(song.getId());
				song.stopEntity(entity);
				MusicSystem.server.removeEntityFromSong(entity);
				if(spell != null)
				{
					spell.onEntityLeft(entity);
					if(song.isEmpty())
						spell.onSongStopped();
				}
				
				// Tell the world
				AbstractPacket packet = new PlayInstrumentPacket(entity.getEntityId(), song.getId(), -1, -1, PlayInstrumentPacket.STOP_COMMAND);
				QliciousMod.packetPipeline.sendToAll(packet);
				
				// Unregister the song if it's empty
				if(song.isEmpty())
				{
					SongSystem.server.unregisterSong(song);
					AbstractPacket songPacket = new RegisterSongPacket(song, true);
					QliciousMod.packetPipeline.sendToAll(songPacket);
				}
			}
		}
	}
}

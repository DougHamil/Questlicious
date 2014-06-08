package com.duggernaut.qlicious.music.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import com.duggernaut.qlicious.Logger;
import com.duggernaut.qlicious.QliciousMod;
import com.duggernaut.qlicious.music.MusicSystem;
import com.duggernaut.qlicious.music.Song;
import com.duggernaut.qlicious.music.SongSpell;
import com.duggernaut.qlicious.music.SongSystem;
import com.duggernaut.qlicious.music.instruments.Instrument;
import com.duggernaut.qlicious.net.AbstractPacket;

public class CastSongSpellPacket extends AbstractPacket
{
	private int entityId;
	private int instrumentId;
	private int songSpellId;
	
	public CastSongSpellPacket(){}
	public CastSongSpellPacket(int entityId, int instrumentid, int spellId)
	{
		this.entityId = entityId;
		this.instrumentId = instrumentId;
		this.songSpellId = spellId;
	}
	
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeInt(this.instrumentId);
		buffer.writeInt(this.songSpellId);
		
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		this.entityId = buffer.readInt();
		this.instrumentId = buffer.readInt();
		this.songSpellId = buffer.readInt();
		
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		
		
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		Logger.log(String.format("Cast spell packet received on server with spell %d", this.songSpellId));
		Entity entity = player.worldObj.getEntityByID(this.entityId);
		
		// Initialize song with the requested spell
		Song song = SongSystem.server.initializeNewSong(this.songSpellId);
		song.addEntity(entity, instrumentId);
		MusicSystem.server.setEntitySong(entity, song);
		SongSpell spell = SongSystem.server.getSongSpellById(song.getId());
		if(spell != null)
			spell.onEntityJoined(entity, Instrument.instrumentsById.get(instrumentId));
		
		// Tell the world that this player is now playing the song
		AbstractPacket packet = new PlayInstrumentPacket(entity.getEntityId(), song.getId(), song.getTrackIndexForEntity(entity), instrumentId, PlayInstrumentPacket.PLAY_COMMAND);
		QliciousMod.packetPipeline.sendToAll(packet);
	}

}

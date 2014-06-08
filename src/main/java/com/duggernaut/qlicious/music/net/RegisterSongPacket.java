package com.duggernaut.qlicious.music.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

import com.duggernaut.qlicious.music.MIDIBank;
import com.duggernaut.qlicious.music.Song;
import com.duggernaut.qlicious.music.SongSpells;
import com.duggernaut.qlicious.music.SongSystem;
import com.duggernaut.qlicious.net.AbstractPacket;

import cpw.mods.fml.common.network.ByteBufUtils;

public class RegisterSongPacket extends AbstractPacket
{
	private int songId;
	private int songSpellId;
	private boolean isUnregister;
	private long tickPosition;

	public RegisterSongPacket() {}
	public RegisterSongPacket(Song song, boolean isUnregister)
	{
		this.songId = song.getId();
		this.songSpellId = song.getSongSpellId();
		this.isUnregister = isUnregister;
		this.tickPosition = song.getTickPosition();
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(songId);
		buffer.writeInt(songSpellId);
		buffer.writeBoolean(this.isUnregister);
		buffer.writeLong(this.tickPosition);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		songId = buffer.readInt();
		songSpellId = buffer.readInt();
		isUnregister = buffer.readBoolean();
		tickPosition = buffer.readLong();
	}

	// Called when a song was registered to the server's song system, so register it to ours
	@Override
	public void handleClientSide(EntityPlayer player) {
		if(this.isUnregister)
		{
			SongSystem.client.unregisterSong(songId);
		}
		else
		{
			SongSpells spell = SongSpells.fromId(this.songSpellId);
			Song song = new Song(this.songId, spell.getId(), MIDIBank.instance.getSequenceForSong(spell.getMidiFilename()), this.tickPosition, false);
			SongSystem.client.registerSong(song);
		}
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		// 
	}

}

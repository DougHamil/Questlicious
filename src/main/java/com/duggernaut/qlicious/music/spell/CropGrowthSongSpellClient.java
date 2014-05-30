package com.duggernaut.qlicious.music.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import com.duggernaut.qlicious.music.Song;
import com.duggernaut.qlicious.music.SongSpell;
import com.duggernaut.qlicious.music.instruments.Instrument;

public class CropGrowthSongSpellClient extends SongSpell
{
	public CropGrowthSongSpellClient(Song song, EntityPlayer clientPlayer) {
		super(song, clientPlayer);
	}

	@Override
	public void onEntityJoined(Entity entity, Instrument inst)
	{
		System.out.println("Client side - entity joined spell!");
	}
}

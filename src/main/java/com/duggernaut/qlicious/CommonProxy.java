package com.duggernaut.qlicious;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.duggernaut.qlicious.editor.SchematicContainerTileEntity;
import com.duggernaut.qlicious.music.ServerMusicEventHandler;
import com.duggernaut.qlicious.music.Song;
import com.duggernaut.qlicious.music.SongSpell;
import com.duggernaut.qlicious.music.SongSpells;
import com.duggernaut.qlicious.music.instruments.Instrument;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	public static int SCHEMATIC_GUI_ID = 20;
	public static int SELECT_SONG_SPELL_GUI_ID = 21;
	
	public static Instrument activeInstrument;
	public static SchematicContainerTileEntity schematicContainer;
	public static int dimension;
	
	public EntityPlayer getClientPlayer()
	{
		return null;
	}
	
	public boolean isClientSide()
	{
		return false;
	}
	
	public SongSpell createSongSpell(Song song)
	{
		return SongSpells.instantiateForSong(song, null);
	}
	
	public boolean isEntityPlayer(World world, Entity entity)
	{
		return false;
	}
	
	public void registerHandlers()
	{
		FMLCommonHandler.instance().bus().register(new ServerMusicEventHandler());
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}
}

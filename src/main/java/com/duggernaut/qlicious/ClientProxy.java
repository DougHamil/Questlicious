package com.duggernaut.qlicious;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import com.duggernaut.qlicious.editor.SchematicContainerGUI;
import com.duggernaut.qlicious.music.ClientMusicEventHandler;
import com.duggernaut.qlicious.music.Song;
import com.duggernaut.qlicious.music.SongSpell;
import com.duggernaut.qlicious.music.SongSpells;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{
	@Override
	public SongSpell createSongSpell(Song song)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().equals(Side.CLIENT))
			return SongSpells.instantiateForSong(song, this.getClientPlayer());
		else
			return SongSpells.instantiateForSong(song,  null);
	}
	
	@Override
	public boolean isClientSide() {
		return FMLCommonHandler.instance().getEffectiveSide().equals(Side.CLIENT);
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public boolean isEntityPlayer(World world, Entity entity) {
		return entity.equals(Minecraft.getMinecraft().thePlayer);
	}

	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
		FMLCommonHandler.instance().bus().register(new ClientMusicEventHandler());
		FMLCommonHandler.instance().bus().register(new DebugKeyHandler());
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		
		if(ID == SCHEMATIC_GUI_ID)
			return new SchematicContainerGUI();
		// TODO Auto-generated method stub
		return null;
	}
}

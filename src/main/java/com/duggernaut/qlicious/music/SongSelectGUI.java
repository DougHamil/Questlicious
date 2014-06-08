package com.duggernaut.qlicious.music;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.duggernaut.qlicious.CommonProxy;
import com.duggernaut.qlicious.Logger;
import com.duggernaut.qlicious.QliciousMod;
import com.duggernaut.qlicious.music.net.CastSongSpellPacket;
import com.duggernaut.qlicious.music.net.PlayInstrumentPacket;
import com.duggernaut.qlicious.net.AbstractPacket;
import com.google.common.collect.Maps;

public class SongSelectGUI extends GuiScreen
{
	private Integer selectedSongId = null;
	
	private Map<Integer, Integer> buttonIdToSongSpellId = Maps.newHashMap();
	
	public Integer getSelectedSongId()
	{
		return this.selectedSongId;
	}
	
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		this.buttonIdToSongSpellId.clear();
		buttonList.clear();
		int btnId = 0;
		for(SongSpells spell : SongSpells.values())
		{
			GuiButton btn = new GuiButton(btnId, 5, 100, spell.getName());
			buttonIdToSongSpellId.put(btnId++, spell.getId());
			buttonList.add(btn);
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton btn)
	{
		Integer songSpellId = this.buttonIdToSongSpellId.get(btn.id);
		SongSpells spell = SongSpells.fromId(songSpellId);

		AbstractPacket packet = new CastSongSpellPacket(Minecraft.getMinecraft().thePlayer.getEntityId(), CommonProxy.activeInstrument.getInstrumentId(), songSpellId);
		QliciousMod.packetPipeline.sendToServer(packet);
		Logger.log("Casting song spell "+spell.getName());
	}
}

package com.duggernaut.qlicious.music.instruments;

import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.duggernaut.qlicious.QliciousMod;
import com.duggernaut.qlicious.music.net.PlayInstrumentPacket;
import com.duggernaut.qlicious.net.AbstractPacket;
import com.google.common.collect.Maps;

public class Instrument extends Item
{
	public static final Map<Integer, Instrument> instrumentsById = Maps.newHashMap();
	private int instrumentId;
	private String instrumentName;
	private static Instrument activeInstrument;
	private boolean isHeld;
	public boolean isPercussive;
	
	public Instrument(String name, int id, boolean isPercussive)
	{
		super();
		this.isHeld = false;
		this.instrumentId = id;
		this.instrumentName = name;
		this.isPercussive = isPercussive;
		setCreativeTab(CreativeTabs.tabMisc);
		setMaxStackSize(1);
		instrumentsById.put(id, this);
	}
	
	public String getInstrumentName()
	{
		return this.instrumentName;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World,
			EntityPlayer player) {
		if(QliciousMod.proxy.getClientPlayer() != null)
		{
			if(activeInstrument != this)
			{
				AbstractPacket packet = new PlayInstrumentPacket(player.getEntityId(), 0, 0, this.instrumentId, PlayInstrumentPacket.PLAY_COMMAND);
				QliciousMod.packetPipeline.sendToServer(packet);
				activeInstrument = this;
			}
		}
    	return par1ItemStack;
	}

	@Override
	public void onUpdate(ItemStack stack, World world,
			Entity entity, int par4, boolean par5) {
		EntityPlayer player = QliciousMod.proxy.getClientPlayer();
		if(player != null && entity.equals(player))
		{
			ItemStack heldItemStack = player.getHeldItem();
			boolean currentlyHeld = false;
			if(heldItemStack != null)
			{
				Item heldItem = heldItemStack.getItem();
				if(heldItem.equals(this))
				{
					currentlyHeld = true;
				}
			}
			
			if(this.isHeld && !currentlyHeld)
			{
				if(activeInstrument == this)
				{
					activeInstrument = null;
					AbstractPacket packet = new PlayInstrumentPacket(player.getEntityId(), 0, 0, this.instrumentId, PlayInstrumentPacket.STOP_COMMAND);
					QliciousMod.packetPipeline.sendToServer(packet);
				}
			}
			
			this.isHeld = currentlyHeld;
		}
		super.onUpdate(stack, world, entity, par4, par5);
	}

	@Override
	public boolean canItemEditBlocks() {
		return false;
	}
}

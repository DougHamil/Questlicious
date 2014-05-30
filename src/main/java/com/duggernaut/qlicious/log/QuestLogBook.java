package com.duggernaut.qlicious.log;

import com.duggernaut.qlicious.QliciousMod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class QuestLogBook extends Item
{
	public QuestLogBook()
	{
		super();
		setUnlocalizedName("questLogBook");
		setCreativeTab(CreativeTabs.tabMisc);
		setMaxStackSize(1);
		setTextureName("qlicious:questLogBook");
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack,
			EntityPlayer par2EntityPlayer, World par3World, int par4, int par5,
			int par6, int par7, float par8, float par9, float par10) {
		
		//if(par3World.isRemote)
		//	par2EntityPlayer.openGui(QliciousMod.instance, QuestLogBookGUI.ID, par3World, par4, par6, par6);
		return true;
	}

	@Override
	public boolean canItemEditBlocks() {
		return false;
	}
}

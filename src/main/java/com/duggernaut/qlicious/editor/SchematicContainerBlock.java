package com.duggernaut.qlicious.editor;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.duggernaut.qlicious.CommonProxy;
import com.duggernaut.qlicious.QliciousMod;

public class SchematicContainerBlock extends BlockContainer
{
	public SchematicContainerBlock()
	{
		super(Material.wood);
		setHardness(2.0F);
		setResistance(5.0F);
		setBlockName("schematicContainer");
		setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
			int meta, float p_149727_7_, float p_149727_8_,	float p_149727_9_)
	{
		TileEntity tileEntity = world.getTileEntity(x,  y,  z);
		if(tileEntity == null || player.isSneaking() || !(tileEntity instanceof SchematicContainerTileEntity))
			return false;
		CommonProxy.schematicContainer = (SchematicContainerTileEntity)tileEntity;
		CommonProxy.dimension = player.dimension;
		player.openGui(QliciousMod.instance, CommonProxy.SCHEMATIC_GUI_ID, world, x, y, z);
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		SchematicContainerTileEntity te = new SchematicContainerTileEntity();
		return te;
	}

	@Override
	public void onBlockAdded(World w, int x,
			int y, int z) {
		// TODO Auto-generated method stub
		super.onBlockAdded(w, x, y, z);
		SchematicContainerWorldSaveData.forWorld(w).addSchematicContainer(w, (SchematicContainerTileEntity)w.getTileEntity(x, y, z));
		
	}
}

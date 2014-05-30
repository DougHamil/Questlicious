package com.duggernaut.qlicious.editor;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;

import com.duggernaut.qlicious.Schematic;
import com.duggernaut.qlicious.Schematics;

public class SchematicContainerTileEntity extends TileEntity
{
	private String schematicName;
	
	public SchematicContainerTileEntity()
	{
		this.schematicName = "MySchematic";
	}
	
	public void setSchematicName(String name)
	{
		this.schematicName = name;
	}
	
	public String getSchematicName()
	{
		return this.schematicName;
	}
	
	// Called on the server to build a schematic file from this container entity
	public void saveSchematic(int dimension)
	{
		List<SchematicContainerTileEntity> instances = SchematicContainerWorldSaveData.forWorld(this.worldObj).getList(this.worldObj);
		// Find other end
		for(SchematicContainerTileEntity sc : instances)
		{
			if(sc.schematicName.equals(this.schematicName) && sc != this)
			{
				int[] start = new int[3];
				int[] end = new int[3];
				
				start[0] = this.xCoord;
				start[1] = this.yCoord;
				start[2] = this.zCoord;
				end[0] = sc.xCoord;
				end[1] = sc.yCoord;
				end[2] = sc.zCoord;
				
				System.out.println(String.format("%s Coords: %d, %d, %d -> %d, %d, %d",this.schematicName, start[0], start[1], start[2], end[0], end[1], end[2]));
				Schematic schematic = Schematics.fromWorld(this.worldObj, dimension, start, end);
				schematic.save("C:/Users/Doug/Desktop/"+sc.getSchematicName()+".schematic");
				break;
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		System.out.println("Writing tag: "+this.schematicName);
		tag.setString("schematicName", this.schematicName);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		this.schematicName = tag.getString("schematicName");
		System.out.println("Reading tag: "+this.schematicName);
	}
	
	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		this.readFromNBT(pkt.func_148857_g());
	}
}

package com.duggernaut.qlicious.editor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import com.duggernaut.qlicious.QliciousMod;
import com.duggernaut.qlicious.net.AbstractPacket;

import cpw.mods.fml.common.network.ByteBufUtils;

public class SchematicContainerPacket extends AbstractPacket
{
	private int x;
	private int y;
	private int z;
	private String schematicName;
	
	public SchematicContainerPacket() {}
	public SchematicContainerPacket(SchematicContainerTileEntity tileEntity, String name)
	{
		this.x = tileEntity.xCoord;
		this.y = tileEntity.yCoord;
		this.z = tileEntity.zCoord;
		this.schematicName = name;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		ByteBufUtils.writeUTF8String(buffer, this.schematicName);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		schematicName = ByteBufUtils.readUTF8String(buffer);
	}
	
	@Override
	public void handleClientSide(EntityPlayer player) {
		// TODO Auto-generated method stub
		TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
		if(tileEntity instanceof SchematicContainerTileEntity)
		{
			((SchematicContainerTileEntity)tileEntity).setSchematicName(this.schematicName);
		}
	}
	
	@Override
	public void handleServerSide(EntityPlayer player) {
		// TODO Auto-generated method stub
		TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
		if(tileEntity instanceof SchematicContainerTileEntity)
		{
			SchematicContainerTileEntity container = (SchematicContainerTileEntity)tileEntity;
			container.setSchematicName(this.schematicName);
			container.saveSchematic(player.dimension);
		}
		
		QliciousMod.packetPipeline.sendToAll(this);
	}
}

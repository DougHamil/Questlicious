package com.duggernaut.qlicious.music.spell;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;

import com.duggernaut.qlicious.music.Song;
import com.duggernaut.qlicious.music.SongSpell;
import com.duggernaut.qlicious.music.instruments.Instrument;
import com.google.common.collect.ImmutableSet;

public class CropGrowthSongSpellServer extends SongSpell
{
	private static final int BASE_RADIUS = 2;	// How many blocks away from the player will be affected?
	private static final int RADIUS_INCREASE_PER_PLAYER = 2;  // Linear increase based on number of entities playing song
	private static final int MILLISECONDS_PER_METADATA_INCREASE = 5000; // 3 seconds per meta-data tick for crops
	
	private static final ImmutableSet<Class> PLANT_BLOCKS = ImmutableSet.<Class>builder()
			.add(Blocks.wheat.getClass())
			.add(Blocks.potatoes.getClass())
			.add(Blocks.reeds.getClass())
			.add(lotr.common.block.LOTRBlockLettuceCrop.class)
			.add(lotr.common.block.LOTRBlockPipeweedCrop.class)
			.build();
	
	private Entity initialEntity;
	private int numEntitiesPlayingSong = 0;
	private int lastTick = 0;
	
	public CropGrowthSongSpellServer(Song song, EntityPlayer clientPlayer) {
		super(song, clientPlayer);
	}
	
	@Override
	public void onEntityJoined(Entity entity, Instrument inst)
	{
		System.out.println("Server side - entity joined spell!");
		if(this.initialEntity == null)
			this.initialEntity = entity;
		this.numEntitiesPlayingSong++;
	}
	
	@Override
	public void onEntityLeft(Entity entity) {
		if(this.initialEntity == entity)
			this.initialEntity = null;
		this.numEntitiesPlayingSong--;
	}

	@Override
	public void onSongTick(long elapsedSongTimeMillis) {
		int tick = (int)elapsedSongTimeMillis / MILLISECONDS_PER_METADATA_INCREASE;
		
		if(tick > lastTick)
		{
			//System.out.println("Server tick: Meta level is "+metaDataLevel);
			if(this.initialEntity != null && !this.initialEntity.isDead)
			{
				int radius = BASE_RADIUS + RADIUS_INCREASE_PER_PLAYER * (this.numEntitiesPlayingSong-1);
				int pX = (int) Math.floor(this.initialEntity.posX);
				int pY = (int) Math.floor(this.initialEntity.posY);
				int pZ = (int) Math.floor(this.initialEntity.posZ);
				int minX = pX - radius;
				int minY = pY - radius;
				int minZ = pZ - radius;
				int maxX = pX + radius;
				int maxY = pY + radius;
				int maxZ = pZ + radius;
				for(int x = minX; x <= maxX; x++)
				{
					for(int y = minY; y <= maxY; y++)
					{
						for(int z = minZ; z <= maxZ; z++)
						{
							Block block = this.initialEntity.worldObj.getBlock(x, y, z);
							if(this.PLANT_BLOCKS.contains(block.getClass()))
							{
								int oldMetaData = this.initialEntity.worldObj.getBlockMetadata(x,  y,  z);
								if(oldMetaData < 15)
									this.initialEntity.worldObj.setBlockMetadataWithNotify(x, y , z, oldMetaData+1, 2);
							}
						}
					}
				}
			}
		}
		lastTick = tick;
	}
}

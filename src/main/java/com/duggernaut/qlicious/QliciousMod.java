package com.duggernaut.qlicious;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;

import com.duggernaut.qlicious.editor.SchematicContainerBlock;
import com.duggernaut.qlicious.editor.SchematicContainerPacket;
import com.duggernaut.qlicious.editor.SchematicContainerTileEntity;
import com.duggernaut.qlicious.log.QuestLogBook;
import com.duggernaut.qlicious.music.MusicSystem;
import com.duggernaut.qlicious.net.PacketPipeline;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/***
 * Main mod file for Questlicious mod enhancing the LOTR mod.
 * 
 * @author Doug
 */
@Mod(modid = QliciousMod.MODID, version = QliciousMod.VERSION)
public class QliciousMod {
	public static final String MODID = "questlicious";
	public static final String VERSION = "1.0";
	public static final int MIDDLE_EARTH_DIMENSION_ID = 100;
	
	@Instance("questlicious")
	public static QliciousMod instance;
	
	public static final PacketPipeline packetPipeline = new PacketPipeline();
	
	@SidedProxy(clientSide="com.duggernaut.qlicious.ClientProxy", serverSide="com.duggernaut.qlicious.ServerProxy")
	public static CommonProxy proxy;
	
	// Blocks
	private static final SchematicContainerBlock schematicContainerBlock = new SchematicContainerBlock();
	// Items
	private static final QuestLogBook questLogBook = new QuestLogBook();


	private File sourceFile;

	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	sourceFile = event.getSourceFile();
    	// Schematics
    	GameRegistry.registerBlock(this.schematicContainerBlock, "schematicContainer");
    	GameRegistry.registerTileEntity(SchematicContainerTileEntity.class,  "schematicContainer");
    	// Items
    	GameRegistry.registerItem(questLogBook, "questLogBook");
    	
    	// Register packets
    	packetPipeline.registerPacket(SchematicContainerPacket.class);
    	
    	// Music
    	MusicSystem.initialize(sourceFile);
    }

	@EventHandler
	public void init(FMLInitializationEvent event) {

		packetPipeline.initialise();
    	MinecraftForge.EVENT_BUS.register(new WorldLoadHandler(this.sourceFile));
    	NetworkRegistry.INSTANCE.registerGuiHandler(instance,  proxy);
    	this.proxy.registerHandlers();
	}
	
	@EventHandler
	public void postInitialize(FMLPostInitializationEvent event)
	{
		packetPipeline.postInitialise();
	}
}

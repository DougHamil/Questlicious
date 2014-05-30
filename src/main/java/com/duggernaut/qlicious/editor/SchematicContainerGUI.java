package com.duggernaut.qlicious.editor;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import com.duggernaut.qlicious.CommonProxy;
import com.duggernaut.qlicious.QliciousMod;

public class SchematicContainerGUI extends GuiScreen
{
	private String schematicName;
	private GuiTextField schematicNameTextField;
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		this.schematicName = this.schematicNameTextField.getText();
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		this.schematicNameTextField.drawTextBox();
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		super.keyTyped(par1, par2);
		this.schematicNameTextField.textboxKeyTyped(par1,  par2);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.schematicNameTextField.mouseClicked(par1, par2, par3);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.schematicName = CommonProxy.schematicContainer.getSchematicName();
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(1, width / 2 + 2, height /2 + 20, 98, 20, "Save"));
		this.schematicNameTextField = new GuiTextField(this.fontRendererObj, 20, 75, 170, 20);
		this.schematicNameTextField.setFocused(false);
		this.schematicNameTextField.setMaxStringLength(100);
		this.schematicNameTextField.setText(this.schematicName);
	}

	@Override
	protected void actionPerformed(GuiButton btn) {
		if(btn.id == 1)
		{
			this.schematicName = this.schematicNameTextField.getText();
			QliciousMod.packetPipeline.sendToServer(new SchematicContainerPacket(CommonProxy.schematicContainer, this.schematicName));
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}

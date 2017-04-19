package zeldaswordskills.client.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;

public class GuiConfigZeldaSwordSkills extends GuiConfig {

	public GuiConfigZeldaSwordSkills(GuiScreen parentScreen){
		super(parentScreen, getCategoryElements(), ModInfo.ID, false, false, "Zelda Sword Skills at the Tip of a Sword");
	}
	
	@Override
	public void initGui(){
		super.initGui();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
	}
	
	@Override
	protected void actionPerformed(GuiButton button){
		super.actionPerformed(button);
	}
	
	private static List<IConfigElement> getCategoryElements(){
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		
		List<IConfigElement> general = new ConfigElement(Config.config.getCategory("general")).getChildElements();
		List<IConfigElement> client = new ConfigElement(Config.config.getCategory("client")).getChildElements();
		List<IConfigElement> magicMeter = new ConfigElement(Config.config.getCategory("Magic Meter")).getChildElements();
		
		
		list.add(new DummyConfigElement.DummyCategoryElement("General", "General setting for Zelda Sword Skills.", general));
		list.add(new DummyConfigElement.DummyCategoryElement("Client", Config.config.getCategory("client").getComment(), client));
		list.add(new DummyConfigElement.DummyCategoryElement("Magic Meter", Config.config.getCategory("Magic Meter").getComment(), magicMeter));
		list.add(new DummyConfigElement("BattleGear2 Swords", false, null, "[BattleGear2] Allow Master Swords to be held in the off-hand"));
		return list;
	}
	
}

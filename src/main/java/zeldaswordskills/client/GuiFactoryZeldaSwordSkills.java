package zeldaswordskills.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import zeldaswordskills.client.config.GuiConfigZeldaSwordSkills;

public class GuiFactoryZeldaSwordSkills implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {return GuiConfigZeldaSwordSkills.class;}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {return null;}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {return null;}

}

package tschipp.callablehorses.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.config.CallableHorsesConfig;

public class GuiConfigCallableHorses extends GuiConfig
{
    private static final String LANG_PREFIX = CallableHorses.MODID + ".category.";

    public GuiConfigCallableHorses(GuiScreen parent) {
        super(parent, getConfigElements(), CallableHorses.MODID, false, false, "Callable Horses Configuration");
    }

    private static List<IConfigElement> getConfigElements() {

        final Configuration configuration = CallableHorsesConfig.EventHandler.getConfiguration();

        final ConfigCategory topLevelCategory = configuration.getCategory(Configuration.CATEGORY_GENERAL);
        topLevelCategory.getChildren()
                .forEach(configCategory -> configCategory.setLanguageKey(GuiConfigCallableHorses.LANG_PREFIX + configCategory.getName()));

		return new ConfigElement(topLevelCategory).getChildElements();
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }
}

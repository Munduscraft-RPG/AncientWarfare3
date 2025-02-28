package xyz.dylanlogan.ancientwarfare.automation.gui;

import xyz.dylanlogan.ancientwarfare.automation.container.ContainerWorksiteFishControl;
import xyz.dylanlogan.ancientwarfare.core.container.ContainerBase;
import xyz.dylanlogan.ancientwarfare.core.gui.GuiContainerBase;
import xyz.dylanlogan.ancientwarfare.core.gui.elements.Checkbox;
import xyz.dylanlogan.ancientwarfare.core.network.NetworkHandler;

public class GuiWorksiteFishControl extends GuiContainerBase<ContainerWorksiteFishControl> {

    private Checkbox fishBox;
    private Checkbox inkBox;

    public GuiWorksiteFishControl(ContainerBase par1Container) {
        super(par1Container, 168, 48);
    }

    @Override
    public void initElements() {
        fishBox = new Checkbox(8, 8, 16, 16, "guistrings.automation.harvest_fish") {
            @Override
            public void onToggled() {
                getContainer().harvestFish = checked();
                getContainer().sendSettingsToServer();
            }
        };
        addGuiElement(fishBox);

        inkBox = new Checkbox(8, 8 + 16, 16, 16, "guistrings.automation.harvest_ink") {
            @Override
            public void onToggled() {
                getContainer().harvestInk = checked();
                getContainer().sendSettingsToServer();
            }
        };
        addGuiElement(inkBox);
    }

    @Override
    public void setupElements() {
        fishBox.setChecked(getContainer().harvestFish);
        inkBox.setChecked(getContainer().harvestInk);
    }

    @Override
    protected boolean onGuiCloseRequested() {
        getContainer().sendSettingsToServer();
        NetworkHandler.INSTANCE.openGui(player, NetworkHandler.GUI_WORKSITE_FISH_FARM, getContainer().tileEntity.xCoord, getContainer().tileEntity.yCoord, getContainer().tileEntity.zCoord);
        return false;
    }

}

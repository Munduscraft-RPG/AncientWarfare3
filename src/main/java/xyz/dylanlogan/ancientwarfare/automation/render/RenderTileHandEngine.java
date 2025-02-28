package xyz.dylanlogan.ancientwarfare.automation.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import xyz.dylanlogan.ancientwarfare.automation.tile.torque.TileTorqueBase;
import xyz.dylanlogan.ancientwarfare.core.interfaces.ITorque;
import xyz.dylanlogan.ancientwarfare.core.model.ModelBaseAW;
import xyz.dylanlogan.ancientwarfare.core.model.ModelLoader;
import xyz.dylanlogan.ancientwarfare.core.model.ModelPiece;
import org.lwjgl.opengl.GL11;

public class RenderTileHandEngine extends TileEntitySpecialRenderer implements IItemRenderer {

    private final ResourceLocation texture;
    private final ModelBaseAW model;
    private final ModelPiece outputGear, inputGear;

    public RenderTileHandEngine() {
        ModelLoader loader = new ModelLoader();
        model = loader.loadModel(getClass().getResourceAsStream("/assets/ancientwarfare/models/automation/hand_engine.m2f"));
        outputGear = model.getPiece("outputGear");
        inputGear = model.getPiece("inputGear");
        texture = new ResourceLocation("ancientwarfare", "textures/model/automation/hand_engine.png");
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float delta) {
        GL11.glPushMatrix();
        bindTexture(texture);
        GL11.glTranslated(x + 0.5d, y, z + 0.5d);

        TileTorqueBase handEngine = (TileTorqueBase) te;
        float outRotation = -handEngine.getClientOutputRotation(handEngine.getPrimaryFacing(), delta);
        float inRotation = -handEngine.getClientOutputRotation(ForgeDirection.UP, delta);//top side, not technically an 'output' rotation, but i'm lazy and not making a new method for it
        renderModel(inRotation, outRotation, ((TileTorqueBase) te).getPrimaryFacing().ordinal());
        GL11.glPopMatrix();
    }

    protected void renderModel(float inR, float outR, int face) {
        float[] rot = ITorque.forgeDiretctionToRotationMatrix[face];
        if (rot[0] != 0) {
            GL11.glRotatef(rot[0], 1, 0, 0);
        }
        else if (rot[1] != 0) {
            GL11.glRotatef(rot[1], 0, 1, 0);
        }
        outputGear.setRotation(0, 0, outR);
        inputGear.setRotation(0, inR, 0);
        model.renderModel();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        bindTexture(texture);
        GL11.glTranslated(0.5d, 0, 0.5d);
        renderModel(0, 0, 2);
        GL11.glPopMatrix();
    }

}

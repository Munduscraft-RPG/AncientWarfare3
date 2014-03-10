package net.shadowmage.ancientwarfare.modeler.gui;

import java.io.File;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.shadowmage.ancientwarfare.core.config.Statics;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase;
import net.shadowmage.ancientwarfare.core.gui.GuiFileSelect;
import net.shadowmage.ancientwarfare.core.gui.elements.Button;
import net.shadowmage.ancientwarfare.core.gui.elements.CompositeScrolled;
import net.shadowmage.ancientwarfare.core.gui.elements.GuiElement;
import net.shadowmage.ancientwarfare.core.gui.elements.Label;
import net.shadowmage.ancientwarfare.core.gui.elements.ModelWidget;
import net.shadowmage.ancientwarfare.core.gui.elements.NumberInput;
import net.shadowmage.ancientwarfare.core.model.ModelBaseAW;
import net.shadowmage.ancientwarfare.core.model.ModelPiece;
import net.shadowmage.ancientwarfare.core.model.Primitive;
import net.shadowmage.ancientwarfare.core.model.PrimitiveBox;
import net.shadowmage.ancientwarfare.core.model.PrimitiveQuad;
import net.shadowmage.ancientwarfare.core.model.PrimitiveTriangle;

public class GuiModelEditor extends GuiContainerBase
{

static ModelBaseAW model;

//map of elements by name of what they edit -- PRX, BRX, PX, BX, etc..
private HashMap<String, GuiElement> widgetMap = new HashMap<String, GuiElement>();

ModelWidget modelWidget;

NumberInput scaleInput;

CompositeScrolled pieceControlArea;
CompositeScrolled primitiveControlArea;
CompositeScrolled fileControlArea;
CompositeScrolled partListArea;

public GuiModelEditor(ContainerBase par1Container)
  {
  super(par1Container, 256, 240, defaultBackground);
  }

@Override
public void initElements()
  {
  modelWidget = new ModelWidget(0, 0, 256, 240)
    {
    @Override
    protected void onSelection(ModelPiece piece, Primitive primitive)
      {
      handleSelection(piece, primitive);
      }
    };
  modelWidget.setSelectable(true);
  if(model!=null)//attempt to use existing client-side model, if it exists
    {
    modelWidget.setModel(model);
    }
  else
    {
    modelWidget.initModel(); 
    model = modelWidget.getModel();
    }
  this.addGuiElement(modelWidget);
  
  pieceControlArea = new CompositeScrolled(-guiLeft, -guiTop, ((width-xSize) / 2), height/2);
  addGuiElement(pieceControlArea);
  
  primitiveControlArea = new CompositeScrolled(-guiLeft, -guiTop + height/2, ((width-xSize) / 2), height/2);
  addGuiElement(primitiveControlArea);
  
  fileControlArea = new CompositeScrolled(xSize, -guiTop, ((width-xSize) / 2), height/2);
  addGuiElement(fileControlArea);
  
  partListArea = new CompositeScrolled(xSize, -guiTop + height/2, ((width-xSize) / 2), height/2);
  addGuiElement(partListArea);
  }

@Override
public void setupElements()
  {
  widgetMap.clear();
  pieceControlArea.clearElements();
  primitiveControlArea.clearElements();
  fileControlArea.clearElements();
  partListArea.clearElements();
  
  pieceControlArea.setRenderPosition(-guiLeft, -guiTop);
  pieceControlArea.setSize(((width-xSize) / 2), height/2);
  
  primitiveControlArea.setRenderPosition(-guiLeft, -guiTop + height/2);
  primitiveControlArea.setSize(((width-xSize) / 2), height/2);
  
  fileControlArea.setRenderPosition(xSize, -guiTop);
  fileControlArea.setSize((width-xSize) / 2, height/2);
  
  partListArea.setRenderPosition(xSize, -guiTop + height/2);
  partListArea.setSize((width-xSize)/2, height/2);
  
  addFileControls();
  addPieceList();
    
  ModelPiece piece = getModelPiece();
  if(piece!=null)
    {
    addPieceElements();
    Primitive p = getPrimitive();   
    if(p!=null)
      {
      addPrimitiveElements(p);
      } 
    else
      {
      primitiveControlArea.setAreaSize(18);
      addNewPrimitiveButton(3);
      }
    } 
  else
    {
    addNewPieceButton(3);
    pieceControlArea.setAreaSize(18);
    primitiveControlArea.setAreaSize(18);
    }
  }

protected ModelPiece getModelPiece()
  {
  return modelWidget.getSelectedPiece();
  }

protected Primitive getPrimitive()
  {
  return modelWidget.getSelectedPrimitive();
  }

/**
 * save<br>
 * load<br>
 * import<br>
 * load texture<br>
 * uv-map<br>
 */
private void addFileControls()
  {
  int totalHeight = 3;
  int w = ((width - xSize)/2)-17;
  int h = 12;
  
  Button b = new Button(3, totalHeight, w, h, "Load Model")
    {
    @Override
    protected void onPressed()
      {
      GuiFileSelect gui = new GuiFileSelect(GuiModelEditor.this, Statics.configPath, false)
        {
        @Override
        public void onFileSelected(File file)
          {
          modelWidget.loadModel(file);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);      
      }
    };
  totalHeight+=12;
  fileControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Save Model")
    {
    @Override
    protected void onPressed()
      {
      GuiFileSelect gui = new GuiFileSelect(GuiModelEditor.this, Statics.configPath, false)
        {
        @Override
        public void onFileSelected(File file)
          {
          modelWidget.saveModel(file);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui); 
      }
    };
  totalHeight+=12;
  fileControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Import Pieces")
    {
    @Override
    protected void onPressed()
      {
      GuiFileSelect gui = new GuiFileSelect(GuiModelEditor.this, Statics.configPath, false)
        {
        @Override
        public void onFileSelected(File file)
          {
          modelWidget.importPieces(file);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);  
      }
    };
  totalHeight+=12;
  fileControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Load Texture")
    {
    @Override
    protected void onPressed()
      {
      GuiFileSelect gui = new GuiFileSelect(GuiModelEditor.this, Statics.configPath, false)
        {
        @Override
        public void onFileSelected(File file)
          {
          modelWidget.loadTexture(file);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);      
      }
    };
  totalHeight+=12;
  fileControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "U/V Map")
    {
    @Override
    protected void onPressed()
      {
      /**
       * TODO open UV Map editor GUI
       */
      }
    };
  totalHeight+=12;
  fileControlArea.addGuiElement(b);
  
  float val = scaleInput != null? scaleInput.getFloatValue() : 0.0625f;
  scaleInput = new NumberInput(3, totalHeight, w, val, this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      refreshGui();
      }
    };  
  fileControlArea.addGuiElement(scaleInput);
  totalHeight+=12;
  
  fileControlArea.setAreaSize(totalHeight);
  }

/**
 * per-piece controls<br>
 * delete piece<br>
 * copy piece<br>
 * swap parent<br>
 * rename piece<br>
 * piece origin<br>
 * piece rotation<br>
 */
private void addPieceElements()
  {
  int totalHeight = 3;
  int w = ((width - xSize)/2)-17;
  int h = 12;
  totalHeight = addNewPieceButton(totalHeight);//"New Piece"
  
  Button b = new Button(3, totalHeight, w, h, "Delete Piece")
    {
    @Override
    protected void onPressed()
      {
      modelWidget.deleteSelectedPiece();
      }
    };
  totalHeight+=12;
  pieceControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Copy Piece")
    {
    @Override
    protected void onPressed()
      {
      modelWidget.copyPiece();
      }
    };
  totalHeight+=12;
  pieceControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Swap Parent")
    {
    @Override
    protected void onPressed()
      {
      GuiPieceSelection gui = new GuiPieceSelection(GuiModelEditor.this)
        {
        @Override
        protected void onPieceSelected(ModelPiece piece)
          {
          modelWidget.swapPieceParent(piece);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);
      }
    };
  totalHeight+=12;
  pieceControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Rename Piece")
    {
    @Override
    protected void onPressed()
      {
      GuiPieceNameInput gui = new GuiPieceNameInput(GuiModelEditor.this)
        {
        @Override
        protected void onNameSelected(String name)
          {
          modelWidget.renameCurrentPiece(name);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);
      }
    };
  totalHeight+=12;
  pieceControlArea.addGuiElement(b);
  
  
  int c0 = 5;//label
  int c1 = c0+17;//-
  int c2 = c1+12;//20+12 --input
  int c3 = 2 + w - 12;//+
      
  int w2 = w - 24 - 20;
  
  Label label; 
  NumberInput input;
  
  ModelPiece currentPiece = getModelPiece();
  
  /************************************* PRX *********************************/
  label = new Label(c0, totalHeight+2, "RX");
  pieceControlArea.addGuiElement(label);
  
  b = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx()-1, piece.ry(), piece.rz());
        NumberInput input = (NumberInput) widgetMap.get("PRX");
        input.setValue(piece.rx());
        }
      }
    };
  pieceControlArea.addGuiElement(b);
  
  input = new NumberInput(c2, totalHeight, w2, currentPiece.rx(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(value, piece.ry(), piece.rz());        
        }
      }
    };
  input.setAllowNegative();
  pieceControlArea.addGuiElement(input);
  widgetMap.put("PRX", input);
    
  b = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx()+1, piece.ry(), piece.rz());
        NumberInput input = (NumberInput) widgetMap.get("PRX");
        input.setValue(piece.rx());
        }
      }
    };
  pieceControlArea.addGuiElement(b);  
  
  totalHeight+=12;
  
  
  /************************************* PRY *********************************/
  label = new Label(c0, totalHeight+2, "RY");
  pieceControlArea.addGuiElement(label);
  
  b = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx(), piece.ry()-1, piece.rz());
        NumberInput input = (NumberInput) widgetMap.get("PRY");
        input.setValue(piece.rx());
        }
      }
    };
  pieceControlArea.addGuiElement(b);
  
  input = new NumberInput(c2, totalHeight, w2, currentPiece.ry(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx(), value, piece.rz());        
        }
      }
    };  
  input.setAllowNegative();
  pieceControlArea.addGuiElement(input);
  widgetMap.put("PRY", input);
  
  b = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx(), piece.ry()+1, piece.rz());
        NumberInput input = (NumberInput) widgetMap.get("PRY");
        input.setValue(piece.rx());
        }
      }
    };
  pieceControlArea.addGuiElement(b);  
  
  totalHeight+=12;
  
  
  /************************************* PRZ *********************************/
  label = new Label(c0, totalHeight+2, "RZ");
  pieceControlArea.addGuiElement(label);
  
  b = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx(), piece.ry(), piece.rz()-1);
        NumberInput input = (NumberInput) widgetMap.get("PRZ");
        input.setValue(piece.rz());
        }
      }
    };
  pieceControlArea.addGuiElement(b);
  
  input = new NumberInput(c2, totalHeight, w2, currentPiece.rz(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx(), piece.ry(), value);        
        }
      }
    };
  input.setAllowNegative();
  pieceControlArea.addGuiElement(input);
  widgetMap.put("PRZ", input);
  
  b = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setRotation(piece.rx(), piece.ry(), piece.rz()+1);
        NumberInput input = (NumberInput) widgetMap.get("PRZ");
        input.setValue(piece.rz());
        }
      }
    };
  pieceControlArea.addGuiElement(b);  
  
  totalHeight+=12;
  
  /************************************* PX *********************************/
  label = new Label(c0, totalHeight+2, "X");
  pieceControlArea.addGuiElement(label);
  
  b = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.x()-1.f * scaleInput.getFloatValue(), piece.y(), piece.z());
        NumberInput input = (NumberInput) widgetMap.get("PX");
        input.setValue(piece.x() / scaleInput.getFloatValue());
        }
      }
    };
  pieceControlArea.addGuiElement(b);
  
  input = new NumberInput(c2, totalHeight, w2, currentPiece.x() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(value * scaleInput.getFloatValue(), piece.y(), piece.z());        
        }
      }
    };
  input.setAllowNegative();
  pieceControlArea.addGuiElement(input);
  widgetMap.put("PX", input);
  
  b = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.x()+1.f * scaleInput.getFloatValue(), piece.y(), piece.z());
        NumberInput input = (NumberInput) widgetMap.get("PX");
        input.setValue(piece.x() / scaleInput.getFloatValue());
        }
      }
    };
  pieceControlArea.addGuiElement(b);  
  
  totalHeight+=12;
  
  /************************************* PY *********************************/
  label = new Label(c0, totalHeight+2, "Y");
  pieceControlArea.addGuiElement(label);
  
  b = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.x(), piece.y()-1.f * scaleInput.getFloatValue(), piece.z());
        NumberInput input = (NumberInput) widgetMap.get("PY");
        input.setValue(piece.y() / scaleInput.getFloatValue());
        }
      }
    };
  pieceControlArea.addGuiElement(b);
  
  input = new NumberInput(c2, totalHeight, w2, currentPiece.y() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.x(), value * scaleInput.getFloatValue(), piece.z());        
        }
      }
    };
  input.setAllowNegative();
  pieceControlArea.addGuiElement(input);
  widgetMap.put("PY", input);
  
  b = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.x(), piece.y()+1.f * scaleInput.getFloatValue(), piece.z());
        NumberInput input = (NumberInput) widgetMap.get("PY");
        input.setValue(piece.y() / scaleInput.getFloatValue());
        }
      }
    };
  pieceControlArea.addGuiElement(b);  
  
  totalHeight+=12;
  
  /************************************* PZ *********************************/
  label = new Label(c0, totalHeight+2, "Z");
  pieceControlArea.addGuiElement(label);
  
  b = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.x(), piece.y(), piece.z()-1.f * scaleInput.getFloatValue());
        NumberInput input = (NumberInput) widgetMap.get("PZ");
        input.setValue(piece.z() / scaleInput.getFloatValue());
        }
      }
    };
  pieceControlArea.addGuiElement(b);
  
  input = new NumberInput(c2, totalHeight, w2, currentPiece.z() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.y(), piece.y(), value * scaleInput.getFloatValue());        
        }
      }
    };
  input.setAllowNegative();
  pieceControlArea.addGuiElement(input);
  widgetMap.put("PZ", input);
  
  b = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {
      ModelPiece piece = getModelPiece();
      if(piece!=null)
        {
        piece.setPosition(piece.x(), piece.y(), piece.z() + 1.f * scaleInput.getFloatValue());
        NumberInput input = (NumberInput) widgetMap.get("PZ");
        input.setValue(piece.z() / scaleInput.getFloatValue());
        }
      }
    };
  pieceControlArea.addGuiElement(b);  
  
  totalHeight+=12;
  
  
  pieceControlArea.setAreaSize(totalHeight);
  }

private int addNewPieceButton(int height)
  {
  int w = ((width - xSize)/2)-17;
  Button b = new Button(3, height, w, 12, "New Piece")
    {
    @Override
    protected void onPressed()
      {
      GuiPieceNameInput gui = new GuiPieceNameInput(GuiModelEditor.this)
        {
        @Override
        protected void onNameSelected(String name)
          {
          modelWidget.addNewPiece(name);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);
      }
    };
  pieceControlArea.addGuiElement(b);
  return height + 12;
  }

private int addNewPrimitiveButton(int height)
  {
  int w = ((width - xSize)/2)-17;
  Button b = new Button(3, height, w, 12, "New Primitive")
    {
    @Override
    protected void onPressed()
      {
      GuiPrimitiveSelection gui = new GuiPrimitiveSelection(GuiModelEditor.this)
        {
        @Override
        protected void onPrimitiveCreated(Primitive p)
          {
          modelWidget.addNewPrimitive(p);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);
      }
    };
  primitiveControlArea.addGuiElement(b);
  return height + 12;
  }

/**
 * per primitive controls<br>
 * delete primitive<br>
 * copy primitive<br>
 * swap primitive parent<br>
 * primitive origin<br>
 * primitive rotation<br>
 * primitive location (offset from rotation point)<br>
 */
private void addPrimitiveElements(Primitive prim)
  {  
  int totalHeight = 3;
  int w = ((width - xSize)/2)-17;
  int h = 12;
  totalHeight = addNewPrimitiveButton(totalHeight);

  Button b = new Button(3, totalHeight, w, h, "Delete Primitive")
    {
    @Override
    protected void onPressed()
      {
      modelWidget.deleteSelectedPrimitive();
      }
    };
  totalHeight+=12;
  primitiveControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Copy Primitive")
    {
    @Override
    protected void onPressed()
      {
      modelWidget.copyPrimitive();
      }
    };
  totalHeight+=12;
  primitiveControlArea.addGuiElement(b);
  
  b = new Button(3, totalHeight, w, h, "Swap Parent")
    {
    @Override
    protected void onPressed()
      {
      GuiPieceSelection gui = new GuiPieceSelection(GuiModelEditor.this)
        {
        @Override
        protected void onPieceSelected(ModelPiece piece)
          {
          modelWidget.swapPrimitiveParent(piece);
          }
        };
      Minecraft.getMinecraft().displayGuiScreen(gui);
      }
    };
  totalHeight+=12;
  primitiveControlArea.addGuiElement(b);
  
  totalHeight = addOriginControls(totalHeight);
  
   
  if(prim.getClass()==PrimitiveBox.class)
    {
    totalHeight = addBoxControls(totalHeight);
    }
  else if(prim.getClass()==PrimitiveTriangle.class)
    {
    totalHeight = addTriangleControls(totalHeight);   
    }
  else if(prim.getClass()==PrimitiveQuad.class)
    {
    totalHeight = addQuadControls(totalHeight);   
    }
  
  primitiveControlArea.setAreaSize(totalHeight);
  }

private int addOriginControls(int totalHeight)
  {
  int w = ((width - xSize)/2)-17;
  int c0 = 5;//label
  int c1 = c0+17;//-
  int c2 = c1+12;//20+12 --input
  int c3 = 2 + w - 12;//+      
  int w2 = w - 24 - 20;//width of the input bar
  
  Primitive currentBox = getPrimitive();
  
  Label label;
  Button button;
  NumberInput input;
  
  /************************************* OX *********************************/
  label = new Label(c0, totalHeight, "OX");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      Primitive box = getPrimitive();
      box.setOrigin(box.x()-1.f * scaleInput.getFloatValue(), box.y(), box.z());
      NumberInput num = (NumberInput)widgetMap.get("BOX");
      num.setValue(box.x() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      Primitive box = getPrimitive();
      box.setOrigin(box.x()+1.f * scaleInput.getFloatValue(), box.y(), box.z());
      NumberInput num = (NumberInput)widgetMap.get("BOX");
      num.setValue(box.x() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.x() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      Primitive box = getPrimitive();
      box.setOrigin(value * scaleInput.getFloatValue(), box.y(), box.z());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BOX", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  
  /************************************* OY *********************************/
  label = new Label(c0, totalHeight, "OY");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      Primitive box = getPrimitive();
      box.setOrigin(box.x(), box.y()-1.f * scaleInput.getFloatValue(), box.z());
      NumberInput num = (NumberInput)widgetMap.get("BOY");
      num.setValue(box.y() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      Primitive box = getPrimitive();
      box.setOrigin(box.x(), box.y()+1.f * scaleInput.getFloatValue(), box.z());
      NumberInput num = (NumberInput)widgetMap.get("BOY");
      num.setValue(box.y() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.y() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      Primitive box = getPrimitive();
      box.setOrigin(box.x(), value * scaleInput.getFloatValue(), box.z());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BOY", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  
  /************************************* OZ *********************************/
  label = new Label(c0, totalHeight, "OZ");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      Primitive box = getPrimitive();
      box.setOrigin(box.x(), box.y(), box.z()-1.f * scaleInput.getFloatValue());
      NumberInput num = (NumberInput)widgetMap.get("BOZ");
      num.setValue(box.z() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      Primitive box = getPrimitive();
      box.setOrigin(box.x(), box.y(), box.z()+1.f * scaleInput.getFloatValue());
      NumberInput num = (NumberInput)widgetMap.get("BOZ");
      num.setValue(box.z() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.z() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      Primitive box = getPrimitive();
      box.setOrigin(box.x(), box.y(), value * scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BOZ", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  return totalHeight;
  }

private int addBoxControls(int totalHeight)
  {
  int w = ((width - xSize)/2)-17;
  int c0 = 5;//label
  int c1 = c0+17;//-
  int c2 = c1+12;//20+12 --input
  int c3 = 2 + w - 12;//+      
  int w2 = w - 24 - 20;//width of the input bar
  
  PrimitiveBox currentBox = (PrimitiveBox)getPrimitive();
  
  Label label;
  Button button;
  NumberInput input;
  
  
  /************************************* BRX *********************************/
  label = new Label(c0, totalHeight, "RX");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx() -1, box.ry(), box.rz());
      NumberInput num = (NumberInput)widgetMap.get("BRX");
      num.setValue(box.rx());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx()+1, box.ry(), box.rz());
      NumberInput num = (NumberInput)widgetMap.get("BRX");
      num.setValue(box.rx());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.rx(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(value, box.ry(), box.rz());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BRX", input);
  input.setAllowNegative();
  
  totalHeight+=12;
    
  /************************************* BRY *********************************/
  label = new Label(c0, totalHeight, "RY");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx(), box.ry()-1, box.rz());
      NumberInput num = (NumberInput)widgetMap.get("BRY");
      num.setValue(box.ry());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx(), box.ry()+1, box.rz());
      NumberInput num = (NumberInput)widgetMap.get("BRY");
      num.setValue(box.ry());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.ry(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx(), value, box.rz());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BRY", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  /************************************* BRZ *********************************/
  label = new Label(c0, totalHeight, "RZ");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx(), box.ry(), box.rz()-1);
      NumberInput num = (NumberInput)widgetMap.get("BRZ");
      num.setValue(box.rz());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx(), box.ry(), box.rz()+1);
      NumberInput num = (NumberInput)widgetMap.get("BRZ");
      num.setValue(box.rz());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.rz(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setRotation(box.rx(), box.ry(), value);
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BRZ", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  
  /************************************* BX *********************************/
  label = new Label(c0, totalHeight, "X");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1()-1.f * scaleInput.getFloatValue(), box.y1(), box.z1(), box.width(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BX");
      num.setValue(box.x1() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1()+1.f * scaleInput.getFloatValue(), box.y1(), box.z1(), box.width(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BX");
      num.setValue(box.x1() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.x1() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(value * scaleInput.getFloatValue(), box.y1(), box.z1(), box.width(), box.height(), box.length());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BX", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  
  /************************************* BY *********************************/
  label = new Label(c0, totalHeight, "Y");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1()-1.f * scaleInput.getFloatValue(), box.z1(), box.width(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BY");
      num.setValue(box.y1() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1()+1.f * scaleInput.getFloatValue(), box.z1(), box.width(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BY");
      num.setValue(box.y1() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.y1() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), value * scaleInput.getFloatValue(), box.z1(), box.width(), box.height(), box.length());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BY", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  
  /************************************* BZ *********************************/
  label = new Label(c0, totalHeight, "Z");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1()-1.f * scaleInput.getFloatValue(), box.width(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BZ");
      num.setValue(box.z1() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1()+1.f * scaleInput.getFloatValue(), box.width(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BZ");
      num.setValue(box.z1() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.z1() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), value * scaleInput.getFloatValue(), box.width(), box.height(), box.length());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BZ", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  /************************************* BW *********************************/
  label = new Label(c0, totalHeight, "W");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width()-1.f * scaleInput.getFloatValue(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BW");
      num.setValue(box.width() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width()+1.f * scaleInput.getFloatValue(), box.height(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BW");
      num.setValue(box.width() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.width() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), value * scaleInput.getFloatValue(), box.height(), box.length());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BW", input);
  input.setAllowNegative();
  
  totalHeight+=12;

  /************************************* BH *********************************/
  label = new Label(c0, totalHeight, "H");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width(), box.height()-1.f * scaleInput.getFloatValue(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BH");
      num.setValue(box.height() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width(), box.height()+1.f * scaleInput.getFloatValue(), box.length());
      NumberInput num = (NumberInput)widgetMap.get("BH");
      num.setValue(box.height() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.height() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width(), value * scaleInput.getFloatValue(), box.length());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BH", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  
  /************************************* BL *********************************/
  label = new Label(c0, totalHeight, "L");
  primitiveControlArea.addGuiElement(label);
  
  button = new Button(c1, totalHeight, 12, 12, "-")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width(), box.height(), box.length()-1.f * scaleInput.getFloatValue());
      NumberInput num = (NumberInput)widgetMap.get("BL");
      num.setValue(box.length() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  button = new Button(c3, totalHeight, 12, 12, "+")
    {
    @Override
    protected void onPressed()
      {      
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width(), box.height(), box.length()+1.f * scaleInput.getFloatValue());
      NumberInput num = (NumberInput)widgetMap.get("BL");
      num.setValue(box.length() / scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(button);
  
  input = new NumberInput(c2, totalHeight, w2, currentBox.length() / scaleInput.getFloatValue(), this)
    {
    @Override
    public void onValueUpdated(float value)
      {
      PrimitiveBox box = (PrimitiveBox)getPrimitive();
      box.setBounds(box.x1(), box.y1(), box.z1(), box.width(), box.height(), value * scaleInput.getFloatValue());
      }
    };
  primitiveControlArea.addGuiElement(input);
  widgetMap.put("BL", input);
  input.setAllowNegative();
  
  totalHeight+=12;
  
  return totalHeight;
  }

private int addTriangleControls(int totalHeight)
  {
  /**
   * TODO add x1,y1,z1,x2,y2,z2,x3,y3,z3,flip controls
   */
  return totalHeight;  
  }

private int addQuadControls(int totalHeight)
  {
  /**
   * TODO add x,y,z,w,h,rx,ry,rz,flip controls
   */
  return totalHeight;
  }

private void addPieceList()
  {
  /**
   * TODO
   */
  }

private void handleSelection(ModelPiece piece, Primitive primitive)
  {
  this.refreshGui();
  }

}

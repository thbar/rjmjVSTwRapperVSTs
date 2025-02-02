package rjm.vst.midi.polytool;


import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;
import rjm.vst.midi.examples.gui.swing.JustEchoMidi;
import rjm.vst.tools.VstUtils;
import rjm.vst.javafx.SceneToJComponent;
import rjm.vst.javafx.UIUtils;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class PolyToolGui extends VSTPluginGUIAdapter implements ChangeListener {

    JSlider VolumeSlider;
    JTextField VolumeText;
    JCheckBox CbThru;
    
    CheckBox cb1;
    int currYPos = 0;
    double rowHeight;

    private VSTPluginAdapter pPlugin;
    
    VBox root;
    
    VBox rowBox;
    
    //THIS IS THE HOT AREA RIGHT NOW
    private Scene createScene() {
        //Group  root  =  new  Group();
        this.root  =  new  VBox();
        Scene  scene  =  new  Scene(root, Color.DARKRED);
        Text  text  =  new  Text();
        
        VBox parentBox = new VBox();
        rowBox = new VBox();
        parentBox.getChildren().add(text);

        text.setFont(new Font(25));
        text.setText("PolyTool");
        
        Button btnNewRow = new Button();
        btnNewRow.setText("Add row");
        btnNewRow.setOnAction(e -> this.HandleNewRowButton()); 
        parentBox.getChildren().add(btnNewRow);

        parentBox.getChildren().add(rowBox);

        root.getChildren().add(parentBox);
       
        
        return (scene);
    } 
    
 

    public PolyToolGui( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) throws Exception {

	super( r, plugin );
	try
	{
	    this.setTitle( "PolyTool" );
	    this.setSize(805, 200);
	    //Consider using setPreferredSize if this ever gives any issues

	    this.addComponentListener(new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e)
		{
		    //this. doesn't work
		}
	    });


	    //TODO (close immediately if it's first opened, and then somehow know if it's launched again?)

	    ((PolyTool)plugin).gui=this; //tell the plug that it has a gui!


	    final JFXPanel fxPanel = new JFXPanel();
	    Scene scene = createScene();
	    fxPanel.setScene(scene);
	    this.pPlugin = plugin;
	    this.getContentPane().add(fxPanel);


	    if( RUNNING_MAC_X ) this.show();
	}
	catch (Exception e)
	{
	    VstUtils.outStackTrace(e);
	}




    }

   
  
    public Label getRowLabel(String text)
    {
	Label label = new Label();
	label.setFont(new Font("Arial", 9));
	label.setText(text);
	return label;
    }

    
    public void addGuiRow(PolyRow row) //Create a row in the UI
    {
	int index = 0;
	
	//This is meant to be a single 1 row grid
	GridPane rowGrid = new GridPane();
	rowGrid.setPadding(new Insets(4));
	rowGrid.setHgap(10);
	rowGrid.setVgap(5);
	rowGrid.setStyle("-fx-background-color: #C0C0C0;");
	//TODO
	//Enabled, Input Chan (app level instead?), Input Note, Learn Button, (Output Chan make app level?), CC#, CC Min, CC Max 
	
        //int numWidth = 50; //width of text fields that contain numbers
	
	
	//TODO - Fix bug where this text field doesn't seem to get saved/recalled
        TextField tfRowName = new TextField();
        if (row.getName() != null)
        { tfRowName.setText(row.getName()); }
        else 
        { tfRowName.setText("Row " + row.getId()); }
        tfRowName.setId("" + row.getId());  //The "" in front makes it cast the input as a string
	tfRowName.setMaxWidth(90);
	tfRowName.setStyle("-fx-background-color: #A0A0A0;");
	tfRowName.textProperty().addListener((observable, oldValue, newValue) -> {
	    this.HandleRowName(tfRowName);
	});
        //tfRowName.setOnAction(e -> this.HandleRowName(tfRowName));  //Apparently this is only if Return is pressed
	rowGrid.add(tfRowName,index,0);
	index++;

        CheckBox cb = new CheckBox();
        cb.setText("");
        if (row.getEnabled())
        { cb.setSelected(true); }
        else
        { cb.setSelected(false); }
        cb.setId("" + row.getId());
        //This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
        cb.setOnAction(e -> this.HandleEnabledCheckbox(cb)); 
        rowGrid.add(cb,index,0);
        rowGrid.add(getRowLabel("Enabled"), index, 1);
        index++;

        CheckBox cb1 = new CheckBox();
        cb1.setText("");
        if (row.isInverse())
        { cb1.setSelected(true); }
        else
        { cb1.setSelected(false); }
        cb1.setId("" + row.getId());
        cb1.setOnAction(e -> this.HandleInverseCheckbox(cb1)); 
        rowGrid.add(cb1,index,0);
        rowGrid.add(getRowLabel("Inverse"), index, 1);
        index++;

        String[] channels = new String[16];
        for (int i = 0; i < 16; i++){ channels[i] = Integer.toString(i + 1);}
        
        ComboBox<String> cbInputChannel = new ComboBox<String>();
        cbInputChannel.getItems().addAll(channels);
        if (row.getInputChannel() >= 0)
        { //Here getting string instead of int so we pick the item based on the visible GUI value
            cbInputChannel.getSelectionModel().select(Integer.toString(row.getInputChannel())); }
        else
        { cbInputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
        cbInputChannel.setId("" + row.getId());
        cbInputChannel.setOnAction(e -> HandleInChannelCombo(cbInputChannel));
        rowGrid.add(cbInputChannel,index,0);
        rowGrid.add(getRowLabel("Input Channel"),index,1);
        HandleInChannelCombo(cbInputChannel);
        index++;
        
        ComboBox<String> cbOutputChannel = new ComboBox<String>();
        cbOutputChannel.getItems().addAll(channels);
        if (row.getOutputChannel() >= 0)
        { cbOutputChannel.getSelectionModel().select(Integer.toString(row.getOutputChannel())); }
        else
        { cbOutputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
        cbOutputChannel.setId("" + row.getId());
        cbOutputChannel.setOnAction(e -> HandleOutChannelCombo(cbOutputChannel));
        rowGrid.add(cbOutputChannel,index,0);
        rowGrid.add(getRowLabel("Output Channel"),index,1);
        HandleOutChannelCombo(cbOutputChannel);
        index++;

	Button learnBtn = new Button();
	learnBtn.setText("Learn Note");
	if (row.getNote() != null)
	{ learnBtn.setText(row.getNote().getNoteNamePlusOctave()); }
	learnBtn.setId("" + row.getId());
	learnBtn.setMinWidth(77);
	learnBtn.setOnAction(e -> HandleLearnButton(learnBtn)); 
        rowGrid.add(learnBtn,index,0);
        index++;

        String[] vals = new String[128];
        for (int i = 0; i < 128; i++){ vals[i] = Integer.toString(i);}

        ComboBox<String> minValue = new ComboBox<String>();
        minValue.getItems().addAll(vals);
        if (row.getMinOutputValue() >= 0)
        { minValue.getSelectionModel().select(Integer.toString(row.getMinOutputValue())); }
        else
        { minValue.getSelectionModel().select(0); }//Auto select channel 1 for input }
        minValue.setId("" + row.getId());
        minValue.setOnAction(e -> HandleMinOutValueCombo(minValue));
        rowGrid.add(minValue,index,0);
        rowGrid.add(getRowLabel("Min Value"),index,1);
        HandleMinOutValueCombo(minValue);
        index++;
        
        
        ComboBox<String> maxValue = new ComboBox<String>();
        maxValue.getItems().addAll(vals);
        if (row.getMaxOutputValue() >= 0)
        { maxValue.getSelectionModel().select(Integer.toString(row.getMaxOutputValue())); }
        else
        { maxValue.getSelectionModel().select(127); }//Auto select channel 1 for input }
        maxValue.setId("" + row.getId());
        maxValue.setOnAction(e -> HandleMaxOutValueCombo(maxValue));
        rowGrid.add(maxValue,index,0);
        rowGrid.add(getRowLabel("Max Value"),index,1);
        HandleMaxOutValueCombo(maxValue);
        index++;

        String[] ccVals = new String[64];
        for (int i = 0; i < 64; i++){ ccVals[i] = Integer.toString(i + 1);}

        ComboBox<String> ccOut = new ComboBox<String>();
        ccOut.getItems().addAll(ccVals);
        ccOut.getItems().add("Pitch Bend");
        if (row.getOutputCCNum() >= 0)
        { ccOut.getSelectionModel().select(Integer.toString(row.getOutputCCNum())); }
        else
        { ccOut.getSelectionModel().select(12); }//Auto select CC 11 for arbitrary reasons
        ccOut.setId("" + row.getId());
        ccOut.setOnAction(e -> HandleOutCCCombo(ccOut));
        rowGrid.add(ccOut,index,0);
        rowGrid.add(getRowLabel("CC Output"),index,1);
        HandleOutCCCombo(ccOut);
        index++;
        
        
        //TODO make this field invisible when doing pitch bend
        //This is the value we set CC to on note off 
        ComboBox<String> noteOffValue = new ComboBox<String>();
        noteOffValue.getItems().addAll(vals);
        if (row.getNoteOffCCValue() >= 0)
        { noteOffValue.getSelectionModel().select(Integer.toString(row.getNoteOffCCValue())); }
        else
        { noteOffValue.getSelectionModel().select(0); }
        noteOffValue.setId("" + row.getId());
        noteOffValue.setOnAction(e -> HandleNoteOffValueCombo(noteOffValue));
        rowGrid.add(noteOffValue,index,0);
        rowGrid.add(getRowLabel("CC Val on NoteOff"),index,1);
        HandleNoteOffValueCombo(noteOffValue);
        index++;

	Button delBtn = new Button();
	delBtn.setText("Delete");
	delBtn.setId("" + row.getId());
	delBtn.setOnAction(e -> removeRow(rowGrid, Integer.parseInt(delBtn.getId()))); 
        rowGrid.add(delBtn,index,0);
        index++;
	
        this.rowHeight = rowGrid.heightProperty().doubleValue(); //so we know how high these things are

        if (((PolyTool)plugin).getPolyCollection().size() == 2) //First row just added to collection, so first row in GUI
        {
            this.setSize(this.getSize().width, (int)this.getSize().height + (int)rowHeight);
        }

        Platform.runLater(new Runnable(){
            @Override
            public void run()
            { rowBox.getChildren().add(rowGrid);
            }
        });

	//VstUtils.out("poly collection now has " + ((PolyTool)plugin).getPolyCollection().size());
    }
    
    
    public Button currentLearnButton;
    public void HandleLearnButton(Button learnBtn)
    {
	Platform.runLater(new Runnable(){
            @Override
            public void run()
            { learnBtn.setText("Play a key");
            }
        });
	this.currentLearnButton = learnBtn;
    }
    
    
    
    public void HandleNewRowButton()
    {

	//VstUtils.out("Attempting to add row to poly collection");
	int rowId = -1;
	try
	{
            PolyRow row = new PolyRow();
            //VstUtils.out("Row has id of " + row.getId());
	    rowId = ((PolyTool)plugin).getPolyCollection().add(row);
            //VstUtils.out("Row NOW has id of " + rowId);
            addGuiRow(row);
	}
	catch (Exception e)
	{
	    VstUtils.outStackTrace(e);
	}
	//float rows = this.pPlugin.getParameter(PolyTool.PARAM_ID_ROWS);
	//this.pPlugin.setParameter(PolyTool.PARAM_ID_ROWS, rows + 1);
    }
    
    public void HandleEnabledCheckbox(CheckBox cb)
    {
	Boolean checked = cb.selectedProperty().get();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setEnabled(checked);
	getPolyCollection().updateRow(row);
    }
    
    public void HandleInverseCheckbox(CheckBox cb)
    {
	Boolean checked = cb.selectedProperty().get();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setInverse(cb.isSelected());
	getPolyCollection().updateRow(row);
    }
    
    public void HandleInChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setInputChannel(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleOutChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setOutputChannel(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleMinOutValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setMinOutputValue(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleMaxOutValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setMaxOutputValue(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleOutCCCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	if (value == "Pitch Bend")
	{ row.setOutputCCNum(PolyTool.PITCH_BEND); }
	else
	{ row.setOutputCCNum(Integer.parseInt(value)); }
	row.setOutputCCNum(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleNoteOffValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setNoteOffCCValue(Integer.parseInt(value)); 
	getPolyCollection().updateRow(row);
    }
    
    public void HandleRowName(TextField cb)
    {
	VstUtils.out("Handling Row Name");
	String value = cb.getText();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	VstUtils.out("Setting row name to " + value);
	row.setName(value);
	getPolyCollection().updateRow(row);
    }
    
    

    
    public PolyRowCollection getPolyCollection()
    {
	return ((PolyTool)plugin).getPolyCollection();
    }
    
    public void removeRow(GridPane b, int rowId)
    {
	((PolyTool)plugin).getPolyCollection().removeRow(rowId);
	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { rowBox.getChildren().remove(b); }
	});
	VstUtils.out("poly collection now has " + ((PolyTool)plugin).getPolyCollection().size());
	
	if (((PolyTool)plugin).getPolyCollection().size() == 0)
        {
	    clearGuiRows(); //will flush the header too
        }
    }
    
    public void clearGuiRows()
    {
	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { rowBox.getChildren().clear();}
	});

    }
  
    
    


    @Override
    public void stateChanged(ChangeEvent e)
    {
	//Since we're using JavaFX, we maybe don't ever need to use this function...
		//but it is still required as it is an inherited abstract method	
    }






    public static void main(String[] args) throws Throwable 
    {
	PolyToolGui gui = new PolyToolGui(new VSTPluginGUIRunner(), null);
	gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //Apparently EXIT_ON_CLOSE can cause problems with jvstwrapper
	//gui.show();
    }

}

package pt.iscte.pidesco.uml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pidesco.uml.Compartment;
import pt.iscte.pidesco.uml.extensibility.UMLActions;

public class UMLClass extends Figure{
	
	public static Color classColor = new Color(null,255,255,206);
	private Compartment attributeFigure = new Compartment(ToolbarLayout.ALIGN_TOPLEFT);
	private Compartment methodFigure = new Compartment(ToolbarLayout.ALIGN_TOPLEFT);
	private Compartment optionsFigure = new Compartment(ToolbarLayout.ALIGN_CENTER);
	
	private Font font = new Font(null, "sansserif", 11, SWT.BOLD);
	
	private HashMap<String, String> connectionsList;
	
	private static final String EXT_POINT_ACTIONS = "pt.iscte.pidesco.uml.actions";
	
	public UMLClass(Label name, Label classType) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);	
		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(classColor);
		setOpaque(true);
		//setSize(200, 100);
		
		connectionsList = new HashMap<>();
		
		name.setFont(font);
		
		add(classType);
		add(name);
		add(attributeFigure);
		add(methodFigure);
		
		Label optionsLabel = new Label(new Image(
				null, UMLClass.class.getResourceAsStream("images\\options.png")));
		setOptionsListener(optionsLabel, name);
		
		/*Button bn = new Button(optionsLabel.getParent(), SWT.FLAT);
	    bn.setText("Right Click to see the popup menu");
	    
	    Menu popupMenu = new Menu(bn);
	    MenuItem newItem = new MenuItem(popupMenu, SWT.CASCADE);
	    newItem.setText("New");
	    MenuItem refreshItem = new MenuItem(popupMenu, SWT.NONE);
	    refreshItem.setText("Refresh");
	    MenuItem deleteItem = new MenuItem(popupMenu, SWT.NONE);
	    deleteItem.setText("Delete");

	    Menu newMenu = new Menu(popupMenu);
	    newItem.setMenu(newMenu);

	    MenuItem shortcutItem = new MenuItem(newMenu, SWT.NONE);
	    shortcutItem.setText("Shortcut");
	    MenuItem iconItem = new MenuItem(newMenu, SWT.NONE);
	    iconItem.setText("Icon");

	    bn.setMenu(popupMenu);*/
		
		optionsFigure.add(optionsLabel);
		
		add(optionsFigure);
	}
	
	public Compartment getAttributesCompartment() {
		return attributeFigure;
	}
	public Compartment getMethodsCompartment() {
		return methodFigure;
	}
	
	public void setConnection(String className, String connectionType) {
		connectionsList.put(className+".java", connectionType);
	}
	public HashMap<String, String> getConnectionsList() {
		return connectionsList;
	}
	public void setConnectionsList(HashMap<String, String> connectionsList) {
		this.connectionsList = connectionsList;
	}
	
	private void setOptionsListener(Figure figure, Label name) {
		figure.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent me) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent me) {
				// TODO Auto-generated method stub
				System.out.println("Clickei -> " + name.getText());
				
				buildActions(name.getText());
//				buildActions1();
								
			}
			
			@Override
			public void mouseDoubleClicked(MouseEvent me) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void buildActions(String name) {
		Shell shell = new Shell(SWT.SHELL_TRIM | SWT.CENTER);	
		shell.setLayout(new GridLayout());

        shell.setText(name);
        shell.setSize(name.length()*5+300, 200);
        
        Button bn = new Button(shell, SWT.FLAT);
        bn.setText("Test Button");
	    
        List<String> actionsList = loadActions();
	    
	    for (String action : actionsList) {
	    	Button bnAux = new Button(shell, SWT.FLAT);
	    	bnAux.setText(action);
		}
            
        shell.open();
	}
	
	private void buildActions1() {
		Shell shell = new Shell();
	    shell.setLayout(new GridLayout());
	    
	    Button bn = new Button(shell, SWT.FLAT);
	    bn.setText("Test");
	    
	    List<String> actionsList = loadActions();
	    
	    for (String action : actionsList) {
	    	Button bnAux = new Button(shell, SWT.FLAT);
	    	bnAux.setText(action);
		}	    
	    
	    /*Menu popupMenu = new Menu(bn);
	    MenuItem newItem = new MenuItem(popupMenu, SWT.CASCADE);
	    newItem.setText("New");
	    MenuItem refreshItem = new MenuItem(popupMenu, SWT.NONE);
	    refreshItem.setText("Refresh");
	    MenuItem deleteItem = new MenuItem(popupMenu, SWT.NONE);
	    deleteItem.setText("Delete");

	    Menu newMenu = new Menu(popupMenu);
	    newItem.setMenu(newMenu);

	    MenuItem shortcutItem = new MenuItem(newMenu, SWT.NONE);
	    shortcutItem.setText("Shortcut");
	    MenuItem iconItem = new MenuItem(newMenu, SWT.NONE);
	    iconItem.setText("Icon");

	    bn.setMenu(popupMenu);*/
	    
	    shell.open();
	    
	}
	
	private List<String> loadActions() {
		List<String> actionsList = new ArrayList<>();
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = reg.getConfigurationElementsFor(EXT_POINT_ACTIONS);
		System.out.println("Elements -> " + elements.length);
		for(IConfigurationElement e : elements) {
			System.out.println("FOR");
			try {
				UMLActions action = (UMLActions) e.createExecutableExtension("class");
				
				if(action != null)
					actionsList.add(action.setLabel());
				
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			
		}
				
		/*for(int i = 0; i< elements.length ; i++) {
			System.out.println("FOR");
			
			UMLActions action = null;
			try {
				action = (UMLActions) elements[i].createExecutableExtension("class");
				
				if(action != null)
					actionsList.add(action.setLabel());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (IConfigurationElement e : elements) {
			System.out.println("FOR");
			
			UMLActions action = null;
			try {
				action = (UMLActions) e.createExecutableExtension("class");
				
				if(action != null)
					actionsList.add(action.setLabel());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}*/
//		for(IExtension ext : reg.getExtensionPoint(EXT_POINT_ACTIONS).getExtensions()) {
//			System.out.println("FOR");
//			
//			Action action = null;
//			try {
//				action = (Action) ext.getConfigurationElements()[0].createExecutableExtension("class");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if(action != null)
//				actionsList.add(action.setLabel());
//		}
		System.out.println(actionsList.toString());
		return actionsList;
		
	}

}

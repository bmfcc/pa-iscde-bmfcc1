package pt.iscte.pidesco.uml;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pt.iscte.pidesco.extensibility.PidescoView;
import pt.iscte.pidesco.javaeditor.service.JavaEditorServices;
import pt.iscte.pidesco.visitor.ClassChecker;
import pt.iscte.pidesco.visitor.JavaParser;

public class UMLView implements PidescoView {

	private UMLClass umlclass;
	private Graph graph;
	private int layout = 1;
	private File basedir;
	private HashMap<String, HashMap<String, String>> table;
	private HashMap<String, UMLClass> classFigures;
	private HashMap<String, String> classPath;
	private List<String> classList;
	private JavaEditorServices editor;
	private Device dev;

	@Override
	public void createContents(Composite viewArea, Map<String, Image> imageMap) {
		// TODO Auto-generated method stub

		dev = viewArea.getParent().getDisplay();
		
		BundleContext context = Activator.getContext();

		ServiceReference<JavaEditorServices> serviceReference = context.getServiceReference(JavaEditorServices.class);
		editor = context.getService(serviceReference);

		classList = new ArrayList<>();
		table = new HashMap<>();
		classFigures = new HashMap<>();
		classPath = new HashMap<>();
		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		basedir = new File(workspace);

		getFiles(basedir);

		createPartControl(viewArea);
	}

	public void createPartControl(Composite parent) {
		GraphViewer viewer = new GraphViewer(parent, SWT.BORDER);
		viewer.setContentProvider(new ZestNodeContentProvider());
		viewer.setLabelProvider(new ZestFigureProvider());

		List<UMLClass> figureList = new ArrayList<>();

		for(String classToFigure : classList) {
			ClassChecker checker = new ClassChecker();
			JavaParser.parse(classToFigure, checker);
			figureList.add(checker.getUMLClass());
			figureList.addAll(checker.getInnerClasses());
		}

		viewer.setInput(figureList);
		viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		viewer.applyLayout();

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				// TODO Auto-generated method stub
				UMLClass umlClass;
				if(!event.getSelection().isEmpty()) {
					final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					final Object firstElement = selection.getFirstElement();
					if(firstElement instanceof UMLClass) {
						umlClass = (UMLClass)firstElement;
						
						String path = classPath.get(((Label)umlClass.getChildren().get(1)).getText()+".java");
						File classFile;

						if(path != null) {
							classFile = new File(path);
							editor.openFile(classFile);
						}else {
							for (String key : table.keySet()) {
								HashMap<String, String> hmAux = table.get(key);
								if(hmAux.containsKey(((Label)umlClass.getChildren().get(1)).getText()+".java")) {
									UMLClass umlClassParent = classFigures.get(key);
									path = classPath.get(((Label)umlClassParent.getChildren().get(1)).getText()+".java");
									
									classFile = new File(path);
									editor.openFile(classFile);
									break;
								}
							}
						}

						System.out.println(((Label)umlClass.getChildren().get(1)).getText()+".java");
					}
				}
			}
		});
		
	}

	class ZestNodeContentProvider extends ArrayContentProvider implements IGraphEntityContentProvider {
		@Override
		public Object[] getConnectedTo(Object entity) {
			HashMap<String, String> auxConnect = table.get(((Label)((UMLClass)entity).getChildren().get(1)).getText()+".java");

			if(auxConnect!=null) {
				//				System.out.println("Existe conexão");
				UMLClass[] connections = new UMLClass [auxConnect.keySet().size()];
				int i = 0;
				for(String classToConnect: auxConnect.keySet()) {
					connections[i] = classFigures.get(classToConnect);
					i++;

				}

				return connections;
			}

			return new Object[0];
		}
	}

	class ZestFigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider, ISelfStyleProvider {
		@Override
		public IFigure getFigure(Object figure) {
			umlclass = (UMLClass) figure;
			umlclass.setSize(umlclass.getPreferredSize());
			/*umlclass.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent me) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mousePressed(MouseEvent me) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseDoubleClicked(MouseEvent me) {
					// TODO Auto-generated method stub
					UMLClass umlClass = (UMLClass)me.getSource();
					String path = classPath.get(((Label)umlClass.getChildren().get(1)).getText()+".java");
					File classFile;

					if(path != null) {
						System.out.println("NOT NULL");
						classFile = new File(path);
						editor.openFile(classFile);
					}else {
						System.out.println("NULL");
						//Falta ir buscar o ficheiro da classe onde está a nested class
					}

					System.out.println(((Label)umlClass.getChildren().get(1)).getText()+".java");
				}
			});*/

			HashMap<String, String> connections = umlclass.getConnectionsList();

			table.put(((Label)umlclass.getChildren().get(1)).getText()+".java", connections);
			classFigures.put(((Label)umlclass.getChildren().get(1)).getText()+".java", umlclass);			
			
			return umlclass;
		}

		@Override
		public String getText(Object element) {

			if (element instanceof EntityConnectionData) {
				EntityConnectionData data = (EntityConnectionData) element;

				String source = ((Label)((UMLClass)data.source).getChildren().get(1)).getText()+".java";
				String dest = ((Label)((UMLClass)data.dest).getChildren().get(1)).getText()+".java";

				HashMap<String, String> auxConnect = table.get(source);

				String connection = auxConnect.get(dest);

				if(connection.equals("extends")) {
					return "extends";
				}
				if(connection.equals("implements")) {
					return "implements";
				}
				if(connection.equals("nested")) {
					return "nested";
				}
			}
			return "qq coisa";
		}

		@Override
		public int getConnectionStyle(Object rel) {
			if ( rel instanceof EntityConnectionData ) {
				EntityConnectionData data = (EntityConnectionData) rel;

				String source = ((Label)((UMLClass)data.source).getChildren().get(1)).getText()+".java";
				String dest = ((Label)((UMLClass)data.dest).getChildren().get(1)).getText()+".java";

				HashMap<String, String> auxConnect = table.get(source);

				String connection = auxConnect.get(dest);

				if(connection.equals("extends")) {
					return ZestStyles.CONNECTIONS_SOLID;
				}else {
					if(connection.equals("implements")) {
						return ZestStyles.CONNECTIONS_DASH;
					}
				}
			}

			return ZestStyles.CONNECTIONS_SOLID;
		}

		@Override
		public Color getColor(Object rel) {
			return ColorConstants.black;
		}

		@Override
		public Color getHighlightColor(Object rel) {
			return null;
		}

		@Override
		public int getLineWidth(Object rel) {
			return 1;
		}

		@Override
		public IFigure getTooltip(Object entity) {
			return null;
		}

		@Override
		public void selfStyleConnection(Object element, GraphConnection connection) {			
			PolylineConnection connectionFig = (PolylineConnection) connection.getConnectionFigure();

			if(connection.getText().equals("nested")) {
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setScale(20, 10);
				decoration.setLineWidth(2);
				decoration.setOpaque(true);
				decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
				connectionFig.setSourceDecoration(decoration);

			}else{
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setScale(20, 10);
				decoration.setLineWidth(2);
				decoration.setOpaque(true);
				decoration.setBackgroundColor(ColorConstants.white);
				connectionFig.setTargetDecoration(decoration);
			}
		}

		@Override
		public void selfStyleNode(Object element, GraphNode node) {

		}		
	}

	private void getFiles(File workspace) {
		ArrayList<File> directories = new ArrayList<>();

		if(!workspace.isFile()) {
			File[] files = workspace.listFiles();
			for(File file: files) {
				if(!file.isFile()) {
					directories.add(file);
				}else {
					if(file.getName().endsWith(".java")) {
						classList.add(file.getAbsolutePath());
						classPath.put(file.getName(), file.getAbsolutePath());
					}

				}
			}
		}
		for(File file: directories) {
			getFiles(file);
		}

	}

}

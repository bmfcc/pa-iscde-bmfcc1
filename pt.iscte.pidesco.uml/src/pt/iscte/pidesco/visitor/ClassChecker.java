package pt.iscte.pidesco.visitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.swt.graphics.Image;

import pt.iscte.pidesco.uml.UMLClass;
import pt.iscte.pidesco.uml.UMLView;

public class ClassChecker extends ASTVisitor {

	UMLClass umlClass;
	List<UMLClass> innerClasses = new ArrayList<>();

	public List<UMLClass> getInnerClasses() {
		return innerClasses;
	}

	public UMLClass getUMLClass() {
		return umlClass;
	}

	private static int sourceLine(ASTNode node) {
		return ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition());
	}

	// visits class/interface declaration
	@Override
	public boolean visit(TypeDeclaration node) {
		String type;
		Label classLabel;
		Label classType;
		String name = node.getName().toString();

		if(node.isInterface()) {
			type = "Interface";
			classLabel = new Label(name, new Image(
					null, UMLView.class.getResourceAsStream("images\\interface.png")));
			classType = new Label("<<Java " + type + ">>");
		}else {
			type = "Class";
			classLabel = new Label(name, new Image(
					null, UMLView.class.getResourceAsStream("images\\class.png")));
			classType = new Label("<<Java " + type + ">>");
		}

		UMLClass newUmlClass;
		newUmlClass = new UMLClass(classLabel, classType);

		if(node.getSuperclassType() != null) {
			newUmlClass.setConnection(node.getSuperclassType().toString(), "extends");
		}
		List superInterfaceTypes = node.superInterfaceTypes();

		for (Iterator itSuperInterfacesIterator = superInterfaceTypes.iterator(); itSuperInterfacesIterator.hasNext();) {
			Object next = itSuperInterfacesIterator.next();
			if (next instanceof SimpleType) {
				newUmlClass.setConnection(next.toString(), "implements");
			}
		}
		
		for(MethodDeclaration method: node.getMethods()) {
			String methodName = method.getName().toString();
			StringBuilder methodStr = new StringBuilder(methodName);

			StringJoiner params = new StringJoiner(",", "(", ")");

			class ParamsVisitor extends ASTVisitor {

				// visits variable declarations in parameters
				@Override
				public boolean visit(SingleVariableDeclaration node) {
					params.add(node.getType().toString());
					return true;

				}

			}

			ParamsVisitor paramsVisitor = new ParamsVisitor();
			method.accept(paramsVisitor);

			methodStr.append(params.toString());

			Label methodLabel;
			if(method.getReturnType2()!=null) {
				methodStr.append(": " + method.getReturnType2().toString());
			}

			methodLabel = new Label(methodStr.toString());
			newUmlClass.getMethodsCompartment().add(methodLabel);
		}

		if(umlClass==null)
			umlClass = newUmlClass;
		else {
			umlClass.setConnection(node.getName().toString(), "nested");
			innerClasses.add(newUmlClass);
		}

		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	// visits Enum declaration
	@Override
	public boolean visit(EnumDeclaration node) {
		String type = "Enumeration";

		String name = node.getName().toString();

		Label classLabel = new Label(name, new Image(
				null, UMLView.class.getResourceAsStream("images\\enum.png")));
		Label classType = new Label("<<Java" + type + ">>");

		UMLClass newUmlClass;
		newUmlClass = new UMLClass(classLabel, classType);

		if(umlClass == null)
			umlClass = newUmlClass;
		else
			innerClasses.add(newUmlClass);

		//System.out.println("Parsing class " + name + ", starting on line " + sourceLine(node));
		return true;
	}

	// visits attributes
	@Override
	public boolean visit(FieldDeclaration node) {

		// loop for several variables in the same declaration
		for(Object o : node.fragments()) {
			VariableDeclarationFragment var = (VariableDeclarationFragment) o;			
			
			String name = var.getName().toString();
			Label attribute = new Label(name + ": " + node.getType().toString());

			if(innerClasses.size() == 0)
				umlClass.getAttributesCompartment().add(attribute);
			else
				innerClasses.get(innerClasses.size()-1).getAttributesCompartment().add(attribute);

			if(!name.toUpperCase().equals(name)) {
				//System.out.println("Attributes must have all uppercase " + name + ", starting on line " + sourceLine(node));
			}
			boolean isStatic = Modifier.isStatic(node.getModifiers());

		}
		return false; // false to avoid child VariableDeclarationFragment to be processed again
	}

//	@Override
//	public boolean visit(MethodDeclaration node) {
//		//		System.out.println("Testing InnerClass " + node.getName().toString());
//		if(node.getParent().getParent().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
//			System.out.println("It's a Innerclass!");
//			System.out.println(node.getName().toString());
//		}else {
//
////			System.out.println("Parent of Method -> " + node.getParent().toString());
//			String methodName = node.getName().toString();
//			StringBuilder methodStr = new StringBuilder(methodName);
//
//			StringJoiner params = new StringJoiner(",", "(", ")");
//
//			System.out.println("Method -> " + methodName);
//
//			class ParamsVisitor extends ASTVisitor {
//
//				// visits variable declarations in parameters
//				@Override
//				public boolean visit(SingleVariableDeclaration node) {
//					System.out.println("PARAMS -> " + node.getType().toString());
//					params.add(node.getType().toString());
//					return true;
//
//				}
//
//			}
//
//			ParamsVisitor paramsVisitor = new ParamsVisitor();
//			node.accept(paramsVisitor);
//
//			methodStr.append(params.toString());
//
//			System.out.println("--------------------");
//
//			Label method;
//			if(node.getReturnType2()!=null) {
//				methodStr.append(": " + node.getReturnType2().toString());
//			}
//
//			method = new Label(methodStr.toString());
//
//			if(innerClasses.size() == 0)
//				umlClass.getMethodsCompartment().add(method);
//			else {
//				System.out.println("Existem InnerClasses");
//
//				/*class GetParentClassVisitor extends ASTVisitor {
//					@Override
//					public boolean visit(MethodInvocation node) {
//						//System.out.println(node.getExpression());
//						System.out.println("Name: " + node.getName());
//
//						Expression expression = node.getExpression();
//						if (expression != null) {
//							System.out.println("Expr: " + expression.toString());
//							ITypeBinding typeBinding = expression.resolveTypeBinding();
//							if (typeBinding != null) {
//								System.out.println("Type: " + typeBinding.getName());
//							}
//						}
//						IMethodBinding binding = node.resolveMethodBinding();
//						if (binding != null) {
//							ITypeBinding type = binding.getDeclaringClass();
//							if (type != null) {
//								System.out.println("Decl: " + type.getName());
//							}
//						}
//
//						return true;
//					}
//				}
//
//				GetParentClassVisitor getParentClass = new GetParentClassVisitor();
//
//				Block block =node.getBody();
//
//				block.accept(getParentClass);*/
//
//				//				System.out.println("CHECK1 -> " + node.getParent());
//				//				System.out.println("CHECK2 -> " + ASTNode.CLASS_INSTANCE_CREATION);
//				innerClasses.get(innerClasses.size()-1).getMethodsCompartment().add(method);
//			}
//		}
//		return true;
//	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return true;
	}

	// visits variable declarations in parameters
	@Override
	public boolean visit(SingleVariableDeclaration node) {

		String name = node.getName().toString();

		// another visitor can be passed to process the method (parent of parameter) 
		class MethodVisitor extends ASTVisitor {
			// visits methods 
			@Override
			public boolean visit(MethodDeclaration node) {
				String varName = node.getName().toString();
				if(!Character.isLowerCase(varName.charAt(0))) {
					//System.out.println("Method must start with a lowercase " + varName + ", starting on line " + sourceLine(node));
				}
				if(varName.contains("_")) {
					//System.out.println("Method can't contain '_' " + varName + ", starting on line " + sourceLine(node));
				}
				return true;
			}
		}
		class AssignVisitor extends ASTVisitor {
			// visits assignments (=, +=, etc)
			@Override
			public boolean visit(Assignment node) {
				String varName = node.getLeftHandSide().toString();
				if(varName.equals(name)) {
					//System.out.println("Parameter should not be changed " + varName + ", starting on line " + sourceLine(node));
				}
				return true;
			}

			// visits post increments/decrements (i++, i--) 
			@Override
			public boolean visit(PostfixExpression node) {
				String varName = node.getOperand().toString();
				if(varName.equals(name)) {
					//System.out.println("Parameter should not be changed " + varName + ", starting on line " + sourceLine(node));
				}
				return true;
			}

			// visits pre increments/decrements (++i, --i)
			@Override
			public boolean visit(PrefixExpression node) {
				String varName = node.getOperand().toString();
				if(varName.equals(name)) {
					//System.out.println("Parameter should not be changed " + varName + ", starting on line " + sourceLine(node));
				}
				return true;
			}
		}
		AssignVisitor assignVisitor = new AssignVisitor();
		node.getParent().accept(assignVisitor);		
		MethodVisitor methodVisitor = new MethodVisitor();
		node.getParent().accept(methodVisitor);
		return true;
	}

	// visits variable declarations
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		String varName = node.getName().toString();
		if(!Character.isLowerCase(varName.charAt(0))) {
			//System.out.println("Variable must start with a lowercase " + varName + ", starting on line " + sourceLine(node));
		}
		if(varName.contains("_")) {
			//System.out.println("Variable can't contain '_' " + varName + ", starting on line " + sourceLine(node));
		}
		return true;
	}
}

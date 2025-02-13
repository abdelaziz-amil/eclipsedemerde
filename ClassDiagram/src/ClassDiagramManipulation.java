import java.net.Inet4Address;
import java.util.Iterator;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;

import ClassDiagram.*;
import ClassDiagram.Class;

public class ClassDiagramManipulation {
	
	// reference directe sur le type void pour faciliter les transformations
	private VoidType voidRef;

	public void sauverModele(String uri, EObject root) {
		Resource resource = null;
		try {
			URI uriUri = URI.createURI(uri);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
			resource = (new ResourceSetImpl()).createResource(uriUri);
			resource.getContents().add(root);
			resource.save(null);
		} catch (Exception e) {
			System.err.println("ERREUR sauvegarde du mod�le : "+e);
			e.printStackTrace();
		}
	}

	public Resource chargerModele(String uri, EPackage pack) {
		Resource resource = null;
		try {
			URI uriUri = URI.createURI(uri);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
			resource = (new ResourceSetImpl()).createResource(uriUri);
			XMLResource.XMLMap xmlMap = new XMLMapImpl();
			xmlMap.setNoNamespacePackage(pack);
			java.util.Map options = new java.util.HashMap();
			options.put(XMLResource.OPTION_XML_MAP, xmlMap);
			resource.load(options);
		}
		catch(Exception e) {
			System.err.println("ERREUR chargement du modele : "+e);
			e.printStackTrace();
		}
		return resource;
	}

	public ModelBase getModelBase(String modelFile) {
		Resource resource = chargerModele(modelFile, ClassDiagramPackage.eINSTANCE);
		if (resource == null) {
			System.err.println(" Erreur de chargement du modele");
			return null;
		}

		TreeIterator<EObject> it = resource.getAllContents();

		ModelBase base = null;
		while(it.hasNext()) {
			EObject obj = (EObject) it.next();
			if (obj instanceof ModelBase) {
				base = (ModelBase) obj;
				break;
			}
		}
		return base;
	}

	
	public void printModel(ModelBase model) {
		for (Class classe : model.getAllClasses()) {
			System.out.println("**** Class " + classe.getName());
			System.out.println("-- attributes : ");
			for (Attribute attr : classe.getAttributes()) {
				System.out.println("	" + attr.getName() + " : " + attr.getType().getName());
			}
			System.out.println("-- methods : ");
			for ( Method method : classe.getMethods()) {
				System.out.print("	" + method.getReturnType().getName() + " " + method.getName() + " ( ");
				for (int i = 0; i < method.getParamTypes().size(); i++) {
					System.out.print(method.getParamNames().get(i) + " : " + method.getParamTypes().get(i).getName());
				}
				System.out.println(" )");
			}
			System.out.println("-- interfaces : ");
			for (Interface inter : classe.getInterfaces()) {
				System.out.println("	" + inter.getName());
			}
		}
		
		for (Interface inter : model.getAllInterfaces()) {
			System.out.println("**** Interface " + inter.getName());
			for ( Method method : inter.getMethods()) {
				System.out.print("	" + method.getReturnType().getName() + " " + method.getName() + " ( ");
				for (int i = 0; i < method.getParamTypes().size(); i++) {
					System.out.print(method.getParamNames().get(i) + " : " + method.getParamTypes().get(i).getName());
				}
				System.out.println(" )");
			}
			System.out.println("-- implemented by : ");
			for (Class class1 : inter.getImplementedBy()) {
				System.out.println(class1.getName());
			}
			
		}
		
        System.out.println("**** Associations");
        for (Association assoc : model.getAllAssociations()) {
            AssociationEnd end1 = assoc.getEnds().get(0);
            AssociationEnd end2 = assoc.getEnds().get(1);
            System.out.println("-- " + end1.get_class().getName() + " [ " + end1.getName() + " , " + end1.getLowerBound() + ".." + end1.getUpperBound() +
                               " ] <- -> " + end2.get_class().getName() + " [ " + end2.getName() + " , " + end2.getLowerBound() + ".." + end2.getUpperBound() + " ]");
        }
	}
	
	public boolean isGetter(Attribute att, Method met) {		
		if (met.getName().length() <= 4) return false;
		String nomCherche = "get"+att.getName().substring(0,1).toUpperCase()+att.getName().substring(1, att.getName().length());
		if (! met.getName().equals(nomCherche)) return false;
		if (met.getParamTypes().size() != 0 ) return false;
		if (met.getReturnType() != att.getType()) return false;
		return true;
	}
	
	public boolean isSetter(Attribute att, Method met) {
		if (met.getName().length() <= 4) return false;
		String nomCherche = "set"+att.getName().substring(0,1).toUpperCase()+att.getName().substring(1, att.getName().length());
		if (! met.getName().equals(nomCherche)) return false;
		if (met.getParamTypes().size() != 1 ) return false;
		if (met.getParamTypes().get(0) != att.getType()) return false;
		if (met.getReturnType() != this.voidRef) return false;
		return true;
	}
	
	public void addGetter(Attribute att) {
		Method met = ClassDiagramFactory.eINSTANCE.createMethod();
		met.setName("get"+att.getName().substring(0,1).toUpperCase()+att.getName().substring(1, att.getName().length()));
		met.setReturnType(att.getType());
		met.setOwner(att.getOwner());
	}

	public void addSetter(Attribute att) {
		Method met = ClassDiagramFactory.eINSTANCE.createMethod();
		met.setName("set"+att.getName().substring(0,1).toUpperCase()+att.getName().substring(1, att.getName().length()));
		met.getParamTypes().add(att.getType());
		met.getParamNames().add("value");
		met.setReturnType(this.voidRef);
		met.setOwner(att.getOwner());
	}
	
	
	public void addAccessors(ModelBase model) {
		
		for (ClassDiagram.Class cl : model.getAllClasses()) {
			for (Attribute att : cl.getAttributes()) {
				if (cl.getMethods().stream().noneMatch(m -> isGetter(att,m))) addGetter(att);
				if (cl.getMethods().stream().noneMatch(m -> isSetter(att,m))) addSetter(att);
			}
		}
	}
	
	public void addInterface(ModelBase modelBase) {
		for (ClassDiagram.Class class1 : modelBase.getAllClasses()) {
			Interface interface1 = ClassDiagramFactory.eINSTANCE.createInterface();
			interface1.setName("I" + class1.getName());
			class1.getInterfaces().add(interface1);
			modelBase.getAllInterfaces().add(interface1);
			interface1.getMethods().addAll(class1.getMethods());
		}
	}
	
	public static void main(String argv[]) {

		ClassDiagramManipulation cdm = new ClassDiagramManipulation();

		System.out.println(" Chargement du modele\n");

		ModelBase model = cdm.getModelBase("model/Banque.xmi");
		
		cdm.voidRef = model.getVoidT();

		cdm.printModel(model);
		
		System.out.println("\n Ajout des accesseurs\n");
		cdm.addAccessors(model);

		cdm.addInterface(model);
		
		cdm.printModel(model);

		System.out.println("\n Sauvegarde du modele");
		cdm.sauverModele("model/BanqueJava.xmi", model);
	}
}
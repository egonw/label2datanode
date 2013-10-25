package com.github.egonw.label2datanode;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JEditorPane;

import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.PathwayElementEvent;
import org.pathvisio.core.model.PathwayElementListener;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Label;
import org.pathvisio.core.view.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.SelectionBox.SelectionListener;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.gui.SwingEngine;


public class ChangePane extends JEditorPane implements SelectionListener, PathwayElementListener, ApplicationEventListener
{

	private static final long serialVersionUID = -4712168314959036704L;

	PathwayElement input;
	final static int maxThreads = 1;
	volatile ThreadGroup threads;
	volatile Thread lastThread;
	
	private final SwingEngine se;
	private GdbManager gdbManager;
	private ExecutorService executor;

	public ChangePane(SwingEngine se) {
		Engine engine = se.getEngine();
		engine.addApplicationEventListener(this);
		VPathway vp = engine.getActiveVPathway();
		if(vp != null) vp.addSelectionListener(this);
		this.se = se;
		this.gdbManager = se.getGdbManager();
		
		addHyperlinkListener(se);
		setEditable(false);
		setContentType("text/html");
				
		executor = Executors.newSingleThreadExecutor();
	}

	protected void convert() {
		if (input == null) {
			System.out.println("Nothing to convert");
			return;
		}
		System.out.println("Converting... " + input.getClass().getName());
		Pathway pw = input.getPathway();
		PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		elt.setMCenterX(input.getMCenterX());
		elt.setMCenterY(input.getMCenterY());
		elt.setMWidth(input.getMWidth());
		elt.setMHeight(input.getMHeight());
		elt.setTextLabel(input.getTextLabel());
		VPathway vp = se.getEngine().getActiveVPathway();
		VPathwayElement veltold = vp.getPathwayElementView(elt);
		VPathwayElement velt = new GeneProduct(vp, elt);

		// remove old stuff
		vp.removeDrawingObject(veltold, true);
		pw.remove(input);

		// add new stuff
		pw.add(elt);
		vp.addObject(velt);
		input = null;
	}
	
	@Override
	public void selectionEvent(SelectionEvent e) {
		System.out.println("Selection: " + e);
		switch(e.type) {
		case SelectionEvent.OBJECT_ADDED:
			System.out.println("OBJECT_ADDED");
			Iterator<VPathwayElement> it = e.selection.iterator();
			while(it.hasNext()) {
				VPathwayElement o = it.next();
				if(o instanceof Label) {
					setInput(((Label)o).getPathwayElement());
					break;
				}
			}
			break;
		case SelectionEvent.OBJECT_REMOVED:
			System.out.println("OBJECT_REMOVED");
			if(e.selection.size() != 0) break;
			System.out.println(" .. ignoring multiple objects");
		case SelectionEvent.SELECTION_CLEARED:
			System.out.println("OBJECT_CLEARED");
			setInput(null);
			break;
		default:
			System.out.println("OTHER: " + e.getClass().getName());
		}
	}

	public void setInput(final PathwayElement e) {
		System.out.println("Setting input: " + e);
		if(e == input) return;

		if(input != null) input.removeListener(this);
		
		if (e == null) {
			input = null;
		} else {
			System.out.println("  type: " + e.getClass().getName());
			input = e;
			input.addListener(this);
		}
	}

	@Override
	public void applicationEvent(ApplicationEvent e) {
		System.out.println("Application: " + e);
		if(e.getType() == ApplicationEvent.Type.VPATHWAY_CREATED) {
			((VPathway)e.getSource()).addSelectionListener(this);
		}
	}

	@Override
	public void gmmlObjectModified(PathwayElementEvent e) {
		System.out.println("PathwayElement: " + e);
	}
		
}
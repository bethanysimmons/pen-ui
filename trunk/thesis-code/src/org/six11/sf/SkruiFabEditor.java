package org.six11.sf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import org.six11.util.Debug;
import org.six11.util.gui.ApplicationFrame;
import org.six11.util.layout.FrontEnd;
import org.six11.util.lev.NamedAction;
import org.six11.util.pen.PenEvent;
import org.six11.util.pen.PenListener;
import org.six11.util.pen.Pt;
import org.six11.util.pen.Sequence;

import static org.six11.util.Debug.bug;
import static org.six11.util.Debug.num;
import static org.six11.util.layout.FrontEnd.*;

/**
 * A self-contained editor instance.
 * 
 * @author Gabe Johnson <johnsogg@cmu.edu>
 */
public class SkruiFabEditor {

  Main main;
  DrawingBufferLayers layers;
  SketchBook model;
  CornerFinder cornerFinder;

  GraphicDebug guibug;
  Map<String, Action> actions;
  private GlassPane glass;
  private static String ACTION_GO = "Go";
  ApplicationFrame af;

  public SkruiFabEditor(Main m) {
    this.main = m;
    af = new ApplicationFrame("SkruiFab (started " + m.varStr("dateString") + " at "
        + m.varStr("timeString") + ")");
    af.setSize(600, 400);
    createActions(af.getRootPane());
    glass = new GlassPane(this);
    af.getRootPane().setGlassPane(glass);
    glass.setVisible(true);
    model = new SketchBook();
    layers = new DrawingBufferLayers(model);

    /*
     * TODO: GLassPane now handles mouse events and generates pen listens.
     * layers.addPenListener(this);
     */
    guibug = new GraphicDebug(layers);
    model.setGuibug(guibug);
    cornerFinder = new CornerFinder(guibug);
    model.setLayers(layers);

    ScrapGrid grid = new ScrapGrid(model);
    CutfilePane cutfile = new CutfilePane();
    glass.addGestureListener(layers);
    glass.addGestureListener(grid);
    glass.addGestureListener(cutfile);
    FrontEnd fe = new FrontEnd();
    fe.add(layers, "layers");
    fe.add(grid, "grid");
    fe.add(cutfile, "cutfile");
    fe.addRule(ROOT, N, "layers", N);
    fe.addRule(ROOT, W, "layers", W);
    fe.addRule(ROOT, S, "layers", S);
    fe.addRule(ROOT, N, "grid", N);
    fe.addRule(ROOT, E, "grid", E);
    fe.addRule(ROOT, E, "cutfile", E);
    fe.addRule(ROOT, S, "cutfile", S);
    fe.addRule("cutfile", N, "grid", S);
    fe.addRule("cutfile", W, "layers", E);
    fe.addRule("cutfile", W, "grid", W);
    af.add(fe);
    af.center();
    af.setVisible(true);
  }

  Container getContentPane() {
    return af.getContentPane();
  }

  public SketchBook getModel() {
    return model;
  }

  private void createActions(JRootPane rp) {
    // 1. Make action map.
    actions = new HashMap<String, Action>();

    // 2. Fill action map with named actions.
    //
    // 2a. Start with keys for toggling layers 0--9
    for (int num = 0; num < 10; num++) {
      final String numStr = "" + num;
      KeyStroke numKey = KeyStroke.getKeyStroke(numStr.charAt(0));
      actions.put("DEBUG " + num, new NamedAction("Toggle Debug Layer " + num, numKey) {
        public void activate() {
          guibug.whackLayerVisibility(numStr);
        }
      });
    }
    //
    // 2b. Now give actions for other commands like printing, saving,
    // launching ICBMs, etc
    // actions.put(ACTION_PRINT, new NamedAction("Print",
    // KeyStroke.getKeyStroke(KeyEvent.VK_P, 0)) {
    // public void activate() {
    // print();
    // }
    // });
    actions.put(ACTION_GO, new NamedAction("Go", KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)) {
      public void activate() {
        go();
      }
    });

    // 3. For those actions with keyboard accelerators, register them to the
    // root pane.
    for (Action action : actions.values()) {
      KeyStroke s = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
      if (s != null) {
        rp.registerKeyboardAction(action, s, JComponent.WHEN_IN_FOCUSED_WINDOW);
      }
    }
  }

  public void go() {
    List<UnstructuredInk> unstruc = model.getUnanalyzedInk();
    for (UnstructuredInk stroke : unstruc) {
      Sequence seq = stroke.getSequence();
      cornerFinder.findCorners(seq);
    }
  }

  /**
   * Handle pen events. If this is called we can be guaranteed that the original mouse event is or
   * could be ink. In other words, this is not a gesture, so it is safe to put it in the ink list.
   */

}

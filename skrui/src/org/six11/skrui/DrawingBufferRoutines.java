package org.six11.skrui;

import java.awt.Color;
import java.util.List;

import org.six11.util.Debug;
import org.six11.util.gui.shape.Circle;
import org.six11.util.pen.DrawingBuffer;
import org.six11.util.pen.Functions;
import org.six11.util.pen.Pt;
import org.six11.util.pen.Sequence;

/**
 * A collection of drawing routines for DrawingBuffer instances.
 * 
 * @author Gabe Johnson <johnsogg@cmu.edu>
 */
public abstract class DrawingBufferRoutines {

  public static void rect(DrawingBuffer db, Pt where, double sx, double sy, Color borderColor,
      Color fillColor, double borderThickness) {
    db.up();
    if (borderColor != null) {
      db.setColor(borderColor);
    }
    if (fillColor != null) {
      db.setFillColor(fillColor);
      db.setFilling(true);
    } else {
      db.setFilling(false);
    }
    if (borderThickness > 0.0) {
      db.setThickness(borderThickness);
    }
    db.moveTo(where.x, where.y);
    db.forward(sy / 2.0);
    db.turn(90);
    db.down();

    db.forward(sx / 2.0);
    db.turn(90);
    db.forward(sy);
    db.turn(90);
    db.forward(sx);
    db.turn(90);
    db.forward(sy);
    db.turn(90);
    db.forward(sx / 2.0);
    db.up();
    if (fillColor != null) {
      db.setFilling(false);
    }
  }

  public static void spline(DrawingBuffer db, List<Pt> ctrl, Color lineColor, double lineThickness,
      int numSteps) {
    Sequence spline = new Sequence();
    int last = ctrl.size() - 1;
    for (int i = 0; i < last; i++) {
      Functions.getSplinePatch(ctrl.get(Math.max(0, i - 1)), ctrl.get(i), ctrl.get(i + 1), ctrl
          .get(Math.min(last, i + 2)), spline, numSteps);
    }
    db.up();
    if (lineColor != null) {
      db.setColor(lineColor);
    }
    if (lineThickness > 0.0) {
      db.setThickness(lineThickness);
    }
    boolean first = true;
    for (Pt pt : spline) {
      db.moveTo(pt.getX(), pt.getY());
      if (first) {
        db.down();
        first = false;
      }
    }
    db.up();
  }

  public static void bug(String what) {
    Debug.out("DrawwingBufferRoutines", what);
  }

  public static void line(DrawingBuffer db, Pt start, Pt end, Color color) {
    db.up();
    db.setColor(color);
    db.setThickness(1.0);
    db.moveTo(start.x, start.y);
    db.down();
    db.moveTo(end.x, end.y);
    db.up();
  }

  public static void seg(DrawingBuffer db, Segment seg, Color color) {
    db.up();
    db.setColor(color);
    db.setThickness(1.0);
    if (seg.getBestType() == Segment.Type.LINE) {
      db.moveTo(seg.start.x, seg.start.y);
      db.down();
      db.moveTo(seg.end.x, seg.end.y);
    } else if (seg.getBestType() == Segment.Type.ARC) {
      CircleArc arc = seg.bestCircle;
      Pt s = seg.start;
      Pt m = arc.mid;
      Pt e = seg.end;
      db.down();
      db.circleTo(s.x, s.y, m.x, m.y, e.x, e.y);
    } else if (seg.getBestType() == Segment.Type.SPLINE) {
      boolean is_up = true;
      for (Pt pt : seg.splinePoints) {
        db.moveTo(pt.x, pt.y);
        if (is_up) {
          db.down();
          is_up = false;
        }
      }
    }
    db.up();
    bug(seg.toString());
  }

  public static void dot(DrawingBuffer db, Pt center, double radius, double thickness,
      Color borderColor, Color fillColor) {
    db.up();
    if (fillColor != null) {
      db.setFillColor(fillColor);
      db.setFilling(true);
    }
    db.setColor(borderColor);
    db.setThickness(thickness);
    Circle circle = new Circle(center.x, center.y, radius);
    db.down();
    db.addShape(circle);
    db.up();
    if (fillColor != null) {
      db.setFilling(false);
    }
  }

  public static void dots(DrawingBuffer db, List<Pt> points, double radius, double thickness,
      Color borderColor, Color fillColor) {
    for (Pt pt : points) {
      dot(db, pt, radius, thickness, borderColor, fillColor);
    }
  }
}

package src;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javafx.scene.canvas.GraphicsContext;

public class PaintModel extends Observable implements Observer {

	public void save(PrintWriter writer) {
		writer.println("Paint Save File Version 1.0");
		for (PaintCommand c : this.commands) {
			int rVal = (int) (c.getColor().getRed() * 256);
			int gVal = (int) (c.getColor().getGreen() * 256);
			int bVal = (int) (c.getColor().getBlue() * 256);
			String s = "";
			s += "\tcolor:" + rVal + "," + gVal + "," + bVal + "\n";
			s += "\tfilled:" + c.isFill();

			if (c instanceof CircleCommand) {
				writer.println("Circle");
				writer.println(s);
				writer.println("\tcenter:(" + ((CircleCommand) c).getCentre().x + ","
						+ ((CircleCommand) c).getCentre().y + ")");
				writer.println("\tradius:" + ((CircleCommand) c).getRadius());
				writer.println("End Circle");
			} else if (c instanceof RectangleCommand) {
				writer.println("Rectangle");
				writer.println(s);
				writer.println(
						"\tp1:(" + ((RectangleCommand) c).getP1().x + "," + ((RectangleCommand) c).getP1().y + ")");
				writer.println(
						"\tp2:(" + ((RectangleCommand) c).getP2().x + "," + ((RectangleCommand) c).getP2().y + ")");
				writer.println("End Rectangle");

			} else if (c instanceof SquiggleCommand) {
				writer.println("Squiggle");
				writer.println(s);
				writer.println("\tpoints");
				for (Point p : ((SquiggleCommand) c).getPoints()) {
					writer.println("\t\tpoint:(" + p.x + "," + p.y + ")");
				}
				writer.println("\tend points");
				writer.println("End Squiggle");

			} else if (c instanceof PolygonCommand) {
				writer.println("Polyline");
				writer.println(s);
				writer.println("\tpoints");
				for (Point p : ((PolygonCommand) c).getPoints()) {
					writer.println("\t\tpoint:(" + p.x + "," + p.y + ")");
				}
				writer.println("\tend points");
				writer.println("End Polyline");

			} else {
				;
			}

		}
		writer.println("End Paint Save File");

	}

	public void reset() {
		for (PaintCommand c : this.commands) {
			c.deleteObserver(this);
		}
		this.commands.clear();
		this.setChanged();
		this.notifyObservers();
	}

	public void addCommand(PaintCommand command) {
		this.commands.add(command);
		command.addObserver(this);
		this.setChanged();
		this.notifyObservers();
	}

	private ArrayList<PaintCommand> commands = new ArrayList<PaintCommand>();

	public void executeAll(GraphicsContext g) {
		for (PaintCommand c : this.commands) {
			c.execute(g);
		}
	}

	/**
	 * We Observe our model components, the PaintCommands
	 */
	@Override
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers();
	}
}

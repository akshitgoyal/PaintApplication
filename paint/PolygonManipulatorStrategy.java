package paint;

import javafx.scene.input.MouseEvent;

public class PolygonManipulatorStrategy extends ShapeManipulatorStrategy {

	PolygonManipulatorStrategy(PaintModel paintModel) {
		super(paintModel);
	}
	
	private PolygonCommand polygonCommand = new PolygonCommand();
	boolean firstClick = true;
	
	public void mouseMoved(MouseEvent e) {
		if(this.polygonCommand != null) {
		Point p = new Point((int)e.getX(), (int)e.getY());
		this.polygonCommand.addTempLine(p);
		}
	}
	
	
	public void mousePressed(MouseEvent e) {
		Point p1 = new Point((int)e.getX(), (int)e.getY());
		
		
		if(e.isPrimaryButtonDown()) {
			this.polygonCommand.add(p1);
			if(firstClick) {
				this.addCommand(polygonCommand);
				firstClick = false;
			}
			
			
		}
		else {
			this.polygonCommand = new PolygonCommand();
			firstClick = true;
		
		}
		
	}
	
	
	
	
	
	
	

}

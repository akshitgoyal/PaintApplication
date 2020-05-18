package src;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;

/**
 * Parse a file in Version 1.0 PaintSaveFile format. An instance of this class
 * understands the paint save file format, storing information about its effort
 * to parse a file. After a successful parse, an instance will have an ArrayList
 * of PaintCommand suitable for rendering. If there is an error in the parse,
 * the instance stores information about the error. For more on the format of
 * Version 1.0 of the paint save file format, see the associated documentation.
 * 
 * @author
 *
 */
public class PaintFileParser {
	private int lineNumber = 0; // the current line being parsed
	private String errorMessage = ""; // error encountered during parse
	private PaintModel paintModel;

	/**
	 * Below are Patterns used in parsing
	 */
	private Pattern pFileStart = Pattern.compile("^PaintSaveFileVersion1.0$");
	private Pattern pFileEnd = Pattern.compile("^EndPaintSaveFile$");

	private Pattern pCircleStart = Pattern.compile("^Circle$");
	private Pattern pCircleEnd = Pattern.compile("^EndCircle$");
	private Pattern pColor = Pattern.compile("^color:([0-9]+),([0-9]+),([0-9]+)$");
	private Pattern pFilled = Pattern.compile("^filled:(true|false)$");
	private Pattern pRadius = Pattern.compile("^radius:([0-9]+)$");
	private Pattern pCenter = Pattern.compile("^center:\\(([0-9]+),([0-9]+)\\)$");

	private Pattern pRectangleStart = Pattern.compile("^Rectangle$");
	private Pattern pRectangleEnd = Pattern.compile("^EndRectangle$");
	private Pattern pP1 = Pattern.compile("^p1:\\(([0-9]+),([0-9]+)\\)$");
	private Pattern pP2 = Pattern.compile("^p2:\\(([0-9]+),([0-9]+)\\)$");

	private Pattern pSquiggleStart = Pattern.compile("^Squiggle$");
	private Pattern pPointsStart = Pattern.compile("^points$");
	private Pattern pPoints = Pattern.compile("^point:\\(([0-9]+),([0-9]+)\\)$");
	private Pattern pPointsEnd = Pattern.compile("^endpoints$");
	private Pattern pSquiggleEnd = Pattern.compile("^EndSquiggle$");

	private Pattern pPolylineStart = Pattern.compile("^Polyline$");
	private Pattern pPolylineEnd = Pattern.compile("^EndPolyline$");

	// ADD MORE!!

	/**
	 * Store an appropriate error message in this, including lineNumber where the
	 * error occurred.
	 * 
	 * @param mesg
	 */
	private void error(String mesg) {
		this.errorMessage = "Error in line " + lineNumber + " " + mesg;
	}

	/**
	 * 
	 * @return the error message resulting from an unsuccessful parse
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Parse the inputStream as a Paint Save File Format file. The result of the
	 * parse is stored as an ArrayList of Paint command. If the parse was not
	 * successful, this.errorMessage is appropriately set, with a useful error
	 * message.
	 * 
	 * @param inputStream the open file to parse
	 * @param paintModel  the paint model to add the commands to
	 * @return whether the complete file was successfully parsed
	 */
	public boolean parse(BufferedReader inputStream, PaintModel paintModel) {
		this.paintModel = paintModel;
		this.errorMessage = "";

		// During the parse, we will be building one of the
		// following commands. As we parse the file, we modify
		// the appropriate command.

		CircleCommand circleCommand = null;
		RectangleCommand rectangleCommand = null;
		SquiggleCommand squiggleCommand = null;
		PolygonCommand polygonCommand = null;
		Point circleCenter = null;
		int circleRadius = 0;
		boolean isFilled = false;
		Color color = null;
		Point rectP1 = null;
		Point rectP2 = null;
		Point point = null;
		ArrayList<Point> pointList = new ArrayList<Point>();
		boolean check = false;
		try {
			int state = 0;
			Matcher m;
			String l;

			this.lineNumber = 0;
			while ((l = inputStream.readLine()) != null) {
				l = l.replaceAll("\\s+", "");

				this.lineNumber++;
				System.out.println(lineNumber + " " + l + " " + state);
				switch (state) {
				case 0:
					m = pFileStart.matcher(l);
					if (m.matches()) {
						state = 27;
						break;
					}
					error("Expected Start of Paint Save File");
					return false;

				case 27:
					if (pCircleStart.matcher(l).matches()) {
						state = 1;
					} else if (pRectangleStart.matcher(l).matches()) {

						state = 7;
					} else if (pSquiggleStart.matcher(l).matches()) {
						state = 13;
					} else if (pPolylineStart.matcher(l).matches()) {
						state = 20;
					} else if (pFileEnd.matcher(l).matches()) {
						state = 28;

					} else if (l.length() == 0) {
						break;
					}
					else {
					error("Expected a shape or file end");
					return false;
					}

				case 1: // Looking for the start of a new object or end of the save file
					if (state == 1) {
						m = pCircleStart.matcher(l);
						if (m.matches()) {

							state = 2;
							break;
						}
						state = 27;
					}

				case 2:
					if (state == 2) {

						m = pColor.matcher(l);
						if (m.matches()) {

								int redColor = Integer.parseInt(m.group(1));
								int blueColor = Integer.parseInt(m.group(2));
								int greenColor = Integer.parseInt(m.group(3));
								if((0 > redColor ||redColor > 255) || (0 > blueColor ||blueColor > 255) ||(0 > greenColor ||greenColor > 255)) {
									error("Expected Valid RGB Color Values");
									return false;
									
								}
								color = Color.rgb(redColor, blueColor, greenColor);

							

							state = 3;
							break;
						}
						error("Expected Color Values");
						return false;
					}

				case 3:

					if (state == 3) {

						m = pFilled.matcher(l);
						if (m.matches()) {
							isFilled = Boolean.parseBoolean(m.group(1));
							state = 4;

							break;
						}
						error("Expected isFilled Value");
						return false;
					}

				case 4:
					if (state == 4) {
						m = pCenter.matcher(l);
						if (m.matches()) {
							int x = Integer.parseInt(m.group(1));
							int y = Integer.parseInt(m.group(2));
							circleCenter = new Point(x, y);
							state = 5;
							break;
						}
						error("Expected Center Value");
						return false;
					}
				case 5:
					if (state == 5) {
						m = pRadius.matcher(l);
						if (m.matches()) {
							circleRadius = Integer.parseInt(m.group(1));
							state = 6;
							break;
						}
						error("Expected Radius Value");
						return false;
					}

				case 6:
					if (state == 6) {

						m = pCircleEnd.matcher(l);
						if (m.matches()) {
							circleCommand = new CircleCommand(circleCenter, circleRadius);
							circleCommand.setColor(color);
							circleCommand.setFill(isFilled);
							this.paintModel.addCommand(circleCommand);
							circleCommand = null;
							circleCenter = null;
							circleRadius = 0;
							color = null;
							isFilled = false;
							state = 27;

							break;

						}
						error("Expected Circle End");
						return false;
					}

				case 7:
					if (state == 7) {
						m = pRectangleStart.matcher(l);

						if (m.matches()) {
							state = 8;

							break;
						}
						state = 27;
					}

				case 8:
					if (state == 8) {
						m = pColor.matcher(l);
						if (m.matches()) {
							int redColor = Integer.parseInt(m.group(1));
							int blueColor = Integer.parseInt(m.group(2));
							int greenColor = Integer.parseInt(m.group(3));
							if((0 > redColor ||redColor > 255) || (0 > blueColor ||blueColor > 255) ||(0 > greenColor ||greenColor > 255)) {
								error("Expected Valid RGB Color Values");
								return false;
								
							}
							color = Color.rgb(redColor, blueColor, greenColor);
							

							state = 9;
							break;
						}
						error("Expected Color Values");
						return false;
					}
				case 9:
					if (state == 9) {
						m = pFilled.matcher(l);
						if (m.matches()) {
							isFilled = Boolean.parseBoolean(m.group(1));
							state = 10;
							break;
						}
						error("Expected isFilled Value");
						return false;
					}

				case 10:
					if (state == 10) {
						m = pP1.matcher(l);
						if (m.matches()) {
							int x = Integer.parseInt(m.group(1));
							int y = Integer.parseInt(m.group(2));
							rectP1 = new Point(x, y);

							state = 11;
							break;
						}
						error("Expected Rectangle P1 Value");
						return false;
					}
				case 11:
					if (state == 11) {
						m = pP2.matcher(l);
						if (m.matches()) {
							int x = Integer.parseInt(m.group(1));
							int y = Integer.parseInt(m.group(2));
							rectP2 = new Point(x, y);
							state = 12;
							break;
						}
						error("Expected Rectangle P2 Value");
						return false;
					}

				case 12:
					if (state == 12) {

						m = pRectangleEnd.matcher(l);
						if (m.matches()) {
							rectangleCommand = new RectangleCommand(rectP1, rectP2);
							rectangleCommand.setFill(isFilled);
							rectangleCommand.setColor(color);
							this.paintModel.addCommand(rectangleCommand);
							rectangleCommand = null;
							color = null;
							isFilled = false;
							rectP1 = null;
							rectP2 = null;
							state = 27;
							break;
						}
						error("Expected Rectangle End");
						return false;
					}

				case 13:

					if (state == 13) {
						m = pSquiggleStart.matcher(l);
						if (m.matches()) {
							state = 14;
							break;

						}
						state = 27;
					}

				case 14:
					if (state == 14) {
						m = pColor.matcher(l);
						if (m.matches()) {
							int redColor = Integer.parseInt(m.group(1));
							int blueColor = Integer.parseInt(m.group(2));
							int greenColor = Integer.parseInt(m.group(3));
							if((0 > redColor ||redColor > 255) || (0 > blueColor ||blueColor > 255) ||(0 > greenColor ||greenColor > 255)) {
								error("Expected Valid RGB Color Values");
								return false;
								
							}
							color = Color.rgb(redColor, blueColor, greenColor);

							state = 15;
							break;
						}
						error("Expected Color Values");
						return false;
					}

				case 15:
					if (state == 15) {
						m = pFilled.matcher(l);
						if (m.matches()) {
							isFilled = Boolean.parseBoolean(m.group(1));
							state = 16;
							break;
						}
						error("Expected isFilled Value");
						return false;
					}

				case 16:
					if (state == 16) {
						m = pPointsStart.matcher(l);
						if (m.matches()) {
							state = 17;
							break;

						}
						error("Expected points start");
						return false;
					}

				case 17:
					if (state == 17) {
						m = pPoints.matcher(l);
						if (m.matches()) {
							int x = Integer.parseInt(m.group(1));
							int y = Integer.parseInt(m.group(2));
							point = new Point(x, y);
							pointList.add(point);
							state = 17;
							break;

						} else if (pPointsEnd.matcher(l).matches()) {
							state = 18;
						} else {
							error("Expected points");
							return false;
						}
					}

				case 18:
					if (state == 18) {
						m = pPointsEnd.matcher(l);
						if (m.matches()) {
							state = 19;
							break;
						}
						error("Expected points end");
						return false;
					}

				case 19:
					if (state == 19) {
						m = pSquiggleEnd.matcher(l);
						if (m.matches()) {
							squiggleCommand = new SquiggleCommand();
							for (Point p : pointList) {
								squiggleCommand.add(p);

							}
							squiggleCommand.setColor(color);
							squiggleCommand.setFill(isFilled);
							this.paintModel.addCommand(squiggleCommand);
							squiggleCommand = null;
							color = null;
							isFilled = false;
							pointList = new ArrayList<Point>();
							state = 27;
							break;

						}
						error("Expected Squiggle end");
						return false;
					}

				case 20:
					if (state == 20) {
						m = pPolylineStart.matcher(l);
						if (m.matches()) {
							state = 21;
							break;

						}
						state = 27;
					}

				case 21:
					if (state == 21) {
						m = pColor.matcher(l);
						if (m.matches()) {
							int redColor = Integer.parseInt(m.group(1));
							int blueColor = Integer.parseInt(m.group(2));
							int greenColor = Integer.parseInt(m.group(3));
							if((0 > redColor ||redColor > 255) || (0 > blueColor ||blueColor > 255) ||(0 > greenColor ||greenColor > 255)) {
								error("Expected Valid RGB Color Values");
								return false;
								
							}
							color = Color.rgb(redColor, blueColor, greenColor);

							state = 22;
							break;
						}
						error("Expected Color Values");
						return false;
					}

				case 22:
					if (state == 22) {
						m = pFilled.matcher(l);
						if (m.matches()) {
							isFilled = Boolean.parseBoolean(m.group(1));
							state = 23;
							break;
						}
						error("Expected isFilled Value");
						return false;
					}

				case 23:
					if (state == 23) {
						m = pPointsStart.matcher(l);
						if (m.matches()) {
							state = 24;
							break;

						}
						error("Expected points start");
						return false;
					}

				case 24:
					if (state == 24) {
						m = pPoints.matcher(l);
						if (m.matches()) {
							int x = Integer.parseInt(m.group(1));
							int y = Integer.parseInt(m.group(2));
							point = new Point(x, y);
							pointList.add(point);
							state = 24;
							break;

						} else if (pPointsEnd.matcher(l).matches()) {
							state = 25;
						} else {
							error("Expected points");
							return false;
						}
					}

				case 25:
					if (state == 25) {
						m = pPointsEnd.matcher(l);
						if (m.matches()) {
							state = 26;
							break;
						}
						error("Expected points end");
						return false;
					}

				case 26:
					if (state == 26) {
						m = pPolylineEnd.matcher(l);
						if (m.matches()) {
							polygonCommand = new PolygonCommand();
							for (Point p : pointList) {
								polygonCommand.add(p);

							}
							polygonCommand.setColor(color);
							polygonCommand.setFill(isFilled);
							this.paintModel.addCommand(polygonCommand);
							polygonCommand = null;
							color = null;
							isFilled = false;
							pointList = new ArrayList<Point>();
							state = 27;
							break;

						}
					}
				case 28:
					if (state == 28) {
						
						m = pFileEnd.matcher(l);
						if (m.matches()) {
							check = true;

						} else {
							error("Expected Last Line");
							
							return false;
						}

					}
				}
			}

			return check;
		} catch (Exception e) {
			return false;

		}

	}
}

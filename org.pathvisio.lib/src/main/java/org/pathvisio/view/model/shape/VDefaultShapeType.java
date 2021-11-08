package org.pathvisio.view.model.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import org.pathvisio.util.ColorUtils;

public class VDefaultShapeType {

	/**
	 * these constants are internal, only for the switch statement below. There is
	 * no relation with the constants defined in ShapeType.
	 */
	public enum Internal {
		DEFAULT_SHAPE, // warning icon
		DEFAULT_ARROWHEAD; // question mark icon
	}

	/**
	 * Internal, Only for general shape types that can be described as a path. The
	 * shapes are constructed as a general path with arbitrary size and then resized
	 * to fit w and h parameters.
	 */
	static public void getPluggableGraphic(Internal st, Graphics2D g) {
		GeneralPath path = new GeneralPath();
		switch (st) {
		case DEFAULT_SHAPE:
			g.scale(0.1, 0.1);
			path.moveTo(190.37201, 177.034);
			path.curveTo(188.13501, 180.698, 184.158, 182.955, 179.87901, 182.955);
			path.lineTo(12.282, 182.955);
			path.curveTo(7.8559995, 182.955, 3.7719994, 180.571, 1.5839996, 176.722);
			path.curveTo(-0.5750003, 172.873, -0.52600026, 168.173, 1.7309996, 164.373);
			path.lineTo(85.842, 15.153);
			path.curveTo(88.05, 11.431, 92.046005, 9.193, 96.364006, 9.193);
			path.lineTo(96.69601, 9.193);
			g.setColor(ColorUtils.hexToColor("#ffd966ff"));
			g.fill(path);
			g.setColor(Color.BLUE);
			path.curveTo(101.14101, 9.3, 105.13701, 11.811, 107.20901, 15.739);
			path.lineTo(190.724, 164.968);
			path.curveTo(192.717, 168.768, 192.629, 173.33101, 190.372, 177.034);
			path.closePath();
			path.moveTo(179.87901, 170.634);
			path.lineTo(96.35401, 21.45401);
			path.lineTo(12.292015, 170.634);
			path.closePath();
			// exclamation mark
			path.append(new Ellipse2D.Double(83.975, 133.06198, 25, 25), false);
			path.closePath();
			path.moveTo(88.44, 125.301);
			path.lineTo(103.887, 125.301);
			path.lineTo(106.838, 64.003006);
			path.lineTo(85.46, 64.003006);
			path.closePath();
			g.fill(path);
			break;
		case DEFAULT_ARROWHEAD:
			g.scale(0.3, 0.3);
			path.moveTo(45.0, 0.0);
			path.lineTo(5.0, 0.0);
			path.curveTo(2.25, 0.0, 0.0, 2.25, 0.0, 5.0);
			path.lineTo(0.0, 45.0);
			path.curveTo(0.0, 47.75, 2.25, 50.0, 5.0, 50.0);
			path.lineTo(45.0, 50.0);
			path.curveTo(47.75, 50.0, 50.0, 47.75, 50.0, 45.0);
			path.lineTo(50.0, 5.0);
			path.curveTo(50.0, 2.25, 47.75, 0.0, 45.0, 0.0);
			path.closePath();
			g.setColor(ColorUtils.hexToColor("#ffd966ff"));
			g.fill(path);
			g.setColor(Color.BLUE);
			path.moveTo(46.0, 45.0);
			path.curveTo(46.0, 45.542, 45.542, 46.0, 45.0, 46.0);
			path.lineTo(5.0, 46.0);
			path.curveTo(4.458, 46.0, 4.0, 45.542, 4.0, 45.0);
			path.lineTo(4.0, 5.0);
			path.curveTo(4.0, 4.458, 4.458, 4.0, 5.0, 4.0);
			path.lineTo(45.0, 4.0);
			path.curveTo(45.542, 4.0, 46.0, 4.458, 46.0, 5.0);
			path.lineTo(46.0, 45.0);
			path.closePath();
			g.fill(path);
			// question mark
			path = new GeneralPath();
			path.append(new Ellipse2D.Double(21.713, 34.318, 7, 7), false);
			g.fill(path);
			path = new GeneralPath();
			path.moveTo(30.896, 8.772);
			path.curveTo(29.265, 7.9810004, 27.386, 7.5920005, 25.267, 7.5920005);
			path.curveTo(22.972, 7.5920005, 20.973, 8.065001, 19.262001, 8.993);
			path.curveTo(17.544, 9.936, 16.236, 11.1189995, 15.343001, 12.555);
			path.curveTo(14.45, 13.978, 14.0, 15.394, 14.0, 16.787);
			path.curveTo(14.0, 17.457, 14.281, 18.082, 14.848, 18.676);
			path.curveTo(15.408999, 19.241001, 16.105999, 19.537, 16.924, 19.537);
			path.curveTo(18.319, 19.537, 19.265999, 18.705, 19.768, 17.049);
			path.curveTo(20.295, 15.474999, 20.94, 14.271999, 21.703, 13.459);
			path.curveTo(22.464998, 12.641999, 23.648998, 12.233999, 25.266998, 12.233999);
			path.curveTo(26.644, 12.233999, 27.769, 12.639999, 28.641998, 13.438999);
			path.curveTo(29.512999, 14.251999, 29.951998, 15.240999, 29.951998, 16.418999);
			path.curveTo(29.951998, 17.020998, 29.804998, 17.578999, 29.522997, 18.078999);
			path.curveTo(29.233997, 18.593998, 28.879997, 19.062998, 28.467997, 19.475998);
			path.curveTo(28.048996, 19.900997, 27.364996, 20.522999, 26.428997, 21.341997);
			path.curveTo(25.356997, 22.282997, 24.506996, 23.084997, 23.880997, 23.769997);
			path.curveTo(23.248997, 24.455997, 22.742996, 25.233997, 22.358997, 26.151997);
			path.curveTo(21.980997, 27.051996, 21.788998, 28.110996, 21.788998, 29.350996);
			path.curveTo(21.788998, 30.325996, 22.047998, 31.071997, 22.571999, 31.567997);
			path.curveTo(23.090998, 32.063995, 23.734, 32.317997, 24.494999, 32.317997);
			path.curveTo(25.959, 32.317997, 26.828999, 31.549997, 27.114998, 30.024998);
			path.curveTo(27.275997, 29.311998, 27.394999, 28.813997, 27.472998, 28.518997);
			path.curveTo(27.556997, 28.237997, 27.664997, 27.956997, 27.814999, 27.661997);
			path.curveTo(27.963999, 27.380997, 28.189999, 27.059998, 28.489998, 26.716997);
			path.curveTo(28.783998, 26.371998, 29.187998, 25.980997, 29.683998, 25.513998);
			path.curveTo(31.488998, 23.903997, 32.734997, 22.760998, 33.434, 22.075998);
			path.curveTo(34.130997, 21.403997, 34.732998, 20.589998, 35.237, 19.645998);
			path.curveTo(35.744, 18.705, 36.0, 17.609, 36.0, 16.362);
			path.curveTo(36.0, 14.787999, 35.559, 13.311999, 34.667, 11.973999);
			path.curveTo(33.777, 10.621, 32.521, 9.55, 30.896, 8.772);
			path.closePath();
			g.fill(path);
			break;
		default:
			break;
		}
	}

}

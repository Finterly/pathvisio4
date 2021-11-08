package org.pathvisio.view.model.shape;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import junit.framework.TestCase;

public class VDefaultShapeTypeTest extends TestCase {
	
	public void testShape() {
		
		Graphics2D g = VDefaultShapeType.getPluggableShape(VDefaultShapeType.Internal.DEFAULT_SHAPE, g);

		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setBounds(30, 30, 450, 450);
		window.getContentPane().add(g);
		window.setVisible(true);
		window.getContentPane().setBackground(Color.WHITE);

	}
}

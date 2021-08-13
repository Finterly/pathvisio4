package org.pathvisio.view.model;

public class VUtils {
	
	
	// starty for shapes
		public double getMTopFromCenterY(double centerY, double height) {
			return centerY - height / 2;
		}

		public double getCenterYFromMTop(double mTop, double height) {
			return mTop + height / 2;
		}

		// startx for shapes
		public double getMLeft() {
			return mCenterx - mWidth / 2;
		}

		public void setMLeft(double v) {
			mCenterx = v + mWidth / 2;
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(this));
		}
		
	gdata.getMLeft();
	
	gdata.getMTop();

}

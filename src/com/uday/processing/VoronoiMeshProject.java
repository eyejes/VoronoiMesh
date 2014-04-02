package com.uday.processing;

import java.util.ArrayList;
import java.util.Arrays;

import megamu.mesh.MPolygon;
import megamu.mesh.Voronoi;
import processing.core.PApplet;

public class VoronoiMeshProject extends PApplet {

	/**
	 * @param args
	 */

	ArrayList<Float> pointsArray = new ArrayList<Float>();
	float[][] points = new float[3][2];
	Voronoi myVoronoi;
	int pointCounter;
	float easing = (float) 0.005;
	float targetX = 0;
	float targetY = 0;
	float threshold = 25;
	float dx;
	float dy;
	boolean newPointAdded;

	public void setup() {

		pointsArray.add((float) 200);
		pointsArray.add((float) 100);
		pointsArray.add((float) 400);
		pointsArray.add((float) 500);
		pointsArray.add((float) 320);
		pointsArray.add((float) 113);

		pointCounter = pointsArray.toArray().length / 2;
		println(pointCounter);
		size(displayWidth, displayHeight);

		newPointAdded = false;
	}

	public void draw() {

		points = new float[(pointsArray.size() / 2)][2];
		
		if( (newPointAdded || (pow(targetX - points[pointCounter - 1][0],2))+(pow(targetY - points[pointCounter - 1][1],2)) < threshold) ){
			int randomPoint = pointCounter - 1;
			while(randomPoint == pointCounter - 1){
				randomPoint = (int)random(pointCounter - 1);
			}
			targetX = pointsArray.get(2*randomPoint);
			targetY = pointsArray.get(2*randomPoint + 1);
			dx =  targetX - pointsArray.get(2*(pointCounter - 1));
			dy = targetY - pointsArray.get(2*(pointCounter - 1) + 1);
			println(dx,dy, newPointAdded);
			newPointAdded = false;
		}
		
		else{
			float px = pointsArray.get(2*(pointCounter - 1));
			float py = pointsArray.get(2*(pointCounter - 1) + 1);
			
			pointsArray.remove(pointsArray.size() - 1);
			pointsArray.remove(pointsArray.size() - 1);
			
			pointsArray.add(pointsArray.size(), px + easing*dx);
			pointsArray.add(pointsArray.size(), py + easing*dy);
		}
		
		for (int i = 0; i < pointsArray.size(); i = i + 2) {
			points[i / 2][0] = pointsArray.get(i);
			points[i / 2][1] = pointsArray.get(i + 1);
		}

		myVoronoi = new Voronoi(points);
		MPolygon[] myRegions = myVoronoi.getRegions();

		for (int i = 0; i < myRegions.length; i++) {
			// an array of points
			float[][] regionCoordinates = myRegions[i].getCoords();
			fill(0);
			strokeWeight(5);
			stroke(200);
			if (i == pointCounter - 1) {
				fill(255);
			}
			myRegions[i].draw(this); // draw this shape
		}

		// mouseMoved() ;

	}

	public void mousePressed() {
		boolean pointAlreadyInList = false;
		for (int i = 0; i < pointCounter; i++) {
			if (points[i][0] == (float) mouseX
					&& points[i][1] == (float) mouseY) {
				pointAlreadyInList = true;
			}
		}

		if (pointAlreadyInList == false) {
			pointsArray.add(pointsArray.size(), (float) mouseX);
			pointsArray.add(pointsArray.size(), (float) mouseY);
			pointCounter = pointsArray.size() / 2;

			newPointAdded = true;
		}

	}

	public void mouseDragged() {

		boolean pointAlreadyInList = false;
		for (int i = 0; i < pointCounter / 2; i++) {
			if (points[i][0] == (float) mouseX
					&& points[i][1] == (float) mouseY) {
				println("point already in array");
				pointAlreadyInList = true;
			}
		}

		if (pointAlreadyInList == false) {
			pointsArray.remove(pointsArray.size() - 1);
			pointsArray.remove(pointsArray.size() - 1);

			pointsArray.add(pointsArray.size(), (float) mouseX);
			pointsArray.add(pointsArray.size(), (float) mouseY);

			pointCounter = pointsArray.size() / 2;
		}

	}

	// public void mouseMoved() {
	// pointsArray.remove(pointsArray.size()-1);
	// pointsArray.remove(pointsArray.size()-1);
	//
	// pointsArray.add(pointsArray.size(), (float) mouseX);
	// pointsArray.add(pointsArray.size(), (float) mouseY);
	//
	// pointCounter = pointsArray.size() - 1;
	//
	// }

	float[][] addElement(float[][] org, float addedX, float addedY) {
		float[][] result = Arrays.copyOf(org, org.length + 2);
		result[org.length / 2][0] = addedX;
		result[org.length / 2][1] = addedY;

		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main(new String[] { "--present",
				"com.uday.processing.VoronoiMeshProject" });
	}

}

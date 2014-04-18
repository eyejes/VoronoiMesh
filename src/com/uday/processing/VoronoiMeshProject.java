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
	float easing = (float) 0.0025;
	float targetX = 0;
	float targetY = 0;
	float threshold = 200;
	float dx;
	float dy;
	boolean newPointAdded;
	float px;
	float py;
	int heartBeat = 0;
	boolean heartBeatUp = true;

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

		if ((newPointAdded || (pow(
				targetX - pointsArray.get(2 * (pointCounter - 1)), 2))
				+ (pow(targetY - pointsArray.get(2 * (pointCounter - 1) + 1), 2)) < threshold)) {
			println("hit target");
			int randomPoint = pointCounter - 1;
			while (randomPoint == pointCounter - 1) {
				randomPoint = (int) (random(67867) % (pointCounter - 1));
				println(randomPoint);
			}
			targetX = pointsArray.get(2 * randomPoint);
			targetY = pointsArray.get(2 * randomPoint + 1);
			dx = targetX - pointsArray.get(2 * (pointCounter - 1));
			dy = targetY - pointsArray.get(2 * (pointCounter - 1) + 1);
			println(targetX, targetY, newPointAdded);
			newPointAdded = false;
		}

		else {
			px = pointsArray.get(2 * (pointCounter - 1));
			py = pointsArray.get(2 * (pointCounter - 1) + 1);

			// if (px < 5 || px > displayWidth - 5 || py < 5
			// || py > displayHeight - 5) {
			// newPointAdded = true;
			// }

			pointsArray.remove(pointsArray.size() - 1);
			pointsArray.remove(pointsArray.size() - 1);

			dx = targetX - px;
			dy = targetY - py;
			// if (abs(dx) > 1) {
			px += dx * easing;
			// }

			// if (abs(dy) > 1) {
			py += dy * easing;
			// }

			pointsArray.add(pointsArray.size(), px);
			pointsArray.add(pointsArray.size(), py);
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
			for (int j = 0; j < points.length; j++) {
				float ellipseSizeX = 15;
				float ellipseSizeY = 15;

				if (j == pointCounter - 1) {

					if (heartBeatUp && heartBeat < 100) {
						heartBeat++;
					}

					else if (!heartBeatUp && heartBeat > 0) {
						heartBeat--;
					}

					else {
						if (heartBeatUp) {
							heartBeatUp = false;
							heartBeat = 100;
						}

						else {
							heartBeatUp = true;
							heartBeat = 0;
						}
					}

					stroke(0xFF, 0x77, 0x6D);
					fill(0xE8, 0x2C, 0x0C);
					ellipseSizeX = 20 + heartBeat / 8;
					ellipseSizeY = 20 + heartBeat / 8;

					ellipse(points[j][0], points[j][1], ellipseSizeX,
							ellipseSizeY);
				} else {
					strokeWeight(5);
					ellipse(points[j][0], points[j][1], ellipseSizeX,
							ellipseSizeY);
				}

			}
		}

		stroke(255);
		textSize(30);
		text("px: " + px, 20, 20);
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

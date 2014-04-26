package com.uday.processing;

import java.util.ArrayList;
import java.util.Arrays;

import megamu.mesh.MPolygon;
import megamu.mesh.Voronoi;
import processing.core.PApplet;
import processing.core.PImage;
import SimpleOpenNI.SimpleOpenNI;
import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;

public class VoronoiMeshProject extends PApplet {

	/**
	 * @param args
	 */

	ArrayList<Float> pointsArray = new ArrayList<Float>();
	float[][] points = new float[3][2];
	Voronoi myVoronoi;
	int pointCounter;
	float easing = (float) 0.05;
	float targetX = 0;
	float targetY = 0;
	float threshold = 200;
	float dx;
	float dy;
	float dxoriginal;
	float dyoriginal;

	boolean newPointAdded;
	float px;
	float py;
	int heartBeat = 0;
	boolean heartBeatUp = true;
	SimpleOpenNI context;
	PImage depthImage;
	int minDepth = 200;
	int maxDepth = 2400;// 1600 at gallery;
	float worldRecord = 500;
	BlobDetection mBlobDetection;
	int agencyIndexX;
	int agencyIndexY;
	int canvasSizeX;
	int canvasSizeY;
	int oldBlobSize;
	int newBlobSize;
	float blobThreshold = (float) 0.15;

	public void setup() {

		canvasSizeX = displayWidth;
		canvasSizeY = displayHeight;

		pointsArray.add((float) 200);
		pointsArray.add((float) 100);
		// pointsArray.add((float) 400);
		// pointsArray.add((float) 500);
		// pointsArray.add((float) 320);
		// pointsArray.add((float) 113);

		pointCounter = pointsArray.toArray().length / 2;
		println(pointCounter);
		size(canvasSizeX, canvasSizeY);

		newPointAdded = false;

		initOpenNi();
		initBlobDetection();
		frameRate(10);
	}

	public void draw() {

		manageContext(false);
		findBlobs(depthImage);
		manageTessellation();

		// drawBlobsAndEdges(true, true);

	}

	public void manageTessellation() {

		points = new float[(pointsArray.size() / 2)][2];

		agencyIndexX = 0;// 2 * (pointCounter - 1);
		agencyIndexY = 0;// 2 * (pointCounter - 1) + 1;

		if ((newPointAdded || (pow(targetX - pointsArray.get(agencyIndexX), 2))
				+ (pow(targetY - pointsArray.get(agencyIndexY), 2)) < threshold)) {
			println("hit target");
			// int randomPoint = pointCounter - 1;
			// while (randomPoint == pointCounter - 1) {
			// randomPoint = (int) (random(67867) % (pointCounter - 1));
			// //println(randomPoint);
			// }
			// targetX = pointsArray.get(2 * randomPoint);
			// targetY = pointsArray.get(2 * randomPoint + 1);

			targetX = random(canvasSizeX);
			targetY = random(canvasSizeY);

			dx = targetX - pointsArray.get(agencyIndexX);
			dy = targetY - pointsArray.get(agencyIndexY);
			dxoriginal = dx;
			dyoriginal = dy;
			println(targetX, targetY, newPointAdded);
			newPointAdded = false;
		}

		else {
			px = pointsArray.get(agencyIndexX);
			py = pointsArray.get(agencyIndexY);

			// if (px < 5 || px > displayWidth - 5 || py < 5
			// || py > displayHeight - 5) {
			// newPointAdded = true;
			// }

			pointsArray.remove(0);
			pointsArray.remove(0);

			dx = targetX - px;
			dy = targetY - py;
			// if (abs(dx) > 1) {
			px += dx * easing;
			// }

			// if (abs(dy) > 1) {
			py += dy * easing;
			// }

			pointsArray.add(0, px);
			pointsArray.add(0, py);
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
			if (i == 0) {
				fill(255);
			}
			myRegions[i].draw(this); // draw this shape
		}

		for (int j = 0; j < pointCounter; j++) {
			float ellipseSizeX = 15;
			float ellipseSizeY = 15;

			if (j == 0) {

				if (heartBeatUp && heartBeat < 50) {
					heartBeat++;
				}

				else if (!heartBeatUp && heartBeat > 0) {
					heartBeat--;
				}

				else {
					if (heartBeatUp) {
						heartBeatUp = false;
						heartBeat = 50;
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

				ellipse(points[j][0], points[j][1], ellipseSizeX, ellipseSizeY);
			} else {
				strokeWeight(5);
				stroke(255);
				fill(100);
				ellipse(points[j][0], points[j][1], ellipseSizeX, ellipseSizeY);
			}
		}

		stroke(255);
		textSize(30);
		addBlobPoints();
		// text("px: " + px, 20, 20);
		// mouseMoved() ;
	}

	public void initOpenNi() {
		context = new SimpleOpenNI(this);
		if (context.isInit() == false) {
			println("Can't init SimpleOpenNI, maybe the camera is not connected!");
			exit();
			return;
		}

		// disable mirror
		context.setMirror(false);

		// enable depthMap generation
		context.enableDepth();
	}

	public void initBlobDetection() {
		depthImage = new PImage(context.depthWidth(), context.depthHeight());
		mBlobDetection = new BlobDetection(depthImage.width, depthImage.height);
		mBlobDetection.setPosDiscrimination(true);
		mBlobDetection.setThreshold(0.2f);
	}

	public void manageContext(boolean drawDepth) {
		context.update();
		depthImage = context.depthImage();
		// println(depthImage.width,depthImage.height);

		int[] depthMap = context.depthMap();
		int steps = 3; // to speed up the drawing, draw every third point
		int index;
		int closestX = 0;
		int closestY = 0;
		int trackColor = color(0x00, 0xFF, 0x00, 0xFF);
		int sumX = 0;
		int sumY = 0;
		int count = 0;

		for (int y = 0; y < context.depthHeight(); y++) {
			for (int x = 0; x < context.depthWidth(); x++) {
				index = x + y * context.depthWidth();

				if (depthMap[index] >= minDepth && depthMap[index] <= maxDepth) {
					depthImage.pixels[index] = 0xFF00FF00;
					sumX += x;
					sumY += y;
					count++;
				} else {
					depthImage.pixels[index] = 0;
				}

			}
		}
		if (drawDepth) {
			image(depthImage, 0, 0);

		}
		if (count != 0) {
			closestX = sumX / count;
			closestY = sumY / count;
		}

		sumX = 0;
		sumY = 0;
		count = 0;
	}

	public void findBlobs(PImage image) {
		mBlobDetection.computeBlobs(image.pixels);

	}

	public void addBlobPoints() {
		Blob b;

		// pointsArray.clear();
		// pointsArray.add((float) 200);
		// pointsArray.add((float) 100);
		// pointCounter = pointsArray.size() / 2;

		newBlobSize = mBlobDetection.getBlobNb();
		if (oldBlobSize == newBlobSize) {
			for (int n = 0; n < mBlobDetection.getBlobNb(); n++) {
				b = (Blob) mBlobDetection.getBlob(n);
				if (b != null) {

					if (b.h > 0.4 && b.w > 0.4) {
						boolean pointAlreadyInList = false;
						for (int i = 0; i < pointCounter; i++) {
							if (abs(points[i][0] - (float) b.x * canvasSizeX) <= 160
									&& (points[i][1] - (float) b.y
											* canvasSizeY) <= 160) {
								pointAlreadyInList = true;
							}

							else {
								pointsArray.remove(i*2);
								pointsArray.remove(i*2);
								pointsArray.add(i*2, (float) b.x
										* canvasSizeX);
								pointsArray.add(i*2+1, (float) b.y
										* canvasSizeY);
								pointCounter = pointsArray.size() / 2;

								//newPointAdded = true;
							}

						}

					}

				}

			}
		}

		else {

			pointsArray.clear();
			pointsArray.add((float) px);
			pointsArray.add((float) py);
			pointCounter = pointsArray.size() / 2;
			for (int n = 0; n < mBlobDetection.getBlobNb(); n++) {
				b = (Blob) mBlobDetection.getBlob(n);
				if (b != null) {

					if (b.h > blobThreshold && b.w > blobThreshold) {
						boolean pointAlreadyInList = false;
						for (int i = 0; i < pointCounter; i++) {
							try{
							if (abs(points[i][0] - (float) b.x * canvasSizeX) <= 80
									&& (points[i][1] - (float) b.y
											* canvasSizeY) <= 80) {
								pointAlreadyInList = true;
							}
							}
							catch(Exception e){
								
							}

						}

						if (pointAlreadyInList == false) {

							pointsArray.add(pointsArray.size(), (float) b.x
									* canvasSizeX);
							pointsArray.add(pointsArray.size(), (float) b.y
									* canvasSizeY);
							pointCounter = pointsArray.size() / 2;

							newPointAdded = true;
						}
					}

				}

			}
		}

		oldBlobSize = newBlobSize;

	}

	void drawBlobsAndEdges(boolean drawBlobs, boolean drawEdges) {
		noFill();
		Blob b;
		EdgeVertex eA, eB;
		for (int n = 0; n < mBlobDetection.getBlobNb(); n++) {
			b = (Blob) mBlobDetection.getBlob(n);
			if (b != null) {
				// Edges
				if (drawEdges) {
					strokeWeight(3);
					stroke(0, 255, 0);
					for (int m = 0; m < b.getEdgeNb(); m++) {
						eA = b.getEdgeVertexA(m);
						eB = b.getEdgeVertexB(m);
						if (eA != null && eB != null)
							line(eA.x * width, eA.y * height, eB.x * width,
									eB.y * height);
					}
				}

				// Blobs
				if (drawBlobs) {
					if (b.h > 0.4 && b.w > 0.4) {
						strokeWeight(1);
						stroke(255, 0, 0);
						rect(b.xMin * width, b.yMin * height, b.w * width, b.h
								* height);

						ellipse((float) (b.xMin + b.w * (float) 0.5) * width,
								(float) (b.yMin + b.h * (float) 0.5) * height,
								30, 30);
					}

				}

			}

		}
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

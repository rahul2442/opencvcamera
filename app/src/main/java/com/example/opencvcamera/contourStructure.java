package com.example.opencvcamera;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

public class contourStructure {
    double area;
    org.opencv.core.Point[] point;
    MatOfPoint matOfPoint;
    Rect rect;
    contourStructure(double area, org.opencv.core.Point[] point,MatOfPoint matOfPoint,Rect rect){
        this.area = area;
        this.point = point;
        this.matOfPoint = matOfPoint;
        this.rect = rect;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public org.opencv.core.Point[] getPoint() {
        return point;
    }

    public void setPoint(Point[] point) {
        this.point = point;
    }

    public MatOfPoint getMatOfPoint() {
        return matOfPoint;
    }

    public void setMatOfPoint(MatOfPoint matOfPoint) {
        this.matOfPoint = matOfPoint;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}

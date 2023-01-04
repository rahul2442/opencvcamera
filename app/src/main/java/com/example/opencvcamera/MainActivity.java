package com.example.opencvcamera;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    int flag = 0;
    Mat temp;
    ImageView img ;
    int counter=0;
    Boolean value = true;
    TextView width;
    TextView height;
    double w,h;
    boolean change = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        height = (TextView)findViewById(R.id.height);

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch(status){

                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }

        };


    }
    public void changeView(View view){
        if(change){
            change=false;
        }
        else
            change=true;
    }



    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        ArrayList<contourStructure> finalContours = new ArrayList<>();
        ArrayList<contourStructure> insideContours = new ArrayList<>();
        List<MatOfPoint> list = new ArrayList<MatOfPoint>();
        Point[] points = new Point[4];
        Point[] insidePoints = new Point[4];
        Mat imgWrap = new Mat();
        Mat src = inputFrame.rgba();





        finalContours=getcontours(src,200000);
        if(finalContours.size()!=0) {
            points = Arrays.copyOf(reorderPoints(finalContours.get(0).getPoint(), src), 4);
            imgWrap = imageWrap(points,src);
            insideContours = getcontours(imgWrap,200);
        }


        if(insideContours.size()!=0){

            insidePoints = Arrays.copyOf(reorderPoints(insideContours.get(0).getPoint(),imgWrap),4);
            for(int i=0 ; i<insideContours.size() ; i++){

                w=findDistance(insideContours.get(i).getPoint()[0],insideContours.get(i).getPoint()[1])/10.0;
                h=findDistance(insideContours.get(i).getPoint()[0],insideContours.get(i).getPoint()[2])/10.0;
                Rect rect = insideContours.get(i).getRect();
                double xm,ym,wm,hm;
                xm = rect.x;
                ym = rect.y;
                wm = rect.width;
                hm = rect.height;



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        height.setText("H-"+String.format("%.2f",h)+" "+"W-"+String.format("%.2f",w));
                    }
                });
                Log.d("finalansweraagya", "onCameraFrame: "+String.format("%.2f",w)+"--"+String.format("%.2f",h));
            }

        }


        Mat gray = new Mat();
        Mat blur = new Mat();
        Mat canny = new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blur , new Size(5,5),1);
        Imgproc.Canny(blur, canny,50,50);
        showObject(src,canny,150000,4000);


        if (!change)return canny;
        return src;


    }

    private void showObject(Mat src,Mat canny, int maxArea, int min) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<contourStructure> finalContours = new ArrayList<>();
        for( int contourIdx=0; contourIdx < contours.size(); contourIdx++ ){
            List<MatOfPoint> list = new ArrayList<MatOfPoint>();
            if(Imgproc.contourArea(contours.get(contourIdx))>min && Imgproc.contourArea(contours.get(contourIdx))<maxArea ){


                MatOfPoint2f approxCurve = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());

                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double peri = Imgproc.arcLength(contour2f,true)*0.02;
                Imgproc.approxPolyDP(contour2f,approxCurve,peri,true);

                //convert to MatofPoint
                MatOfPoint point = new MatOfPoint(approxCurve.toArray());
                Point [] p = approxCurve.toArray();


                //get boundingrect from contour
                Rect rect = Imgproc.boundingRect(point);

                if(point.size().height==4){


                    list.add( new MatOfPoint (
                            p[0],p[1],p[2],p[3]));
                Imgproc.polylines(src, list,true,new Scalar(0,0,255),3);



                }
            }
        }

    }

    private double findDistance(Point point, Point point1) {



        return Math.pow(Math.pow(point.x-point1.x,2)+Math.pow(point.y-point1.y,2),0.5);
    }



    private Point[] reorderPoints(Point[] point,Mat src) {
        ArrayList<Integer> listSum = new ArrayList<>();
        ArrayList<Integer> listdiff = new ArrayList<>();
        int [][] reorder = new int[4][2];
        for(int i=0 ; i<point.length ; i++){
            listSum.add((int)(point[i].x+point[i].y));
        }
        for(int i=0 ; i<point.length ; i++){
            listdiff.add((int)(point[i].x-point[i].y));
        }
        reorder[3][0]=(int)point[listSum.indexOf(Collections.max(listSum))].x;
        reorder[0][0]=(int)point[listSum.indexOf(Collections.min(listSum))].x;
        reorder[2][0]=(int)point[listdiff.indexOf(Collections.max(listdiff))].x;
        reorder[1][0]=(int)point[listdiff.indexOf(Collections.min(listdiff))].x;
        reorder[3][1]=(int)point[listSum.indexOf(Collections.max(listSum))].y;
        reorder[0][1]=(int)point[listSum.indexOf(Collections.min(listSum))].y;
        reorder[2][1]=(int)point[listdiff.indexOf(Collections.max(listdiff))].y;
        reorder[1][1]=(int)point[listdiff.indexOf(Collections.min(listdiff))].y;


        point[0]=new Point(reorder[0][0],reorder[0][1]);
        point[1]=new Point(reorder[1][0],reorder[1][1]);
        point[2]=new Point(reorder[2][0],reorder[2][1]);
        point[3]=new Point(reorder[3][0],reorder[3][1]);

        Log.d("sortedpoints", "reorderPoints: "+ Arrays.toString(point));


        return point;


    }

    private Mat imageWrap(Point[] sortedPoints, Mat source) {
        int width = 210;
        int height = 297;
        MatOfPoint2f src = new MatOfPoint2f(
                sortedPoints[0],
                sortedPoints[1],
                sortedPoints[2],
                sortedPoints[3]);

        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(width,0),
                new Point(0,height),
                new Point(width,height)
        );

        Mat warpMat = Imgproc.getPerspectiveTransform(src,dst);
        //This is you new image as Mat
        Mat destImage = new Mat();
        Imgproc.warpPerspective(source, destImage, warpMat, source.size());


        return destImage;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<contourStructure> getcontours(Mat src,int minArea) {

        Mat gray = new Mat();
        Mat blur = new Mat();
        Mat canny = new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blur , new Size(5,5),1);
        Imgproc.Canny(blur, canny,50,50);
        showObject(src,canny,150000,4000);


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<contourStructure> finalContours = new ArrayList<>();
        for( int contourIdx=0; contourIdx < contours.size(); contourIdx++ ){
            if(Imgproc.contourArea(contours.get(contourIdx))>minArea){


                MatOfPoint2f approxCurve = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());

                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double peri = Imgproc.arcLength(contour2f,true)*0.02;
                Imgproc.approxPolyDP(contour2f,approxCurve,peri,true);

                //convert to MatofPoint
                MatOfPoint point = new MatOfPoint(approxCurve.toArray());
                Point [] p = approxCurve.toArray();


                //get boundingrect from contour
                Rect rect = Imgproc.boundingRect(point);

                if(point.size().height==4){
                    finalContours.add(new contourStructure(Imgproc.contourArea(contours.get(contourIdx)),p,point,rect));

                    Imgproc.drawContours(src,contours,contourIdx,new Scalar(0,255,0),4);


                }
            }
        }
        Collections.sort(finalContours, Comparator.comparing(contourStructure::getArea).reversed());



        return finalContours;
    }




    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem, yo!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){

            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

}


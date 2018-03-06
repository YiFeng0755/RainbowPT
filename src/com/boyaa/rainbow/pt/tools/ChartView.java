package com.boyaa.rainbow.pt.tools;

import org.afree.chart.AFreeChart;
import org.afree.graphics.geom.RectShape;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ChartView extends ImageView
{
    private Bitmap              bitmap;
    private RectShape           rectArea;
    private Canvas              canvas;
    private AFreeChart          chart;

    public ChartView( Context context, AttributeSet attributeSet ){
        super(context, attributeSet);
    }

    public ChartView( Context context ){
        super(context);
        intChart();
    }

    private void intChart(){
    	//Setting different width and height based on the orientation.
    	/*  if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
            rectArea = new RectShape(0.0, 0.0, 600, 600);
        }
        else*/

    	bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
    	rectArea = new RectShape(0.0, 0.0, 800, 600);

    }

    public Bitmap drawChart( AFreeChart chart ){
        canvas = new Canvas(bitmap);
        this.chart = chart;             
        this.chart.draw(canvas, rectArea);
        //setImageBitmap(bitmap);
        return bitmap;
    }

    @Override
    protected void onDraw( Canvas canvas ){
        super.onDraw(canvas);               
    }
}
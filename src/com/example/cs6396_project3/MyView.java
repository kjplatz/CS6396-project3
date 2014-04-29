package com.example.cs6396_project3;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class MyView extends View {
	
	Bitmap bitmap;
	Paint paint;
	int maxX, maxY;
	float xScale, yScale;
	int x, yRect;
	RectF rect;

	private static int cur_y_pos=100;
	
	public void setYPos(int y_pos){
		 /* 6.157 is a scaling factor between UI and our calculation*/ 
		cur_y_pos = (int)(y_pos * yScale);
		if ( cur_y_pos < 10 ) cur_y_pos = 10;
		this.postInvalidate();
	}
	
    public MyView(Context context, DisplayMetrics dm) {
         super(context);
         // TODO Auto-generated constructor stub
         
         paint = new Paint();
         paint.setStyle(Paint.Style.FILL);
         paint.setTextSize(48f);
         maxX = dm.widthPixels;
         maxY = dm.heightPixels;
         InputStream resource = getResources().openRawResource(R.drawable.bg);
         bitmap = BitmapFactory.decodeStream(resource);  
         
         x = 290 * bitmap.getWidth() / 600;
 //        x = 290;
         yScale = (float)bitmap.getHeight() / 136.0f;
         yRect = (int)(12 * yScale);
         
    }

    @Override
    protected void onDraw(Canvas canvas) {
       // TODO Auto-generated method stub
       super.onDraw(canvas);
       int radius=10; 

       paint.setStyle(Paint.Style.FILL);
       paint.setColor(Color.WHITE);
       canvas.drawPaint(paint);
       
       canvas.drawBitmap(bitmap, 0, 0, paint);
       
       // Use Color.parseColor to define HTML colors
       paint.setColor(Color.parseColor("#CD5C5C"));
       canvas.drawCircle(x, cur_y_pos, radius, paint);

       // Use Color.parseColor to define HTML colors
       paint.setColor(Color.BLACK);
       //canvas.drawRect( x, 25, yRect, 75, paint);
       canvas.drawText( "X", x-14, yRect, paint );
   }
}
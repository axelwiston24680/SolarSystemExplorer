package com.axelwiston.solarsystemexplorer;
import android.content.Context;import android.graphics.Canvas;import android.graphics.Paint;import android.util.AttributeSet;import android.view.View;
public class PlanetScaleView extends View {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final String[] labels={"Earth","Neptune","Uranus","Saturn","Jupiter","Sun"};private final float[] sizes={4,15,17,18,42,92};private final int[] colors={0xFF3E91D5,0xFF426FE0,0xFF72D5D0,0xFFE8C778,0xFFD9A66B,0xFFFFC84A};private final float d;
 public PlanetScaleView(Context c){this(c,null);}public PlanetScaleView(Context c,AttributeSet a){super(c,a);d=getResources().getDisplayMetrics().density;}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float y=28*d;for(int i=0;i<labels.length;i++){float r=sizes[i]*d*.55f;float x=24*d+r;p.setColor(colors[i]);c.drawCircle(x,y,r,p);p.setColor(0xFFFFFFFF);p.setTextSize(13*d);c.drawText(labels[i],Math.min(getWidth()-90*d,x+r+12*d),y+5*d,p);y+=Math.max(43*d,r*2+8*d);}p.setColor(0xFFFFD56A);p.setTextSize(12*d);c.drawText("Illustrative relative diameters",12*d,getHeight()-8*d,p);}
}

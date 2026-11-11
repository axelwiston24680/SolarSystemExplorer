package com.axelwiston.solarsystemexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

public class PlanetView extends View {
    public interface OnPlanetSelectedListener { void onPlanetSelected(int index); }
    private OnPlanetSelectedListener listener;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] x = new float[9], y = new float[9], r = new float[9];
    private final int[] colors = {0xFFFFC84A,0xFFB7A18A,0xFFE9B878,0xFF3E91D5,0xFFE45B43,0xFFD9A66B,0xFFE8C778,0xFF72D5D0,0xFF426FE0};
    private final String[] labels = {"Sun","Mercury","Venus","Earth","Mars","Jupiter","Saturn","Uranus","Neptune"};
    private final Random random = new Random(7);

    public PlanetView(Context c) {
        super(c);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    public void setOnPlanetSelectedListener(OnPlanetSelectedListener l) { listener=l; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(), h=getHeight();
        c.drawColor(0xFF101A43);
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<65;i++) {
            float sx=(i*83 % 997)/997f*w, sy=(i*47 % 991)/991f*h;
            p.setColor(i%5==0 ? 0xFFFFD98B : 0xFFB9C9FF);
            c.drawCircle(sx,sy, i%7==0 ? 2.1f : 1.1f,p);
        }
        float cx=w/2f, cy=h/2f;
        float[] orbit={0,42,65,88,111,139,166,191,215};
        for(int i=1;i<9;i++) {
            p.setColor(0x557C91D8); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.2f);
            c.drawOval(new RectF(cx-orbit[i],cy-orbit[i]*.48f,cx+orbit[i],cy+orbit[i]*.48f),p);
        }
        x[0]=cx; y[0]=cy; r[0]=20;
        for(int i=1;i<9;i++) {
            x[i]=cx-orbit[i]*.82f;
            y[i]=cy-orbit[i]*.39f;
            r[i]=i==5?13:(i==6?12:(i==1?5: i==2||i==3||i==4?8:9));
        }
        for(int i=0;i<9;i++) {
            float rad=r[i];
            p.setStyle(Paint.Style.FILL);
            p.setShader(new android.graphics.RadialGradient(x[i]-rad*.35f,y[i]-rad*.4f,rad*1.5f,
                new int[]{lighten(colors[i]),colors[i],0xFF171B35},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawCircle(x[i],y[i],rad,p);
            p.setShader(null);
            if(i==6) {
                p.setColor(0xFFDCC48A); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3.2f);
                c.drawOval(new RectF(x[i]-rad*1.7f,y[i]-rad*.45f,x[i]+rad*1.7f,y[i]+rad*.45f),p);
            }
            p.setStyle(Paint.Style.FILL); p.setColor(0xFFEAF0FF); p.setTextSize(10);
            c.drawText(labels[i],x[i]-p.measureText(labels[i])/2,y[i]+rad+13,p);
        }
        p.setColor(0xFFFFD56A); p.setTextSize(10); p.setStyle(Paint.Style.FILL);
        c.drawText("TAP A PLANET",cx-35,h-8,p);
    }

    private int lighten(int color) {
        return Color.rgb(Math.min(255,Color.red(color)+65),Math.min(255,Color.green(color)+65),Math.min(255,Color.blue(color)+65));
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction()==MotionEvent.ACTION_UP) {
            int best=-1; float distance=Float.MAX_VALUE;
            for(int i=0;i<9;i++) {
                float dx=e.getX()-x[i],dy=e.getY()-y[i],d=dx*dx+dy*dy;
                if(d < Math.pow(r[i]+18,2) && d<distance) { best=i; distance=d; }
            }
            if(best>=0 && listener!=null) { listener.onPlanetSelected(best); return true; }
        }
        return true;
    }
}

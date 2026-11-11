package com.axelwiston.solarsystemexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class PlanetScaleView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String[] labels = {"Earth","Neptune","Uranus","Saturn","Jupiter","Sun"};
    private final float[] sizes = {4,15,17,18,42,92};
    private final int[] colors = {0xFF348FE0,0xFF416DDF,0xFF72D5D0,0xFFE8C778,0xFFD69A5B,0xFFFFC84A};
    private final float d;

    public PlanetScaleView(Context c) { this(c,null); }
    public PlanetScaleView(Context c, AttributeSet a) {
        super(c,a);
        d=getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float y=29*d;
        for(int i=0;i<labels.length;i++) {
            float r=Math.max(4*d,sizes[i]*d*.55f);
            float x=28*d+r;
            if(i==5) {
                p.setShader(new RadialGradient(x,y,r*1.7f,new int[]{0x88FFD76A,0x33FF8A20,0x00FF6A00},null,Shader.TileMode.CLAMP));
                c.drawCircle(x,y,r*1.7f,p);p.setShader(null);
            }
            if(i==3) {
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4*d);p.setColor(0xFFDCC38C);
                c.drawOval(new RectF(x-r*1.7f,y-r*.35f,x+r*1.7f,y+r*.35f),p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setShader(new RadialGradient(x-r*.36f,y-r*.4f,Math.max(2*d,r*1.6f),
                    new int[]{lighten(colors[i]),colors[i],0xFF11152D},null,Shader.TileMode.CLAMP));
            c.drawCircle(x,y,r,p);p.setShader(null);
            c.save();
            c.clipPath(circlePath(x,y,r));
            if(i==0) {
                p.setColor(0xFF39B978);
                c.drawOval(new RectF(x-r*.75f,y-r*.4f,x-r*.08f,y+r*.1f),p);
                c.drawOval(new RectF(x+r*.12f,y-r*.62f,x+r*.65f,y-r*.1f),p);
                p.setColor(0x66FFFFFF);c.drawOval(new RectF(x-r*.5f,y-r*.5f,x+r*.4f,y-r*.28f),p);
            } else if(i==1||i==2) {
                p.setColor(0x557FE9F4);
                for(int j=-1;j<=1;j++)c.drawOval(new RectF(x-r,y+j*r*.45f-r*.07f,x+r,y+j*r*.45f+r*.04f),p);
            } else if(i==3||i==4) {
                for(int j=-2;j<=2;j++) {p.setColor(j%2==0?0x66915D3C:0x66FFF0C5);c.drawRect(x-r,y+j*r*.35f,x+r,y+j*r*.35f+r*.12f,p);}
                if(i==4){p.setColor(0xFFB84F3E);c.drawOval(new RectF(x+r*.2f,y-r*.05f,x+r*.7f,y+r*.24f),p);}
            }
            c.restore();
            if(i==3) {
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f*d);p.setColor(0xFFFFE8B1);
                c.drawArc(new RectF(x-r*1.7f,y-r*.35f,x+r*1.7f,y+r*.35f),195,150,false,p);
            }
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1*d);p.setColor(0x66FFFFFF);c.drawCircle(x,y,r,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFFFFFFF);p.setTextSize(13*d);
            c.drawText(labels[i],Math.min(getWidth()-90*d,x+r+12*d),y+5*d,p);
            y+=Math.max(43*d,r*2+8*d);
        }
        p.setColor(0xFFFFD56A);p.setTextSize(12*d);
        c.drawText("Illustrative relative diameters",12*d,getHeight()-8*d,p);
    }

    private android.graphics.Path circlePath(float x,float y,float r) {
        android.graphics.Path path=new android.graphics.Path();
        path.addCircle(x,y,r,android.graphics.Path.Direction.CW);
        return path;
    }

    private int lighten(int color) {
        return android.graphics.Color.rgb(Math.min(255,android.graphics.Color.red(color)+65),
                Math.min(255,android.graphics.Color.green(color)+65),
                Math.min(255,android.graphics.Color.blue(color)+65));
    }
}

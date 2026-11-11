package com.axelwiston.solarsystemexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;

public class PlanetView extends View {
    public interface OnPlanetSelectedListener { void onPlanetSelected(int index); }
    public interface OnPlanetOpenedListener { void onPlanetOpened(int index); }
    public interface OnWorldFeatureListener { void onWorldFeature(int planet, int feature); }

    private OnPlanetSelectedListener listener;
    private OnPlanetOpenedListener openedListener;
    private OnWorldFeatureListener worldFeatureListener;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] x = new float[9], y = new float[9], radius = new float[9];
    private final float density;
    private final GestureDetector gestures;
    private boolean detailMode = false;
    private int selectedPlanet = 3;
    private float rotation = 0f;
    private float previousX;
    private boolean dragging = false;
    private boolean orbitAnimation = false;
    private float orbitPhase = 0f;
    private final Runnable orbitTick = new Runnable() { @Override public void run() { if (orbitAnimation && !detailMode) { orbitPhase += 0.018f; invalidate(); postDelayed(this, 16); } } };

    private final int[] colors = {0xFFFFC84A,0xFFB7A18A,0xFFE9B878,0xFF3E91D5,0xFFE45B43,0xFFD9A66B,0xFFE8C778,0xFF72D5D0,0xFF426FE0};
    private final String[] labels = {"Sun","Mercury","Venus","Earth","Mars","Jupiter","Saturn","Uranus","Neptune"};

    public PlanetView(Context context) { this(context, null); }
    public PlanetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onSingleTapUp(MotionEvent e) {
                if (!detailMode) {
                    int hit = hitPlanet(e.getX(), e.getY());
                    if (hit >= 0 && listener != null) listener.onPlanetSelected(hit);
                }
                return true;
            }
            @Override public boolean onDoubleTap(MotionEvent e) {
                if (!detailMode) {
                    int hit = hitPlanet(e.getX(), e.getY());
                    if (hit >= 0 && openedListener != null) openedListener.onPlanetOpened(hit);
                }
                return true;
            }
        });
    }

    public void setOnPlanetSelectedListener(OnPlanetSelectedListener l) { listener = l; }
    public void setOnPlanetOpenedListener(OnPlanetOpenedListener l) { openedListener = l; }
    public void setOnWorldFeatureListener(OnWorldFeatureListener l) { worldFeatureListener = l; }
    public void startOrbitAnimation() { orbitAnimation = true; orbitPhase = 0f; removeCallbacks(orbitTick); post(orbitTick); }
    public void showPlanetDetail(int index) { selectedPlanet = Math.max(0, Math.min(8,index)); detailMode = true; rotation = 0f; invalidate(); }
    public void showSolarSystem() { detailMode = false; invalidate(); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w=getWidth(), h=getHeight();
        if (w<=0 || h<=0) return;
        canvas.drawColor(0xFF101A43);
        drawStars(canvas,w,h);
        if (detailMode) drawLargePlanet(canvas,w,h); else drawSolarSystem(canvas,w,h);
    }

    private void drawStars(Canvas c,float w,float h) {
        paint.setShader(null); paint.setStyle(Paint.Style.FILL);
        for(int i=0;i<85;i++) {
            float sx=((i*83)%997)/997f*w, sy=((i*47)%991)/991f*h;
            paint.setColor(i%6==0?0xFFFFD98B:0xFFB9C9FF);
            c.drawCircle(sx,sy,(i%7==0?1.8f:0.9f)*density,paint);
        }
    }

    private void drawSolarSystem(Canvas c,float w,float h) {
        float scale=Math.min(1f,Math.min((w/density)*0.46f/215f,(h/density)*0.43f/103f));
        float unit=density*Math.max(0.35f,scale), cx=w/2f, cy=h/2f;
        float[] orbit={0,42,65,88,111,139,166,191,215};
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(Math.max(1f,density)); paint.setColor(0x557C91D8);
        for(int i=1;i<9;i++){float d=orbit[i]*unit;c.drawOval(new RectF(cx-d,cy-d*.48f,cx+d,cy+d*.48f),paint);}
        x[0]=cx;y[0]=cy;radius[0]=24f*unit;
        for(int i=1;i<9;i++){
            float a = orbitAnimation ? orbitPhase + i * 0.82f : (float)Math.PI;
            x[i]=cx+(float)Math.cos(a)*orbit[i]*.82f*unit;y[i]=cy+(float)Math.sin(a)*orbit[i]*.39f*unit;
            float size=i==5?13f:(i==6?12f:(i==1?5f:(i==2||i==3||i==4?8f:9f)));
            radius[i]=size*unit*1.4f;
        }
        for(int i=0;i<9;i++){
            drawSphere(c,x[i],y[i],radius[i],colors[i],i,orbitPhase + rotation);
            paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(0xFFEAF0FF);paint.setTextSize(10f*unit);
            c.drawText(labels[i],x[i]-paint.measureText(labels[i])/2f,y[i]+radius[i]+13f*unit,paint);
        }
        paint.setColor(0xFFFFD56A);paint.setTextSize(10f*density);
        String hint=orbitAnimation ? "WATCH THE PLANETS ORBIT THE SUN" : "TAP TO LEARN  •  DOUBLE-TAP TO OPEN";
        c.drawText(hint,cx-paint.measureText(hint)/2f,h-8f*density,paint);
    }

    private void drawLargePlanet(Canvas c,float w,float h) {
        float r=Math.min(w*.34f,h*.34f);
        if(selectedPlanet==0) r=Math.min(w*.36f,h*.36f);
        float cx=w/2f,cy=h*.43f;
        drawSphere(c,cx,cy,r,colors[selectedPlanet],selectedPlanet,rotation);
        paint.setShader(null);paint.setStyle(Paint.Style.FILL);paint.setColor(0xFFFFD56A);
        paint.setTextSize(17f*density);
        String label=labels[selectedPlanet];
        c.drawText(label,cx-paint.measureText(label)/2f,cy+r+34f*density,paint);
        paint.setTextSize(10f*density);paint.setColor(0xFFBFD0FF);
        String instruction="DRAG TO ROTATE  •  TAP A DISCOVERY BELOW";
        c.drawText(instruction,cx-paint.measureText(instruction)/2f,h-48f*density,paint);
        String[] tags={"SURFACE","ATMOSPHERE","SPECIAL FACT"};
        for(int j=0;j<3;j++){float tx=w*(0.19f+j*0.31f);paint.setColor(0xCC314778);c.drawRoundRect(new RectF(tx-42*density,h-38*density,tx+42*density,h-10*density),12*density,12*density,paint);paint.setColor(0xFFFFFFFF);paint.setTextSize(8.5f*density);c.drawText(tags[j],tx-paint.measureText(tags[j])/2,h-21*density,paint);}
    }

    private void drawSphere(Canvas c,float cx,float cy,float r,int color,int index,float angle) {
        if (index == 0) {
            paint.setShader(new RadialGradient(cx,cy,r*2.1f,
                    new int[]{0x99FFF3A0,0x55FF9D24,0x00FF6A00},null,Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,r*1.75f,paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1.5f,r*.07f));paint.setColor(0xFFFFD45A);
            for(int k=0;k<12;k++){
                double a=k*Math.PI/6.0+angle*.05;
                float x1=cx+(float)Math.cos(a)*r*1.04f,y1=cy+(float)Math.sin(a)*r*1.04f;
                float x2=cx+(float)Math.cos(a)*r*1.28f,y2=cy+(float)Math.sin(a)*r*1.28f;
                c.drawLine(x1,y1,x2,y2,paint);
            }
            paint.setStyle(Paint.Style.FILL);
        }
        // Saturn's broad rings sit behind the globe.
        if(index==6){
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(3f,r*.24f));
            paint.setColor(0xFFD9BD83);
            c.drawOval(new RectF(cx-r*1.85f,cy-r*.48f,cx+r*1.85f,cy+r*.48f),paint);
            paint.setStrokeWidth(Math.max(1f,r*.07f));paint.setColor(0xFFFFE8B1);
            c.drawOval(new RectF(cx-r*1.85f,cy-r*.48f,cx+r*1.85f,cy+r*.48f),paint);
            paint.setStyle(Paint.Style.FILL);
        }
        paint.setShader(new RadialGradient(cx-r*.36f,cy-r*.42f,Math.max(r*1.65f,1f),
            new int[]{lighten(color),color,0xFF11152D},null,Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,r,paint);paint.setShader(null);
        int save=c.save();
        Path clip=new Path();clip.addCircle(cx,cy,r,Path.Direction.CW);c.clipPath(clip);

        if(index==1){ // Mercury: craters and rocky terrain
            paint.setColor(0x66806C5D);
            c.drawCircle(cx-r*.38f,cy-r*.28f,r*.19f,paint);
            c.drawCircle(cx+r*.32f,cy+r*.22f,r*.14f,paint);
            c.drawCircle(cx-r*.08f,cy+r*.48f,r*.09f,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,r*.035f));paint.setColor(0x55FFF0D5);
            c.drawCircle(cx-r*.38f,cy-r*.28f,r*.19f,paint);c.drawCircle(cx+r*.32f,cy+r*.22f,r*.14f,paint);
            paint.setStyle(Paint.Style.FILL);
        } else if(index==2){ // Venus: thick golden cloud bands
            paint.setColor(0x66FFF1C5);
            for(int j=-2;j<=2;j++)c.drawOval(new RectF(cx-r*1.1f,cy+j*r*.35f-r*.12f,cx+r*1.1f,cy+j*r*.35f+r*.08f),paint);
        } else if(index==3){ // Earth: oceans, continents and polar ice
            paint.setColor(0xFF39A96B);
            Path land=new Path();land.moveTo(cx-r*.72f,cy-r*.2f);land.cubicTo(cx-r*.52f,cy-r*.62f,cx-r*.1f,cy-r*.45f,cx-r*.13f,cy-r*.08f);land.cubicTo(cx-r*.4f,cy+r*.04f,cx-r*.32f,cy+r*.48f,cx-r*.56f,cy+r*.55f);land.close();c.drawPath(land,paint);
            Path land2=new Path();land2.moveTo(cx+r*.12f,cy-r*.56f);land2.cubicTo(cx+r*.65f,cy-r*.62f,cx+r*.7f,cy-r*.22f,cx+r*.42f,cy-r*.04f);land2.cubicTo(cx+r*.58f,cy+r*.26f,cx+r*.28f,cy+r*.58f,cx+r*.08f,cy+r*.27f);land2.close();c.drawPath(land2,paint);
            paint.setColor(0xFFB9E7F5);c.drawOval(new RectF(cx-r*.72f,cy-r*.9f,cx+r*.72f,cy-r*.7f),paint);c.drawOval(new RectF(cx-r*.7f,cy+r*.72f,cx+r*.7f,cy+r*.9f),paint);
            paint.setColor(0x44FFFFFF);c.drawOval(new RectF(cx-r*.55f,cy-r*.38f,cx+r*.45f,cy-r*.2f),paint);
        } else if(index==4){ // Mars: rusty surface, craters and polar cap
            paint.setColor(0xFF9A3E2C);c.drawCircle(cx+r*.28f,cy-r*.12f,r*.16f,paint);
            c.drawCircle(cx-r*.4f,cy+r*.32f,r*.11f,paint);
            c.drawCircle(cx+r*.58f,cy+r*.42f,r*.07f,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,r*.025f));paint.setColor(0x66FFD0A0);
            c.drawCircle(cx+r*.28f,cy-r*.12f,r*.16f,paint);c.drawCircle(cx-r*.4f,cy+r*.32f,r*.11f,paint);paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xCCF3E4D1);c.drawOval(new RectF(cx-r*.32f,cy-r*.96f,cx+r*.42f,cy-r*.78f),paint);
        } else if(index==5){ // Jupiter: broad turbulent cloud bands and Great Red Spot
            for(int j=-4;j<=4;j++){
                paint.setColor(j%2==0?0x88A85F37:0x99F3D0A0);
                c.drawRect(cx-r*1.1f,cy+j*r*.22f,cx+r*1.1f,cy+j*r*.22f+r*.105f,paint);
            }
            paint.setColor(0xFFB84F3E);c.drawOval(new RectF(cx+r*.22f,cy-r*.04f,cx+r*.72f,cy+r*.25f),paint);
            paint.setColor(0x66FFF0D5);c.drawOval(new RectF(cx-r*.72f,cy-r*.48f,cx+r*.2f,cy-r*.34f),paint);
        } else if(index==6){ // Saturn: subtle cream cloud stripes
            for(int j=-3;j<=3;j++){paint.setColor(j%2==0?0x66A77D48:0x66FFF0C5);c.drawRect(cx-r,cy+j*r*.24f,cx+r,cy+j*r*.24f+r*.08f,paint);}
        } else if(index==7){ // Uranus: pale methane atmosphere
            paint.setColor(0x557FFFFF);for(int j=-2;j<=2;j++)c.drawOval(new RectF(cx-r,cy+j*r*.3f-r*.07f,cx+r,cy+j*r*.3f+r*.04f),paint);
        } else if(index==8){ // Neptune: deep blue bands and a dark storm
            paint.setColor(0x4479B8FF);for(int j=-2;j<=2;j++)c.drawOval(new RectF(cx-r,cy+j*r*.32f-r*.06f,cx+r,cy+j*r*.32f+r*.04f),paint);
            paint.setColor(0x5530196B);c.drawOval(new RectF(cx+r*.18f,cy-r*.05f,cx+r*.58f,cy+r*.2f),paint);
        }

        // Soft atmospheric highlight on the sunward edge.
        paint.setShader(new RadialGradient(cx-r*.42f,cy-r*.48f,r*1.2f,
                new int[]{0x33FFFFFF,0x00FFFFFF},null,Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,r,paint);paint.setShader(null);
        c.restoreToCount(save);
        if(index==6){
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(2f,r*.12f));paint.setColor(0xFFFFE7B1);
            c.drawArc(new RectF(cx-r*1.85f,cy-r*.48f,cx+r*1.85f,cy+r*.48f),195,150,false,paint);
        }
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1f,r*.025f));paint.setColor(0x668FFFFF);c.drawCircle(cx,cy,r,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private int hitPlanet(float px,float py) {
        int selected=-1;float closest=Float.MAX_VALUE;
        for(int i=0;i<9;i++){float dx=px-x[i],dy=py-y[i],d=dx*dx+dy*dy;float hit=radius[i]+18f*density;if(d<=hit*hit&&d<closest){selected=i;closest=d;}}
        return selected;
    }
    private int lighten(int color){return Color.rgb(Math.min(255,Color.red(color)+65),Math.min(255,Color.green(color)+65),Math.min(255,Color.blue(color)+65));}

    @Override public boolean onTouchEvent(MotionEvent event) {
        if(detailMode) {
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN){previousX=event.getX();dragging=true;return true;}
            if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&dragging){float dx=event.getX()-previousX;rotation+=dx/(Math.max(1f,getWidth())*.8f)*6.28f;previousX=event.getX();invalidate();return true;}
            if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL){if(dragging && Math.abs(event.getX()-previousX)<8*density && worldFeatureListener!=null){int feature=Math.min(2,Math.max(0,(int)(event.getX()/Math.max(1,getWidth())*3)));worldFeatureListener.onWorldFeature(selectedPlanet,feature);}dragging=false;return true;}
            return true;
        }
        gestures.onTouchEvent(event);
        return true;
    }
}

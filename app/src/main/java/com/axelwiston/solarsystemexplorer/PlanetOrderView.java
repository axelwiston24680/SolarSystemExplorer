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
import java.util.Arrays;

/** A circular drag-and-place puzzle: each planet belongs on its correct orbit. */
public class PlanetOrderView extends View {
    public interface OnOrderCorrectListener { void onOrderCorrect(boolean correct); }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String[] names = {"Mercury","Venus","Earth","Mars","Jupiter","Saturn","Uranus","Neptune"};
    private final int[] colors = {0xFFB9A18A,0xFFE9B878,0xFF348FE0,0xFFE45B43,0xFFD69A5B,0xFFE7C779,0xFF72D5D0,0xFF416DDF};
    // The value is the orbit number (0 = closest to the Sun).
    private final int[] orbitOf = {3,0,6,2,7,1,5,4};
    private final float density;
    private OnOrderCorrectListener listener;
    private int draggingPlanet = -1;
    private float dragX, dragY;
    private boolean orbiting = false;
    private float phase = 0f;
    private final Runnable orbitTick = new Runnable() {
        @Override public void run() {
            if (orbiting) { phase += .018f; invalidate(); postDelayed(this, 16); }
        }
    };

    public PlanetOrderView(Context c) { this(c, null); }
    public PlanetOrderView(Context c, AttributeSet a) {
        super(c, a);
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
    }

    public void setOnOrderCorrectListener(OnOrderCorrectListener l) {
        listener = l;
        notifyState();
    }

    public void startOrbitAnimation() {
        if (!isCorrect()) return;
        orbiting = true;
        phase = 0f;
        removeCallbacks(orbitTick);
        post(orbitTick);
        invalidate();
    }

    private boolean isCorrect() {
        for (int i = 0; i < 8; i++) if (orbitOf[i] != i) return false;
        return true;
    }

    private void notifyState() {
        if (listener != null) listener.onOrderCorrect(isCorrect());
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        c.drawColor(0xFF101A43);

        // Soft star field.
        p.setShader(null); p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 42; i++) {
            float sx = ((i * 83) % 997) / 997f * w;
            float sy = ((i * 47) % 991) / 991f * h;
            p.setColor(i % 5 == 0 ? 0xFFFFD98B : 0xFF91A9E8);
            c.drawCircle(sx, sy, (i % 7 == 0 ? 1.5f : .8f) * density, p);
        }

        float cx = w / 2f;
        float cy = h / 2f + 7f * density;
        float maxR = Math.min(w * .43f, h * .405f);
        float step = maxR / 8f;

        // Glowing Sun at the center.
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(cx, cy, 35 * density,
                new int[]{0xFFFFF6A0,0xFFFFB52E,0x00FF7A00}, null, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, 35 * density, p);
        p.setShader(new RadialGradient(cx - 5*density, cy - 7*density, 25*density,
                new int[]{0xFFFFFFC0,0xFFFFB52B,0xFFE96B12}, null, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, 17*density, p);
        p.setShader(null);
        p.setColor(0xFFFFE9A3); p.setTextSize(10*density);
        String sun = "SUN";
        c.drawText(sun, cx-p.measureText(sun)/2f, cy+39*density, p);

        // Eight circular orbital tracks.
        p.setStyle(Paint.Style.STROKE);
        for (int slot = 0; slot < 8; slot++) {
            float r = orbitRadius(slot);
            p.setStrokeWidth(slot == 0 ? 1.5f*density : density);
            p.setColor(0x557FA7E8);
            c.drawCircle(cx, cy, r, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x887FA7E8);
            c.drawCircle(cx, cy-r, 2*density, p);
            p.setStyle(Paint.Style.STROKE);
        }
        p.setStyle(Paint.Style.FILL);

        // Each planet stays on its assigned orbit while the child moves it.
        for (int planet = 0; planet < 8; planet++) {
            if (planet == draggingPlanet) continue;
            float r = orbitRadius(orbitOf[planet]);
            float angle = -1.15f + planet * .77f + (orbiting ? phase * (planet % 2 == 0 ? 1 : -1) * (1.35f/(planet+1)) : 0f);
            float px = cx + (float)Math.cos(angle) * r;
            float py = cy + (float)Math.sin(angle) * r;
            drawPlanet(c, px, py, planet, 15.5f*density);
            p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(0xFFEAF0FF);p.setTextSize(8.5f*density);
            c.drawText(names[planet],px+13*density,py+3*density,p);
        }
        if (draggingPlanet >= 0) {
            drawPlanet(c, dragX, dragY, draggingPlanet, 19*density);
            p.setColor(0x88FFFFFF); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.5f*density);
            c.drawCircle(dragX, dragY, 22*density, p); p.setStyle(Paint.Style.FILL);
        }

        p.setShader(null); p.setColor(0xFFFFD98B); p.setTextSize(11*density);
        String hint = orbiting ? "THE PLANETS ARE ORBITING THE SUN!" :
                (isCorrect() ? "PERFECT ORDER! TAP ORBIT THE PLANETS" : "DRAG EACH PLANET TO ITS CORRECT ORBIT");
        c.drawText(hint, cx-p.measureText(hint)/2f, h-8*density, p);
    }

    private void drawPlanet(Canvas c, float cx, float cy, int index, float r) {
        if (index == 5) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4*density); p.setColor(0xFFE8CD8E);
            c.drawOval(new RectF(cx-r*1.65f,cy-r*.32f,cx+r*1.65f,cy+r*.32f),p);
        }
        p.setStyle(Paint.Style.FILL);
        int base = colors[index];
        p.setShader(new RadialGradient(cx-r*.36f,cy-r*.42f,r*1.55f,
                new int[]{lighten(base),base,0xFF14172D},null,Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,r,p); p.setShader(null);
        int save = c.save();
        Path clip = new Path(); clip.addCircle(cx,cy,r,Path.Direction.CW); c.clipPath(clip);
        if (index == 2) {
            p.setColor(0xFF39B978);
            c.drawOval(new RectF(cx-r*.68f,cy-r*.42f,cx-r*.05f,cy+r*.02f),p);
            c.drawOval(new RectF(cx+r*.08f,cy-r*.62f,cx+r*.62f,cy-r*.12f),p);
            c.drawOval(new RectF(cx+r*.12f,cy+r*.15f,cx+r*.55f,cy+r*.52f),p);
            p.setColor(0x557BE9FF); c.drawCircle(cx-r*.25f,cy-r*.45f,r*.22f,p);
        } else if (index == 4) {
            p.setColor(0xFF9B3E2D); c.drawCircle(cx+r*.25f,cy-r*.12f,r*.18f,p);
            p.setColor(0xFFB95B3D); c.drawCircle(cx-r*.38f,cy+r*.35f,r*.10f,p);
        } else if (index == 3 || index == 6 || index == 7) {
            for (int j=-3;j<=3;j++) {
                p.setColor(j%2==0?0x668E5A3D:0x66FFF0C7);
                c.drawRect(cx-r,cy+j*r*.25f,cx+r,cy+j*r*.25f+r*.09f,p);
            }
        } else if (index == 0) {
            p.setColor(0x66806E62);
            c.drawCircle(cx-r*.35f,cy-r*.25f,r*.16f,p);
            c.drawCircle(cx+r*.32f,cy+r*.25f,r*.12f,p);
        } else if (index == 1) {
            p.setColor(0x44FFF4D7);
            c.drawOval(new RectF(cx-r*.8f,cy-r*.4f,cx+r*.8f,cy-r*.12f),p);
            c.drawOval(new RectF(cx-r*.7f,cy+r*.2f,cx+r*.7f,cy+r*.4f),p);
        }
        c.restoreToCount(save);
        if (index == 5) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3*density); p.setColor(0xFFFFE4A1);
            c.drawArc(new RectF(cx-r*1.65f,cy-r*.32f,cx+r*1.65f,cy+r*.32f),190,160,false,p);
        }
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1*density); p.setColor(0x66FFFFFF);
        c.drawCircle(cx,cy,r,p); p.setStyle(Paint.Style.FILL);
    }

    private int lighten(int color) {
        return Color.rgb(Math.min(255,Color.red(color)+65),Math.min(255,Color.green(color)+65),Math.min(255,Color.blue(color)+65));
    }

    private float maxRadius() { return Math.min(getWidth()*.43f,getHeight()*.405f); }
    private float orbitRadius(int slot) { float max=maxRadius(); return 34f*density + slot*Math.max(1f,(max-34f*density)/7f); }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float cx=getWidth()/2f, cy=getHeight()/2f+7f*density;
        if (orbiting) return true;
        if (e.getActionMasked()==MotionEvent.ACTION_DOWN) {
            float best=Float.MAX_VALUE;
            for(int planet=0;planet<8;planet++) {
                float r=orbitRadius(orbitOf[planet]);
                float angle=-1.15f+planet*.77f;
                float px=cx+(float)Math.cos(angle)*r, py=cy+(float)Math.sin(angle)*r;
                float d=(e.getX()-px)*(e.getX()-px)+(e.getY()-py)*(e.getY()-py);
                if(d<best && d<(27*density)*(27*density)){best=d;draggingPlanet=planet;}
            }
            if(draggingPlanet>=0){dragX=e.getX();dragY=e.getY();invalidate();return true;}
            return true;
        }
        if(e.getActionMasked()==MotionEvent.ACTION_MOVE && draggingPlanet>=0) {
            dragX=e.getX();dragY=e.getY();invalidate();return true;
        }
        if(e.getActionMasked()==MotionEvent.ACTION_UP && draggingPlanet>=0) {
            float distance=(float)Math.hypot(e.getX()-cx,e.getY()-cy);
            int target=0; float nearest=Float.MAX_VALUE;
            for(int slot=0;slot<8;slot++){float delta=Math.abs(distance-orbitRadius(slot));if(delta<nearest){nearest=delta;target=slot;}}
            int occupant=-1;
            for(int i=0;i<8;i++) if(orbitOf[i]==target){occupant=i;break;}
            int old=orbitOf[draggingPlanet];
            if(occupant>=0 && occupant!=draggingPlanet) orbitOf[occupant]=old;
            orbitOf[draggingPlanet]=target;
            draggingPlanet=-1;invalidate();notifyState();return true;
        }
        if(e.getActionMasked()==MotionEvent.ACTION_CANCEL){draggingPlanet=-1;invalidate();return true;}
        return true;
    }

    @Override protected void onDetachedFromWindow() {
        orbiting=false;removeCallbacks(orbitTick);super.onDetachedFromWindow();
    }
}

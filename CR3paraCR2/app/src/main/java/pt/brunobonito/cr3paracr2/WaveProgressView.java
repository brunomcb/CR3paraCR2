package pt.brunobonito.cr3paracr2;
import android.content.*; import android.graphics.*; import android.util.*; import android.view.*;
public class WaveProgressView extends View {
 private final Paint track=new Paint(1),active=new Paint(1),stop=new Paint(1); private float progress,phase;
 public WaveProgressView(Context c){this(c,null);} public WaveProgressView(Context c,AttributeSet a){super(c,a);float t=dp(4);track.setStrokeWidth(t);active.setStrokeWidth(t);track.setStrokeCap(Paint.Cap.ROUND);active.setStrokeCap(Paint.Cap.ROUND);stop.setStyle(Paint.Style.FILL);}
 public void setColors(int p,int tr){active.setColor(p);stop.setColor(p);track.setColor(tr);}
 public void setProgress(float p){progress=Math.max(0,Math.min(1,p));phase+=.2f;invalidate();}
 protected void onDraw(Canvas c){float left=dp(7),right=getWidth()-dp(7),y=getHeight()/2f;c.drawLine(left,y,right,y,track);float end=left+(right-left)*progress;if(end>left){Path w=new Path();w.moveTo(left,y);for(float x=left;x<=end;x+=dp(.75f)){float d=x-left,r=end-x,e=Math.min(1f,Math.min(d/dp(12),Math.max(0,r/dp(12))));w.lineTo(x,(float)(y+Math.sin(d/dp(24)*Math.PI*2+phase)*dp(3)*e));}c.drawPath(w,active);}c.drawCircle(right,y,dp(2),stop);}
 private float dp(float v){return v*getResources().getDisplayMetrics().density;}
}

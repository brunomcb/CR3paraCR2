package pt.brunobonito.cr3paracr2;
import android.content.Context; import android.graphics.*; import android.util.AttributeSet; import android.view.View;
public class WaveProgressView extends View {
 private final Paint track=new Paint(1), fill=new Paint(1); private float progress=0f, phase=0f;
 public WaveProgressView(Context c){super(c);init();} public WaveProgressView(Context c,AttributeSet a){super(c,a);init();}
 private void init(){track.setStrokeWidth(dp(8));track.setStrokeCap(Paint.Cap.ROUND);fill.setStrokeWidth(dp(8));fill.setStrokeCap(Paint.Cap.ROUND);}
 public void setColors(int primary,int trackColor){fill.setColor(primary);track.setColor(trackColor);invalidate();}
 public void setProgress(float p){progress=Math.max(0,Math.min(1,p));phase+=.45f;invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float y=getHeight()/2f, left=dp(4), right=getWidth()-dp(4);c.drawLine(left,y,right,y,track);float end=left+(right-left)*progress;if(end<=left)return;Path p=new Path();p.moveTo(left,y);float wavelength=dp(24),amp=dp(3);for(float x=left;x<=end;x+=2){float yy=(float)(y+Math.sin((x-left)/wavelength*2*Math.PI+phase)*amp);p.lineTo(x,yy);}c.save();c.clipRect(left,0,end,getHeight());c.drawPath(p,fill);c.restore();}
 private float dp(float x){return x*getResources().getDisplayMetrics().density;}
}

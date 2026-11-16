package pt.brunobonito.cr3paracr2;
import android.content.Context; import android.graphics.*; import android.util.AttributeSet; import android.view.View;
public class WaveProgressView extends View {
 private final Paint track=new Paint(Paint.ANTI_ALIAS_FLAG), active=new Paint(Paint.ANTI_ALIAS_FLAG), stop=new Paint(Paint.ANTI_ALIAS_FLAG);
 private float progress=0f, phase=0f; private final float thickness, amplitude, wavelength;
 public WaveProgressView(Context c){this(c,null);} public WaveProgressView(Context c,AttributeSet a){super(c,a);thickness=dp(8);amplitude=dp(3);wavelength=dp(28);init();}
 private void init(){setLayerType(View.LAYER_TYPE_SOFTWARE,null);track.setStrokeWidth(thickness);track.setStrokeCap(Paint.Cap.ROUND);active.setStrokeWidth(thickness);active.setStrokeCap(Paint.Cap.ROUND);stop.setStyle(Paint.Style.FILL);setMinimumHeight((int)dp(32));}
 public void setColors(int primary,int trackColor){active.setColor(primary);stop.setColor(primary);track.setColor(trackColor);invalidate();}
 public void setProgress(float p){progress=Math.max(0f,Math.min(1f,p));phase+=0.35f;invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float pad=Math.max(thickness/2f+amplitude,dp(8));float left=pad,right=getWidth()-pad,y=getHeight()/2f;if(right<=left)return;c.drawLine(left,y,right,y,track);float end=left+(right-left)*progress;if(end>left){Path path=new Path();path.moveTo(left,y);for(float x=left;x<=end;x+=dp(1)){float envelope=Math.min(1f,Math.min((x-left)/wavelength,(end-x)/wavelength+0.25f));float yy=(float)(y+Math.sin((x-left)/wavelength*2*Math.PI+phase)*amplitude*Math.max(0,envelope));path.lineTo(x,yy);}c.save();c.clipRect(left-thickness,0,end+thickness,getHeight());c.drawPath(path,active);c.restore();}float stopRadius=thickness*0.28f;c.drawCircle(right,y,stopRadius,stop);}
 @Override protected void onMeasure(int ws,int hs){int desired=(int)dp(36);int h=resolveSize(desired,hs);setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(),ws),h);}
 private float dp(float x){return x*getResources().getDisplayMetrics().density;}
}

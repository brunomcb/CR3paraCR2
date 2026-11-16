package pt.brunobonito.cr3paracr2;
import android.app.Application; import com.google.android.material.color.DynamicColors;
public class RawRenameApp extends Application { @Override public void onCreate(){super.onCreate();DynamicColors.applyToActivitiesIfAvailable(this);} }

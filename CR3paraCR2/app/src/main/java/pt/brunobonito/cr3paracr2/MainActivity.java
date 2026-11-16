package pt.brunobonito.cr3paracr2;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.InputFilter;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int PICK_FOLDER = 7;
    private TextView status;
    private Button choose;
    private CheckBox recursive;
    private RadioGroup modes;
    private LinearLayout manualBox;
    private EditText sourceInput, targetInput;
    private Dialog progressDialog; private WaveProgressView wave; private TextView progressPercent, progressTitle;
    private String sourceExt = "cr3", targetExt = "cr2";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(surface());
        LinearLayout page = column();
        page.setPadding(dp(22), statusBarHeight() + dp(28), dp(22), dp(30));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        TextView brand = text("RAW RENAME",14,true); brand.setTextColor(purple()); page.addView(brand,lp(0,22));
        TextView title = text("Renomeia extensões em segundos.",34,true); page.addView(title,lp(0,12));
        TextView desc = text("Escolhe uma regra, seleciona uma pasta e altera as extensões em lote, sem enviar ficheiros para a internet.",16,false); desc.setTextColor(muted()); page.addView(desc,lp(0,24));

        LinearLayout ruleContent = column();
        MaterialCardView ruleShell = cardShell();
        ruleShell.addView(ruleContent);
        LinearLayout ruleCard = ruleContent;
        TextView ruleStep=text("01  DEFINIR REGRA",12,true); ruleStep.setTextColor(purple()); ruleCard.addView(ruleStep,lp(0,12));
        ruleCard.addView(text("Que alteração pretendes fazer?",17,true),lp(0,10));
        modes = new RadioGroup(this); modes.setOrientation(RadioGroup.VERTICAL);
        RadioButton cr3cr2 = radio("CR3  →  CR2", 101, true);
        RadioButton cr2cr3 = radio("CR2  →  CR3", 102, false);
        RadioButton custom = radio("Definir extensões manualmente", 103, false);
        modes.addView(cr3cr2); modes.addView(cr2cr3); modes.addView(custom);
        ruleCard.addView(modes,lp(0,10));

        manualBox = column(); manualBox.setVisibility(View.GONE);
        sourceInput = extensionInput("Extensão a procurar, por exemplo: nef");
        targetInput = extensionInput("Nova extensão, por exemplo: dng");
        manualBox.addView(labelSmall("EXTENSÃO DE ORIGEM")); manualBox.addView(sourceInput,lp(6,14));
        manualBox.addView(labelSmall("EXTENSÃO DE DESTINO")); manualBox.addView(targetInput,lp(6,4));
        ruleCard.addView(manualBox);
        modes.setOnCheckedChangeListener((g,id) -> {
            manualBox.setVisibility(id == 103 ? View.VISIBLE : View.GONE);
            if (id == 101) { sourceExt="cr3"; targetExt="cr2"; }
            if (id == 102) { sourceExt="cr2"; targetExt="cr3"; }
        });
        page.addView(ruleShell,lp(0,18));

        LinearLayout folderContent = column();
        MaterialCardView folderShell = cardShell();
        folderShell.addView(folderContent);
        LinearLayout folderCard = folderContent;
        TextView folderStep=text("02  SELECIONAR PASTA",12,true); folderStep.setTextColor(purple()); folderCard.addView(folderStep,lp(0,12));
        folderCard.addView(text("Escolhe a pasta que contém os ficheiros",17,true),lp(0,8));
        TextView privacy=text("A app apenas acede à pasta que selecionares.",14,false); privacy.setTextColor(muted()); folderCard.addView(privacy,lp(0,14));
        recursive=new CheckBox(this); recursive.setText("Incluir também as subpastas"); recursive.setTextSize(15); recursive.setTextColor(ink()); recursive.setButtonTintList(android.content.res.ColorStateList.valueOf(purple())); folderCard.addView(recursive,lp(0,16));
        choose=new MaterialButton(this); choose.setAllCaps(false); choose.setText("Escolher pasta"); choose.setTextColor(onPrimary()); choose.setTextSize(16); choose.setTypeface(null,1); ((MaterialButton)choose).setCornerRadius(dp(20)); ((MaterialButton)choose).setBackgroundTintList(ColorStateList.valueOf(primary())); choose.setOnClickListener(v->preparePick()); folderCard.addView(choose,new LinearLayout.LayoutParams(-1,dp(58)));
        page.addView(folderShell,lp(0,18));

        TextView warning=text("IMPORTANTE\n\nA app altera apenas o nome da extensão. O formato interno do ficheiro não é convertido.",14,false); warning.setTextColor(Color.rgb(55,48,110)); warning.setPadding(dp(18),dp(16),dp(18),dp(16)); warning.setBackground(roundBg(Color.rgb(239,238,255),18)); page.addView(warning,lp(0,18));
        status=text("Pronto para começar",14,true); status.setTextColor(muted()); status.setTextIsSelectable(true); page.addView(status);
        setContentView(scroll);
    }

    private void preparePick() {
        if (modes.getCheckedRadioButtonId() == 103) {
            sourceExt = cleanExtension(sourceInput.getText().toString());
            targetExt = cleanExtension(targetInput.getText().toString());
            if (!validExtension(sourceExt) || !validExtension(targetExt)) {
                showError("Indica duas extensões válidas, sem espaços. Exemplos: cr3, cr2, nef ou dng."); return;
            }
            if (sourceExt.equalsIgnoreCase(targetExt)) {
                showError("A extensão de origem e a extensão de destino têm de ser diferentes."); return;
            }
        }
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION|Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(i,PICK_FOLDER);
    }

    @Override protected void onActivityResult(int req,int result,Intent data) {
        super.onActivityResult(req,result,data);
        if(req!=PICK_FOLDER||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri tree=data.getData();
        try { getContentResolver().takePersistableUriPermission(tree,data.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION)); } catch(Exception ignored) {}
        String message="Renomear todos os ficheiros ."+sourceExt+" para ."+targetExt+(recursive.isChecked()?" nesta pasta e nas subpastas?":" apenas nesta pasta?")+"\n\nFicheiros com o mesmo nome no destino serão ignorados.";
        new MaterialAlertDialogBuilder(this).setTitle("Confirmar alteração").setMessage(message).setNegativeButton("Cancelar",null).setPositiveButton("Renomear",(d,w)->runRename(tree,recursive.isChecked())).show();
    }

    private void runRename(Uri tree,boolean recurse) {
        choose.setEnabled(false); final String from=sourceExt, to=targetExt;
        showProgressDialog();
        new Thread(() -> {
            int total=0; try { Uri root=DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree)); total=countMatching(root,recurse,from); } catch(Exception ignored) {}
            final int totalFiles=total; runOnUiThread(() -> updateProgress(0,totalFiles));
            int[] c=new int[3]; List<String> details=new ArrayList<>(); int[] done={0};
            try { Uri root=DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree)); process(root,recurse,from,to,c,details,done,totalFiles); }
            catch(Exception e){c[2]++;details.add("Erro geral: "+safe(e.getMessage()));}
            runOnUiThread(() -> showProgressSuccess(c,details));
        }).start();
    }

    private int countMatching(Uri dir,boolean recurse,String from) {
        int total=0; Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(dir,DocumentsContract.getDocumentId(dir));
        String[] cols={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE};
        try(android.database.Cursor cur=getContentResolver().query(children,cols,null,null,null)){if(cur==null)return 0;while(cur.moveToNext()){String id=cur.getString(0),name=cur.getString(1),mime=cur.getString(2);Uri item=DocumentsContract.buildDocumentUriUsingTree(dir,id);if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)){if(recurse)total+=countMatching(item,true,from);}else if(name!=null&&hasExtension(name,from))total++;}}catch(Exception ignored){} return total;
    }

    private void showProgressDialog(){
        progressDialog=new Dialog(this); progressDialog.setCancelable(false);
        LinearLayout box=column(); box.setPadding(dp(28),dp(26),dp(28),dp(22)); box.setBackground(roundBg(surfaceContainer(),28));
        progressTitle=text("A preparar ficheiros…",22,true); box.addView(progressTitle,lp(0,8));
        TextView sub=text("Mantém a aplicação aberta durante a alteração.",14,false); sub.setTextColor(onSurfaceVariant()); box.addView(sub,lp(0,22));
        wave=new WaveProgressView(this); wave.setColors(primary(),secondaryContainer()); LinearLayout.LayoutParams waveParams=new LinearLayout.LayoutParams(-1,dp(32)); waveParams.topMargin=dp(8); waveParams.bottomMargin=dp(8); box.addView(wave,waveParams);
        progressPercent=text("0%",32,true); progressPercent.setGravity(Gravity.CENTER); progressPercent.setTextColor(primary()); box.addView(progressPercent,lp(12,4));
        progressDialog.setContentView(box); if(progressDialog.getWindow()!=null){progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);progressDialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88f),-2);} progressDialog.show(); if(progressDialog.getWindow()!=null)progressDialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88f),-2);
    }
    private void updateProgress(int done,int total){int pct=total==0?100:Math.round(done*100f/total);wave.setProgress(pct/100f);progressPercent.setText(pct+"%");progressTitle.setText(total==0?"Sem ficheiros correspondentes":"A renomear "+done+" de "+total);}
    private void showProgressSuccess(int[] c,List<String> details){wave.setProgress(1f);progressPercent.setText("100%");progressPercent.setTextColor(primary());progressTitle.setText(c[2]==0?"Processo concluído":"Concluído com avisos");
        LinearLayout box=(LinearLayout)progressTitle.getParent(); TextView result=text(c[0]+" renomeados  •  "+c[1]+" ignorados  •  "+c[2]+" erros",14,false);result.setTextColor(onSurfaceVariant());box.addView(result,lp(4,18));MaterialButton ok=new MaterialButton(this);ok.setText("OK");ok.setCornerRadius(dp(18));ok.setOnClickListener(v->{progressDialog.dismiss();choose.setEnabled(true);choose.setText("Escolher outra pasta");status.setText(c[2]==0?"Última operação concluída com sucesso":"Última operação concluída com avisos");});box.addView(ok,new LinearLayout.LayoutParams(-1,dp(52)));}

    private void process(Uri dir,boolean recurse,String from,String to,int[] c,List<String> details,int[] done,int total) {
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(dir,DocumentsContract.getDocumentId(dir));
        String[] cols={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE};
        try(android.database.Cursor cur=getContentResolver().query(children,cols,null,null,null)) {
            if(cur==null){c[2]++;return;}
            while(cur.moveToNext()) {
                String id=cur.getString(0),name=cur.getString(1),mime=cur.getString(2);
                Uri item=DocumentsContract.buildDocumentUriUsingTree(dir,id);
                if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) { if(recurse)process(item,true,from,to,c,details,done,total); }
                else if(name!=null && hasExtension(name,from)) {
                    String target=name.substring(0,name.length()-from.length())+preserveCase(from,to,name);
                    if(siblingExists(dir,target)){c[1]++;details.add("Já existe: "+target);continue;}
                    try { if(DocumentsContract.renameDocument(getContentResolver(),item,target)!=null)c[0]++;else{c[2]++;details.add("Falhou: "+name);} }
                    catch(Exception e){c[2]++;details.add(name+": "+safe(e.getMessage()));}
                    done[0]++; final int current=done[0]; runOnUiThread(() -> updateProgress(current,total));
                }
            }
        } catch(Exception e){c[2]++;details.add("Pasta: "+safe(e.getMessage()));}
    }

    private boolean hasExtension(String name,String ext){return name.toLowerCase(Locale.ROOT).endsWith("."+ext.toLowerCase(Locale.ROOT));}
    private String preserveCase(String from,String to,String name){String old=name.substring(name.length()-from.length());return old.equals(old.toUpperCase(Locale.ROOT))?to.toUpperCase(Locale.ROOT):to.toLowerCase(Locale.ROOT);}
    private String cleanExtension(String s){s=s.trim();while(s.startsWith("."))s=s.substring(1);return s;}
    private boolean validExtension(String s){return s.matches("[A-Za-z0-9]{1,10}");}
    private void showError(String s){new MaterialAlertDialogBuilder(this).setTitle("Verifica as extensões").setMessage(s).setPositiveButton("OK",null).show();}
    private boolean siblingExists(Uri dir,String wanted){Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(dir,DocumentsContract.getDocumentId(dir));try(android.database.Cursor c=getContentResolver().query(children,new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},null,null,null)){if(c!=null)while(c.moveToNext())if(wanted.equalsIgnoreCase(c.getString(0)))return true;}catch(Exception ignored){}return false;}

    private LinearLayout column(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;}
    private MaterialCardView cardShell(){MaterialCardView c=new MaterialCardView(this);c.setRadius(dp(24));c.setCardElevation(0);c.setStrokeWidth(dp(1));c.setStrokeColor(outlineVariant());c.setCardBackgroundColor(surfaceContainer());c.setContentPadding(dp(20),dp(20),dp(20),dp(20));return c;}
    private RadioButton radio(String s,int id,boolean checked){RadioButton r=new RadioButton(this);r.setText(s);r.setId(id);r.setChecked(checked);r.setTextSize(15);r.setTextColor(ink());r.setButtonTintList(android.content.res.ColorStateList.valueOf(purple()));r.setPadding(0,dp(5),0,dp(5));return r;}
    private EditText extensionInput(String hint){EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setTextSize(15);e.setTextColor(ink());e.setHintTextColor(muted());e.setPadding(dp(14),0,dp(14),0);e.setBackground(roundStroke());e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});e.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(52)));return e;}
    private TextView labelSmall(String s){TextView t=text(s,11,true);t.setTextColor(muted());return t;}
    private TextView text(String s,int sp,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(ink());t.setLineSpacing(0,1.12f);if(bold)t.setTypeface(null,1);return t;}
    private LinearLayout.LayoutParams lp(int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);p.bottomMargin=dp(bottom);return p;}
    private android.graphics.drawable.GradientDrawable roundBg(int color,int radius){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private android.graphics.drawable.GradientDrawable roundStroke(){android.graphics.drawable.GradientDrawable g=roundBg(Color.WHITE,14);g.setStroke(dp(1),Color.rgb(210,213,225));return g;}
    private int statusBarHeight(){int id=getResources().getIdentifier("status_bar_height","dimen","android");return id>0?getResources().getDimensionPixelSize(id):dp(24);}
    private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}
    private int onPrimary(){return attr(com.google.android.material.R.attr.colorOnPrimary,Color.WHITE);}
    private int attr(int a,int fallback){TypedValue v=new TypedValue();return getTheme().resolveAttribute(a,v,true)?v.data:fallback;} private int primary(){return attr(com.google.android.material.R.attr.colorPrimary,Color.rgb(91,79,247));} private int surface(){return attr(com.google.android.material.R.attr.colorSurface,Color.rgb(255,251,254));} private int surfaceContainer(){return attr(com.google.android.material.R.attr.colorSurfaceContainerHigh,Color.WHITE);} private int secondaryContainer(){return attr(com.google.android.material.R.attr.colorSecondaryContainer,Color.rgb(232,222,248));} private int surfaceVariant(){return attr(com.google.android.material.R.attr.colorSurfaceVariant,Color.LTGRAY);} private int outlineVariant(){return attr(com.google.android.material.R.attr.colorOutlineVariant,Color.LTGRAY);} private int onSurfaceVariant(){return attr(com.google.android.material.R.attr.colorOnSurfaceVariant,Color.DKGRAY);} private int purple(){return primary();} private int ink(){return attr(com.google.android.material.R.attr.colorOnSurface,Color.rgb(23,26,43));} private int muted(){return onSurfaceVariant();}
    private String safe(String s){return s==null?"erro desconhecido":s;}
}

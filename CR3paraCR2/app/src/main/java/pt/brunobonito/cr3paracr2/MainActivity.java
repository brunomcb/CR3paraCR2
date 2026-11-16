package pt.brunobonito.cr3paracr2;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int PICK_FOLDER = 7;
    private TextView status;
    private Button choose;
    private CheckBox recursive;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        int pad = dp(24);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, dp(42), pad, pad);
        box.setBackgroundColor(Color.rgb(255,251,254));

        TextView title = text("CR3 para CR2", 28, true);
        TextView desc = text("Seleciona uma pasta e a app altera apenas os nomes dos ficheiros .CR3/.cr3 para .CR2/.cr2. O conteúdo RAW não é convertido.", 16, false);
        recursive = new CheckBox(this);
        recursive.setText("Incluir subpastas");
        recursive.setTextSize(16);
        choose = new Button(this);
        choose.setText("Selecionar pasta");
        choose.setOnClickListener(v -> pickFolder());
        status = text("Nenhuma pasta selecionada.", 15, false);
        status.setTextIsSelectable(true);

        box.addView(title, lp(0, 12));
        box.addView(desc, lp(0, 20));
        box.addView(recursive, lp(0, 16));
        box.addView(choose, lp(0, 20));
        box.addView(status, lp(0, 0));
        setContentView(box);
    }

    private TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(Color.rgb(40,35,42));
        if (bold) t.setTypeface(null, 1); return t;
    }
    private LinearLayout.LayoutParams lp(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin=dp(top); p.bottomMargin=dp(bottom); return p;
    }
    private int dp(int v) { return (int)(v*getResources().getDisplayMetrics().density+.5f); }

    private void pickFolder() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(i, PICK_FOLDER);
    }

    @Override protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req, result, data);
        if (req != PICK_FOLDER || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri tree = data.getData();
        try { getContentResolver().takePersistableUriPermission(tree,
                data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)); }
        catch (Exception ignored) {}

        new AlertDialog.Builder(this)
            .setTitle("Confirmar alteração")
            .setMessage("A extensão será alterada de CR3 para CR2" + (recursive.isChecked() ? " nesta pasta e nas subpastas." : " apenas nesta pasta.") +
                    "\n\nIsto não converte o formato RAW e pode não tornar os ficheiros compatíveis com software que não suporte CR3.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Alterar", (d,w) -> runRename(tree, recursive.isChecked()))
            .show();
    }

    private void runRename(Uri tree, boolean recurse) {
        choose.setEnabled(false); status.setText("A processar...");
        new Thread(() -> {
            int[] count = new int[3]; // renamed, skipped, errors
            List<String> details = new ArrayList<>();
            try {
                Uri root = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree));
                process(root, recurse, count, details);
            } catch (Exception e) { count[2]++; details.add("Erro geral: " + safe(e.getMessage())); }
            StringBuilder r = new StringBuilder("Concluído\n\nRenomeados: ").append(count[0])
                    .append("\nIgnorados: ").append(count[1]).append("\nErros: ").append(count[2]);
            if (!details.isEmpty()) { r.append("\n\nDetalhes:\n"); for (String s: details) r.append("• ").append(s).append('\n'); }
            runOnUiThread(() -> { status.setText(r.toString()); choose.setEnabled(true); });
        }).start();
    }

    private void process(Uri dir, boolean recurse, int[] c, List<String> details) {
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(dir, DocumentsContract.getDocumentId(dir));
        String[] cols = { DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE };
        try (android.database.Cursor cur = getContentResolver().query(children, cols, null, null, null)) {
            if (cur == null) { c[2]++; return; }
            while (cur.moveToNext()) {
                String id=cur.getString(0), name=cur.getString(1), mime=cur.getString(2);
                Uri item=DocumentsContract.buildDocumentUriUsingTree(dir,id);
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                    if (recurse) process(item, true, c, details);
                } else if (name != null && name.toLowerCase(Locale.ROOT).endsWith(".cr3")) {
                    String base=name.substring(0,name.length()-4);
                    String ext=name.endsWith(".CR3") ? ".CR2" : ".cr2";
                    String target=base+ext;
                    if (siblingExists(dir,target)) { c[1]++; details.add("Já existe: "+target); continue; }
                    try {
                        Uri renamed=DocumentsContract.renameDocument(getContentResolver(),item,target);
                        if (renamed != null) c[0]++; else { c[2]++; details.add("Falhou: "+name); }
                    } catch (Exception e) { c[2]++; details.add(name+": "+safe(e.getMessage())); }
                }
            }
        } catch (Exception e) { c[2]++; details.add("Pasta: "+safe(e.getMessage())); }
    }

    private boolean siblingExists(Uri dir, String wanted) {
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(dir,DocumentsContract.getDocumentId(dir));
        try (android.database.Cursor c=getContentResolver().query(children,new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},null,null,null)) {
            if (c != null) while(c.moveToNext()) if(wanted.equalsIgnoreCase(c.getString(0))) return true;
        } catch(Exception ignored) {}
        return false;
    }
    private String safe(String s) { return s == null ? "erro desconhecido" : s; }
}

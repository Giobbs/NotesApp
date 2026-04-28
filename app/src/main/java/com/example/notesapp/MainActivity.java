package com.example.notesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.SortType;
import com.example.notesapp.ui.main.NoteAdapter;
import com.example.notesapp.ui.main.NotesViewModel;
import com.example.notesapp.ui.widget.WidgetUpdater;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private NoteAdapter adapter;

    private SearchView searchView;
    private Button btnSort;
    private Button btnPinned;
    private Button btnTagFilter;
    private Button btnImportExport;
    private FloatingActionButton fabAdd;
    private ImageButton btnSettings;

    private boolean pinnedActive = false;
    private long currentLimit = 0;
    private static final String PREFS = "app_settings";
    private static final String KEY_PIN = "user_pin";
    private enum SortState {
        DATE_DESC,
        DATE_ASC,
        TITLE
    }

    private SortState sortState = SortState.DATE_DESC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 Load settings
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

        // 🔹 Apply tema PRIMA della view
        SettingsActivity.applyTheme(prefs);

        setContentView(R.layout.activity_main);

        // 🔹 Init UI
        initViews();
        setupRecycler();
        setupViewModel();
        setupAdapter();
        setupActions();

        // 🔹 Leggi preferenze (ti serviranno per filtrare/raggruppare)
        String aggregation = prefs.getString(SettingsActivity.KEY_AGGREGATION, "none");
        String dateRange = prefs.getString(SettingsActivity.KEY_DATE_RANGE, "7");

        applyFilters(aggregation, dateRange);
    }

    private void applyFilters(String aggregation, String range) {

        adapter.setAggregation(aggregation);

        long now = System.currentTimeMillis();

        switch (range) {
            case "30":
                currentLimit = now - (30L * 24 * 60 * 60 * 1000);
                break;
            case "365":
                currentLimit = now - (365L * 24 * 60 * 60 * 1000);
                break;
            default:
                currentLimit = now - (7L * 24 * 60 * 60 * 1000);
                break;
        }
    }
    private void initViews() {
        searchView = findViewById(R.id.searchView);
        btnSort = findViewById(R.id.btnSort);
        btnPinned = findViewById(R.id.btnPinned);
        btnTagFilter = findViewById(R.id.btnTagFilter);
        btnImportExport = findViewById(R.id.btnImportExport);
        fabAdd = findViewById(R.id.fabAdd);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        adapter = new NoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {

        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        viewModel.setShowPinnedOnly(false);

        btnPinned.setText("Pinned OFF");

        viewModel.getNotes().removeObservers(this);
        viewModel.getNotes().observe(this, this::updateAdapter);
    }

    private void updateAdapter(List<Note> notes) {

        List<Note> filtered = new ArrayList<>();

        for (Note n : notes) {
            if (n.getUpdatedAt() >= currentLimit) {
                filtered.add(n);
            }
        }

        adapter.setNotes(filtered, adapter.getAggregation());
    }



    private void setupAdapter() {

        adapter.setListener(new NoteAdapter.OnNoteActionListener() {

            @Override
            public void onNoteClick(Note note) {

                if (note.isProtected) {

                    authenticate(() -> {
                        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                        intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.id);
                        startActivity(intent);
                    });

                } else {
                    Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                    intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.id);
                    startActivity(intent);
                }
            }

            @Override
            public void onDelete(Note note) {

                viewModel.delete(note, null);
                WidgetUpdater.update(MainActivity.this);

                com.google.android.material.snackbar.Snackbar.make(
                        findViewById(android.R.id.content),
                        "Nota eliminata",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).setAction("UNDO", v -> {

                    viewModel.insert(note,null);
                    WidgetUpdater.update(MainActivity.this);

                }).show();
            }

            @Override
            public void onPin(Note note) {
                viewModel.setPinned(note.id, !note.isPinned());
                WidgetUpdater.update(MainActivity.this);

            }

            @Override
            public void onShare(Note note) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        note.title + "\n\n" + note.content);
                startActivity(Intent.createChooser(shareIntent, "Condividi nota"));
            }

            @Override
            public void onAddTag(Note note, String tag) {
                viewModel.updateTags(note.id, tag);
                WidgetUpdater.update(MainActivity.this);

            }
        });
    }

    private void setupActions() {

        btnImportExport.setOnClickListener(v ->
                startActivity(new Intent(this, ImportExportActivity.class))
        );

        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(clean(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(clean(newText));
                return true;
            }
        });

        btnSort.setOnClickListener(v -> {

            switch (sortState) {

                case DATE_DESC:
                    sortState = SortState.DATE_ASC;
                    viewModel.setSortType(SortType.DATE_ASC);
                    btnSort.setText("Data ↑");
                    break;

                case DATE_ASC:
                    sortState = SortState.TITLE;
                    viewModel.setSortType(SortType.TITLE_ASC);
                    btnSort.setText("Titolo");
                    break;

                default:
                    sortState = SortState.DATE_DESC;
                    viewModel.setSortType(SortType.DATE_DESC);
                    btnSort.setText("Data ↓");
                    break;
            }
        });

        btnPinned.setOnClickListener(v -> {

            pinnedActive = !pinnedActive;

            viewModel.setShowPinnedOnly(pinnedActive);

            btnPinned.setText(
                    pinnedActive ? "Pinned ON ⭐" : "Pinned OFF"
            );
        });

        btnTagFilter.setOnClickListener(v -> {

            android.app.AlertDialog.Builder builder =
                    new android.app.AlertDialog.Builder(this);

            builder.setTitle("Filtra per tag");

            final android.widget.EditText input =
                    new android.widget.EditText(this);

            input.setHint("es: android, java, work");

            builder.setView(input);

            builder.setPositiveButton("Filtra", (dialog, which) -> {

                String tag = clean(input.getText().toString());

                if (tag.isEmpty()) {
                    viewModel.setTagFilter(null); // 🔥 SOLO NULL = OFF
                    btnTagFilter.setText("Tag OFF");
                } else {
                    viewModel.setTagFilter(tag);
                    btnTagFilter.setText("Tag: " + tag);
                }
            });

            builder.setNegativeButton("Reset", (dialog, which) -> {
                viewModel.setTagFilter(null); // 🔥 FIX
                btnTagFilter.setText("Tags OFF");
            });

            builder.show();
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddNoteActivity.class))
        );

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }
    private void authenticate(Runnable onSuccess) {

        BiometricManager biometricManager = BiometricManager.from(this);

        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        switch (result) {

            case BiometricManager.BIOMETRIC_SUCCESS:
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:

                showPinDialog(onSuccess);
                return;

            default:
                Toast.makeText(this,
                        "Autenticazione non disponibile",
                        Toast.LENGTH_SHORT).show();
                return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        onSuccess.run();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(getApplicationContext(),
                                "Autenticazione fallita",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {

                        Toast.makeText(getApplicationContext(),
                                errString,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Sicurezza richiesta")
                        .setSubtitle("Conferma la tua identità per continuare")
                        .setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        )
                        .setNegativeButtonText("Annulla")
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

        String aggregation = prefs.getString(SettingsActivity.KEY_AGGREGATION, "none");
        String dateRange = prefs.getString(SettingsActivity.KEY_DATE_RANGE, "7");

        adapter.setAggregation(aggregation);
        applyFilters(aggregation, dateRange);

     }

    private void showPinDialog(Runnable onSuccess) {

        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Inserisci PIN")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {

                    String pin = input.getText().toString();

                    SharedPreferences prefs =
                            getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

                    String savedPin = prefs.getString(KEY_PIN, null);

                    if (savedPin != null && savedPin.equals(pin)) {
                        onSuccess.run();
                    } else {
                        android.widget.Toast.makeText(this,
                                "PIN errato",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

}
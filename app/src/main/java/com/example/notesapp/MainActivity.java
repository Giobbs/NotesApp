package com.example.notesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

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
        long limit;

        switch (range) {
            case "30":
                limit = now - (30L * 24 * 60 * 60 * 1000);
                break;
            case "365":
                limit = now - (365L * 24 * 60 * 60 * 1000);
                break;
            default:
                limit = now - (7L * 24 * 60 * 60 * 1000);
                break;
        }

        viewModel.getNotes().observe(this, notes -> {

            List<Note> filtered = new ArrayList<>();

            for (Note n : notes) {
                if (n.getUpdatedAt() >= limit) {
                    filtered.add(n);
                }
            }

            adapter.setNotes(filtered); // aggregation già dentro adapter
        });
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

        BiometricManager biometricManager =
                BiometricManager.from(this);

        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        android.util.Log.d("BIO", "canAuthenticate = " + result);

        if (result != BiometricManager.BIOMETRIC_SUCCESS) {

            android.util.Log.d("BIO", "Fallback ON");

            onSuccess.run();
            return;
        }

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        onSuccess.run();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Sblocca nota")
                        .setSubtitle("Autenticazione richiesta")
                        .setNegativeButtonText("Annulla")
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }


    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
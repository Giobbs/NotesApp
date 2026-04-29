package com.example.notesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.example.notesapp.util.UINotifier;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        ThemeManager.apply(prefs);

        setContentView(R.layout.activity_main);

        // 🔹 Init UI
        initViews();
        setupRecycler();
        setupViewModel();
        setupAdapter();
        setupActions();

        // 🔹 Leggi preferenze
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
                        note.isProtected = false;
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

                View root = findViewById(android.R.id.content);

                viewModel.delete(note, null);
                WidgetUpdater.update(MainActivity.this);

                UINotifier.showUndo(
                        root,
                        "Spostata nel cestino",
                        "Ripristina",
                        () -> {

                            viewModel.restore(note.id, () -> {

                                runOnUiThread(() -> {

                                    WidgetUpdater.update(MainActivity.this);


                                    root.post(() -> {
                                        UINotifier.showSuccess(root, "Ripristinata");
                                    });
                                });
                            });
                        }
                );
            }
            @Override
            public void onPin(Note note) {

                View root = findViewById(android.R.id.content);

                boolean newState = !note.isPinned();

                viewModel.setPinned(note.id, newState);
                WidgetUpdater.update(MainActivity.this);

                findViewById(android.R.id.content)
                        .performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                UINotifier.showUndo(
                        root,
                        newState ? "Aggiunta ai preferiti 📌" : "Rimossa dai preferiti",
                        "Annulla",
                        () -> {
                            viewModel.setPinned(note.id, !newState);
                            WidgetUpdater.update(MainActivity.this);
                        }
                );
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

                View root = findViewById(android.R.id.content);

                String clean = tag == null ? "" : tag.trim();

                if (clean.isEmpty()) {
                    UINotifier.showError(root, "Tag non valido");
                    return;
                }

                viewModel.updateTags(note.id, clean);
                WidgetUpdater.update(MainActivity.this);

                UINotifier.showSuccess(
                        root,
                        "Tag aggiunto: " + clean
                );
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

            List<Note> currentNotes = adapter.getCurrentList();
            List<String> allTags = extractAllTags(currentNotes);

            Set<String> selectedTags = new LinkedHashSet<>();

            android.app.AlertDialog.Builder builder =
                    new android.app.AlertDialog.Builder(this);

            builder.setTitle("Filtra per tag");

            // =========================
            // ROOT SCROLL CONTAINER
            // =========================
            android.widget.ScrollView scrollView = new android.widget.ScrollView(this);

            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(40, 30, 40, 30);

            scrollView.addView(container);

            // =========================
            // INPUT MANUALE TAG
            // =========================
            EditText input = new EditText(this);
            input.setHint("Aggiungi tag manuale...");
            input.setPadding(20, 20, 20, 20);
            container.addView(input);

            // =========================
            // SELECTED LABEL
            // =========================
            TextView selectedLabel = new TextView(this);
            selectedLabel.setText("Selezionati:");
            selectedLabel.setPadding(0, 25, 0, 10);
            container.addView(selectedLabel);

            // =========================
            // SELECTED CONTAINER
            // =========================
            LinearLayout selectedContainer = new LinearLayout(this);
            selectedContainer.setOrientation(LinearLayout.VERTICAL);
            container.addView(selectedContainer);

            // =========================
            // AVAILABLE LABEL
            // =========================
            TextView availableLabel = new TextView(this);
            availableLabel.setText("Disponibili:");
            availableLabel.setPadding(0, 30, 0, 10);
            container.addView(availableLabel);

            // =========================
            // TAG GRID (WRAPPING)
            // =========================
            android.widget.GridLayout tagContainer = new android.widget.GridLayout(this);
            tagContainer.setColumnCount(3);
            tagContainer.setUseDefaultMargins(true);

            container.addView(tagContainer);

            // =========================
            // ADD SELECTED CHIP
            // =========================
            java.util.function.Consumer<String> addSelectedChip = tag -> {

                com.google.android.material.chip.Chip chip =
                        new com.google.android.material.chip.Chip(this);

                chip.setText(tag);
                chip.setCloseIconVisible(true);
                chip.setCheckable(false);

                chip.setOnCloseIconClickListener(x -> {
                    selectedTags.remove(tag);
                    selectedContainer.removeView(chip);
                });

                selectedContainer.addView(chip);
            };

            // =========================
            // BUILD AVAILABLE TAGS
            // =========================
            for (String tag : allTags) {

                com.google.android.material.button.MaterialButton chip =
                        new com.google.android.material.button.MaterialButton(this);

                chip.setText(tag);
                chip.setAllCaps(false);
                chip.setCornerRadius(50);
                chip.setStrokeWidth(2);
                chip.setAlpha(0.6f);
                chip.setPadding(30, 10, 30, 10);

                chip.setOnClickListener(c -> {

                    if (selectedTags.contains(tag)) {

                        selectedTags.remove(tag);
                        chip.setAlpha(0.6f);
                        chip.setStrokeWidth(2);

                    } else {

                        selectedTags.add(tag);
                        chip.setAlpha(1f);
                        chip.setStrokeWidth(0);

                        addSelectedChip.accept(tag);
                    }
                });

                tagContainer.addView(chip);
            }

            // =========================
            // DIALOG VIEW
            // =========================
            builder.setView(scrollView);

            builder.setPositiveButton("Applica", (dialog, which) -> {

                List<String> manual = Note.parseTags(input.getText().toString());
                selectedTags.addAll(manual);

                if (selectedTags.isEmpty()) {
                    viewModel.setTagFilters(null);
                    btnTagFilter.setText("Tag OFF");
                } else {
                    List<String> finalTags = new ArrayList<>(selectedTags);
                    viewModel.setTagFilters(finalTags);
                    btnTagFilter.setText("Tags: " + String.join(", ", finalTags));
                }
            });

            builder.setNegativeButton("Reset", (dialog, which) -> {
                viewModel.setTagFilters(null);
                btnTagFilter.setText("Tag OFF");
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
    private List<String> extractAllTags(List<Note> notes) {

        Set<String> tags = new LinkedHashSet<>();

        for (Note n : notes) {
            tags.addAll(n.getTagList());
        }

        return new ArrayList<>(tags);
    }
}
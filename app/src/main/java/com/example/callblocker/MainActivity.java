package com.example.callblocker;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.callblocker.adapter.RangeAdapter;
import com.example.callblocker.model.NumberRange;
import com.example.callblocker.service.CallBlockService;
import com.example.callblocker.service.NotificationService;
import com.example.callblocker.service.RangeManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RangeManager rangeManager;
    private NotificationService notificationService;
    private RangeAdapter adapter;
    private ListView listView;
    private Button addRangeBtn;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean allGranted = result.values().stream().allMatch(granted -> granted);
                        if (allGranted) {
                            requestCallScreeningRole();
                        } else {
                            showPermissionDialog();
                        }
                    });

    private final ActivityResultLauncher<Intent> roleRequestLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        checkCallScreeningRole();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            initComponents();
            loadRanges();
            checkPermissions();
            startCallBlockService();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private void initComponents() {
        rangeManager = new RangeManager(this);
        notificationService = new NotificationService(this);
        listView = findViewById(R.id.rangesListView);
        addRangeBtn = findViewById(R.id.addRangeButton);

        addRangeBtn.setOnClickListener(v -> showAddRangeDialog());

        // Créer le canal de notification
        notificationService.createNotificationChannel();
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.ANSWER_PHONE_CALLS
        };

        // Ajouter permission notifications pour Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissionsWithNotif = new String[permissions.length + 1];
            System.arraycopy(permissions, 0, permissionsWithNotif, 0, permissions.length);
            permissionsWithNotif[permissions.length] = Manifest.permission.POST_NOTIFICATIONS;
            permissions = permissionsWithNotif;
        }

        boolean hasAllPermissions = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
                break;
            }
        }

        if (!hasAllPermissions) {
            permissionLauncher.launch(permissions);
        } else {
            requestCallScreeningRole();
        }
    }

    private void requestCallScreeningRole() {
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            roleRequestLauncher.launch(intent);
        } else {
            checkCallScreeningRole();
        }
    }

    private void checkCallScreeningRole() {
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager != null && roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            Toast.makeText(this, "Application configurée pour le blocage d'appels", Toast.LENGTH_SHORT).show();
        } else {
            showRoleRequiredDialog();
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions requises")
                .setMessage("Cette application a besoin des permissions téléphone et notifications pour fonctionner correctement.")
                .setPositiveButton("Paramètres", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showRoleRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Configuration requise")
                .setMessage("Pour bloquer les appels, cette application doit être définie comme service de filtrage d'appels.")
                .setPositiveButton("Configurer", (dialog, which) -> requestCallScreeningRole())
                .setNegativeButton("Plus tard", null)
                .show();
    }

    private void showDefaultDialerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Application de téléphone")
                .setMessage("Pour les versions Android antérieures, définir cette app comme composeur par défaut peut améliorer le blocage.")
                .setPositiveButton("Paramètres", (dialog, which) -> {
                    Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                    intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton("Ignorer", null)
                .show();
    }

    private void startCallBlockService() {
        Intent serviceIntent = new Intent(this, CallBlockService.class);
        startForegroundService(serviceIntent);
    }

    private void loadRanges() {
        List<NumberRange> ranges = rangeManager.loadRanges();
        adapter = new RangeAdapter(this, ranges, this::deleteRange);
        listView.setAdapter(adapter);
    }

    private void showAddRangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une plage");

        androidx.appcompat.widget.LinearLayoutCompat layout = new androidx.appcompat.widget.LinearLayoutCompat(this);
        layout.setOrientation(androidx.appcompat.widget.LinearLayoutCompat.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Nom de la plage");
        layout.addView(nameInput);

        EditText prefixInput = new EditText(this);
        prefixInput.setHint("Préfixe de la plage");
        prefixInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(prefixInput);

        builder.setView(layout);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String prefix = prefixInput.getText().toString().trim();

            if (!name.isEmpty() && !prefix.isEmpty()) {
                NumberRange range = NumberRange.builder()
                        .name(name)
                        .prefix(prefix)
                        .isActive(true)
                        .build();

                rangeManager.addRange(range);
                loadRanges();
                Toast.makeText(this, "Plage ajoutée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void deleteRange(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la plage")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette plage ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    rangeManager.removeRange(position);
                    loadRanges();
                    Toast.makeText(this, "Plage supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
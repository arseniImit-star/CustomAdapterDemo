package com.example.customadapterdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_AVATAR_REQUEST = 501;
    private static final String PREFS = "contact_hub";
    private static final String SAVED_CONTACTS = "saved_contacts";

    private final ArrayList<User> users = new ArrayList<>();
    private UserListAdapter adapter;
    private ListView listView;
    private ImageView dialogAvatarPreview;
    private String pendingAvatarUri;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list);

        loadUsers();
        adapter = new UserListAdapter(this, users);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> adapter.select(users.get(position)));

        findViewById(R.id.sortName).setOnClickListener(v ->
                sort(Comparator.comparing(u -> u.name.toLowerCase(java.util.Locale.forLanguageTag("ru")))));
        findViewById(R.id.sortPhone).setOnClickListener(v ->
                sort(Comparator.comparing(u -> u.phoneNumber)));
        findViewById(R.id.sortAvatar).setOnClickListener(v ->
                sort(Comparator.comparing(u -> u.sex.title)));
        findViewById(R.id.addButton).setOnClickListener(v -> showAddDialog());
    }

    private void loadUsers() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString(SAVED_CONTACTS, null);
        if (saved != null) {
            parseUsers(saved);
            return;
        }

        try (InputStream in = getAssets().open("contacts.json");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
            parseUsers(out.toString(StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось загрузить contacts.json: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void parseUsers(String json) {
        try {
            JSONArray array = new JSONArray(json);
            users.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("name", "");
                String phone = obj.optString("phoneNumber", "");
                Sex sex = Sex.valueOf(obj.optString("sex", "UNKNOWN"));
                String avatarUri = obj.isNull("avatarUri") ? null : obj.optString("avatarUri", null);
                users.add(new User(name, phone, sex, avatarUri));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка чтения списка контактов: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void saveUsers() {
        try {
            JSONArray array = new JSONArray();
            for (User user : users) {
                JSONObject obj = new JSONObject();
                obj.put("name", user.name);
                obj.put("phoneNumber", user.phoneNumber);
                obj.put("sex", user.sex.name());
                obj.put("avatarUri", user.avatarUri == null ? JSONObject.NULL : user.avatarUri);
                array.put(obj);
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(SAVED_CONTACTS, array.toString()).apply();
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось сохранить контакты", Toast.LENGTH_SHORT).show();
        }
    }

    private void sort(Comparator<User> comparator) {
        Collections.sort(users, comparator);
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
    }

    private void showAddDialog() {
        pendingAvatarUri = null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        form.setPadding(pad, pad / 2, pad, 0);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Имя и фамилия");
        nameInput.setSingleLine(true);
        nameInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        form.addView(nameInput);

        EditText phoneInput = new EditText(this);
        phoneInput.setHint("Номер телефона");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneInput.setSingleLine(true);
        form.addView(phoneInput);

        TextView avatarLabel = new TextView(this);
        avatarLabel.setText("Тип аватарки");
        avatarLabel.setPadding(0, pad / 2, 0, 0);
        form.addView(avatarLabel);

        Spinner avatarSpinner = new Spinner(this);
        avatarSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Sex.values()));
        form.addView(avatarSpinner);

        dialogAvatarPreview = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                (int) (88 * getResources().getDisplayMetrics().density),
                (int) (88 * getResources().getDisplayMetrics().density));
        imageParams.topMargin = pad / 2;
        imageParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        dialogAvatarPreview.setLayoutParams(imageParams);
        dialogAvatarPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        dialogAvatarPreview.setImageResource(R.drawable.avatar_unknown);
        form.addView(dialogAvatarPreview);
        avatarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (pendingAvatarUri == null) {
                    dialogAvatarPreview.setImageResource(((Sex) avatarSpinner.getSelectedItem()).imageRes);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        Button choosePhoto = new Button(this);
        choosePhoto.setText("Выбрать своё фото");
        choosePhoto.setAllCaps(false);
        form.addView(choosePhoto);
        choosePhoto.setOnClickListener(v -> openImagePicker());

        new AlertDialog.Builder(this)
                .setTitle("Новый контакт")
                .setView(form)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Заполните имя и телефон", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    users.add(new User(name, phone, (Sex) avatarSpinner.getSelectedItem(), pendingAvatarUri));
                    saveUsers();
                    adapter.notifyDataSetChanged();
                    listView.setSelection(users.size() - 1);
                })
                .show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_AVATAR_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AVATAR_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Некоторые приложения-галереи не выдают постоянное разрешение.
            }
            pendingAvatarUri = uri.toString();
            if (dialogAvatarPreview != null) dialogAvatarPreview.setImageURI(uri);
            Toast.makeText(this, "Аватарка выбрана", Toast.LENGTH_SHORT).show();
        }
    }
}

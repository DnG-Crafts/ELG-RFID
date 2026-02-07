package dngsoftware.elgrfid;

import static android.view.View.TEXT_ALIGNMENT_CENTER;
import dngsoftware.elgrfid.databinding.ActivityMainBinding;
import dngsoftware.elgrfid.databinding.PickerDialogBinding;
import dngsoftware.elgrfid.databinding.TagDialogBinding;
import static dngsoftware.elgrfid.Utils.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.navigation.NavigationView;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback, NavigationView.OnNavigationItemSelectedListener {
    private NfcAdapter nfcAdapter;
    Tag currentTag = null;
    ArrayAdapter<String> tadapter, wadapter;
    ArrayAdapter<Filament> sadapter;
    String MaterialType = "PLA", MaterialWeight = "1 KG", MaterialColor = "0000FF";
    int intType = 0, intSubtype, extMin, extMax;
    Dialog pickerDialog, tagDialog;
    AlertDialog inputDialog;
    private Handler mainHandler;
    int tagType;
    private Toast currentToast;
    int SelectedSize;
    private ActivityMainBinding main;
    Bitmap gradientBitmap;
    tagItem[] tagItems;
    private ExecutorService executorService;
    private ActivityResultLauncher<Void> cameraLauncher;
    NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private PickerDialogBinding colorDialog;
    tagAdapter recycleAdapter;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeMode(GetSetting(this, "enabledm", false));
        Resources res = getApplicationContext().getResources();
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());

        main = ActivityMainBinding.inflate(getLayoutInflater());
        View rv = main.getRoot();
        setContentView(rv);

        SetPermissions(this);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        setupActivityResultLaunchers();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MenuItem launchItem = navigationView.getMenu().findItem(R.id.nav_launch);
        SwitchCompat launchSwitch = Objects.requireNonNull(launchItem.getActionView()).findViewById(R.id.drawer_switch);
        launchSwitch.setChecked(GetSetting(this, "autoLaunch", true));
        launchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setNfcLaunchMode(this, isChecked);
            SaveSetting(this, "autoLaunch", isChecked);
        });

        MenuItem readItem = navigationView.getMenu().findItem(R.id.nav_read);
        SwitchCompat readSwitch = Objects.requireNonNull(readItem.getActionView()).findViewById(R.id.drawer_switch);
        readSwitch.setChecked(GetSetting(this, "autoread", false));
        readSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SaveSetting(this, "autoread", isChecked);
        });

        MenuItem darkItem = navigationView.getMenu().findItem(R.id.nav_dark);
        SwitchCompat darkSwitch = Objects.requireNonNull(darkItem.getActionView()).findViewById(R.id.drawer_switch);
        darkSwitch.setChecked(GetSetting(this, "enabledm", false));
        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SaveSetting(this, "enabledm", isChecked);
            setThemeMode(isChecked);
        });


        main.txtcolor.setText(MaterialColor);
        main.txtcolor.setTextColor(getContrastColor(Color.parseColor("#" + MaterialColor)));

        main.colorview.setOnClickListener(view -> openPicker());
        main.colorview.setBackgroundColor(Color.rgb(0, 0, 255));
        main.readbutton.setOnClickListener(view -> readTag(currentTag));

        main.writebutton.setOnClickListener(view -> writeTag(currentTag ,MaterialType, intType, intSubtype, MaterialColor,GetMaterialIntWeight(MaterialWeight)));

        main.menubutton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        main.colorspin.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    openPicker();
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return false;
        });

        wadapter = new ArrayAdapter<>(this, R.layout.spinner_item, materialWeights);
        main.spoolsize.setAdapter(wadapter);
        main.spoolsize.setSelection(SelectedSize);
        main.spoolsize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SelectedSize = main.spoolsize.getSelectedItemPosition();
                MaterialWeight = wadapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });


        tadapter = new ArrayAdapter<>(this, R.layout.spinner_item, filamentTypes);
        main.type.setAdapter(tadapter);
        main.type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                MaterialType = tadapter.getItem(position);
                intType = position;
                loadSubtypes(MaterialType);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                Bundle options = new Bundle();
                options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
                nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, options);
            }
        }catch (Exception ignored) {}
    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (nfcAdapter != null) {
                nfcAdapter.disableReaderMode(this);
            }
        } catch (Exception ignored) {}
    }


    private void loadSubtypes(String materialType)
    {
        List<Filament> subTypes = getFilamentSubTypes(materialType);
        if (!subTypes.isEmpty()) {
            sadapter = new ArrayAdapter<>(this, R.layout.spinner_item, subTypes);
            main.subtype.setAdapter(sadapter);
            main.subtype.setSelection(0);
            main.subtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    Filament selected = (Filament) parentView.getItemAtPosition(position);
                    extMin = selected.minTemp;
                    extMax = selected.maxTemp;
                    intSubtype = selected.id;
                    main.infotext.setText(String.format(Locale.getDefault(), getString(R.string.ext_temps), extMin, extMax));
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            try {
                nfcAdapter.disableReaderMode(this);
            } catch (Exception ignored) {
            }
        }
        if (pickerDialog != null && pickerDialog.isShowing()) {
            pickerDialog.dismiss();
        }
        if (inputDialog != null && inputDialog.isShowing()) {
            inputDialog.dismiss();
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (pickerDialog != null && pickerDialog.isShowing()) {
            pickerDialog.dismiss();
            openPicker();
        }
        if (inputDialog != null && inputDialog.isShowing()) {
            inputDialog.dismiss();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_format) {
            formatTag(currentTag);
        } else if (id == R.id.nav_memory) {
           loadTagMemory();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onTagDiscovered(Tag tag) {
        try {
            runOnUiThread(() -> {
                byte[] uid = tag.getId();
                if (uid.length >= 6) {
                    currentTag = tag;
                    tagType = getTagType(NfcA.get(currentTag));
                    showToast(getString(R.string.tag_found) + bytesToHex(uid, false), Toast.LENGTH_SHORT);
                    int tagType = getNtagType(NfcA.get(currentTag));
                    main.tagid.setText(bytesToHex(uid, true));
                    main.tagtype.setText(String.format(Locale.getDefault(), "   NTAG%d", tagType));
                    main.lbltagid.setVisibility(View.VISIBLE);
                    main.lbltagtype.setVisibility(View.VISIBLE);
                    if (GetSetting(this, "autoread", false)) {
                        readTag(currentTag);
                    }
                }
                else {
                    currentTag = null;
                    main.tagid.setText("");
                    main.tagtype.setText("");
                    main.lbltagid.setVisibility(View.INVISIBLE);
                    main.lbltagtype.setVisibility(View.INVISIBLE);
                    showToast(R.string.invalid_tag_type, Toast.LENGTH_SHORT);
                }
            });
        } catch (Exception ignored) {
        }
    }


    public void readTag(Tag tag) {
        try {
            NfcA nfcA = NfcA.get(tag);
            if (tag == null) {
                showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
                return;
            }
            executorService.execute(() -> {
                try {
                    nfcA.connect();
                    byte[] data = new byte[96];
                    ByteBuffer buff = ByteBuffer.wrap(data);
                    for (int page = 4; page < 28; page += 4) {
                        byte[] pageData = transceive(nfcA, new byte[]{(byte) 0x30, (byte) page});
                        if (pageData != null) {
                            buff.put(pageData);
                        }
                    }
                    if (buff.array()[48] == (byte) 0x36) {
                        mainHandler.post(() -> {
                            MaterialType = decodeMaterial(subArray(buff.array(), 56, 4)).trim();
                            main.type.setSelection(tadapter.getPosition(MaterialType));
                            MaterialColor = bytesToHex(subArray(buff.array(), 64, 3), false);
                            main.colorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                            main.txtcolor.setText(MaterialColor);
                            main.txtcolor.setTextColor(getContrastColor(Color.parseColor("#" + MaterialColor)));
                            main.spoolsize.setSelection(wadapter.getPosition(GetMaterialWeight(ByteBuffer.wrap(subArray(buff.array(), 78, 2)).getShort())));
                            main.infotext.setText(String.format(Locale.getDefault(), getString(R.string.ext_temps),
                                    ByteBuffer.wrap(subArray(buff.array(), 68, 2)).getShort(), ByteBuffer.wrap(subArray(buff.array(), 70, 2)).getShort()));
                            List<Filament> subTypes = getFilamentSubTypes(MaterialType);
                            int pos = getPositionById(subTypes, buff.array()[61]);
                            mainHandler.postDelayed(() -> {
                                main.subtype.setSelection(pos);
                            }, 500);
                        });
                        showToast(R.string.data_read_from_tag, Toast.LENGTH_SHORT);
                    } else {
                        showToast(R.string.unknown_or_empty_tag, Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    showToast(R.string.error_reading_tag, Toast.LENGTH_SHORT);
                } finally {
                    try {
                        nfcA.close();
                    } catch (Exception ignored) {
                    }
                }
            });
        }catch (Exception ignored) {
            showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
        }
    }


    private void writeTag(Tag tag, String typeName, int type, int subType, String colorHex, int weightGrams) {
        try {
            NfcA nfcA = NfcA.get(tag);
            if (tag == null) {
                showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
                return;
            }
            executorService.execute(() -> {
                try {
                    nfcA.connect();
                    byte[] empty = new byte[]{0, 0, 0, 0};
                    for (int i = 4; i < 16; i++) {
                        writeTagPage(nfcA, i, empty);
                    }
                    writeTagPage(nfcA, 16, new byte[]{0x36, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE});
                    writeTagPage(nfcA, 17, new byte[]{(byte) 0xEE, 0, 0, 0});
                    //type
                    writeTagPage(nfcA, 18, encodeMaterial(typeName));
                    // sub type
                    writeTagPage(nfcA, 19, new byte[]{(byte) type, (byte) subType, 0, 0});
                    // color
                    writeTagPage(nfcA, 20, hexToByte(colorHex + "FF"));
                    // ext min  / ext max
                    writeTagPage(nfcA, 21, doubleBE(extMin, extMax));
                    //  bed temp range?
                    writeTagPage(nfcA, 22, empty);
                    //diameter / weight
                    writeTagPage(nfcA, 23, doubleBE(175, weightGrams));
                    // prod date?
                    writeTagPage(nfcA, 24, new byte[]{0, (byte) 0x36, (byte) 0xC8, 0});
                    playBeep();
                    showToast(R.string.data_written_to_tag, Toast.LENGTH_SHORT);
                } catch (Exception e) {
                    showToast(R.string.error_writing_to_tag, Toast.LENGTH_SHORT);
                } finally {
                    try {
                        nfcA.close();
                    } catch (Exception ignored) {
                    }
                }
            });
        } catch (Exception ignored) {
            showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    void openPicker() {
        try {
            pickerDialog = new Dialog(this, R.style.Theme_ElgRFID);
            pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pickerDialog.setCanceledOnTouchOutside(false);
            pickerDialog.setTitle(R.string.pick_color);
            PickerDialogBinding dl = PickerDialogBinding.inflate(getLayoutInflater());
            View rv = dl.getRoot();
            colorDialog = dl;
            pickerDialog.setContentView(rv);
            gradientBitmap = null;

            dl.btncls.setOnClickListener(v -> {
                if (dl.txtcolor.getText().toString().length() == 6) {
                    try {
                        MaterialColor = dl.txtcolor.getText().toString();
                        int color = Color.rgb(dl.redSlider.getProgress(), dl.greenSlider.getProgress(), dl.blueSlider.getProgress());
                        main.colorview.setBackgroundColor(color);

                        main.txtcolor.setText(MaterialColor);
                        main.txtcolor.setTextColor(getContrastColor(Color.parseColor("#" + MaterialColor)));

                    } catch (Exception ignored) {
                    }
                }
                pickerDialog.dismiss();
            });

            dl.redSlider.setProgress(Color.red(Color.parseColor("#" + MaterialColor)));
            dl.greenSlider.setProgress(Color.green(Color.parseColor("#" + MaterialColor)));
            dl.blueSlider.setProgress(Color.blue(Color.parseColor("#" + MaterialColor)));


            setupPresetColors(dl);
            updateColorDisplay(dl, dl.redSlider.getProgress(), dl.greenSlider.getProgress(), dl.blueSlider.getProgress());

            setupGradientPicker(dl);

            dl.gradientPickerView.setOnTouchListener((v, event) -> {
                v.performClick();
                if (gradientBitmap == null) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    float touchX = event.getX();
                    float touchY = event.getY();
                    int pixelX = Math.max(0, Math.min(gradientBitmap.getWidth() - 1, (int) touchX));
                    int pixelY = Math.max(0, Math.min(gradientBitmap.getHeight() - 1, (int) touchY));
                    int pickedColor = gradientBitmap.getPixel(pixelX, pixelY);
                    setSlidersFromColor(dl, Color.rgb(Color.red(pickedColor), Color.green(pickedColor), Color.blue(pickedColor)));
                    return true;
                }
                return false;
            });

            setupCollapsibleSection(dl,
                    dl.rgbSlidersHeader,
                    dl.rgbSlidersContent,
                    dl.rgbSlidersToggleIcon,
                    GetSetting(this,"RGB_VIEW",false)
            );
            setupCollapsibleSection(dl,
                    dl.gradientPickerHeader,
                    dl.gradientPickerContent,
                    dl.gradientPickerToggleIcon,
                    GetSetting(this,"PICKER_VIEW",true)
            );
            setupCollapsibleSection(dl,
                    dl.presetColorsHeader,
                    dl.presetColorsContent,
                    dl.presetColorsToggleIcon,
                    GetSetting(this,"PRESET_VIEW",true)
            );
            setupCollapsibleSection(dl,
                    dl.photoColorHeader,
                    dl.photoColorContent,
                    dl.photoColorToggleIcon,
                    GetSetting(this, "PHOTO_VIEW", false)
            );

            SeekBar.OnSeekBarChangeListener rgbChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    updateColorDisplay(dl, dl.redSlider.getProgress(), dl.greenSlider.getProgress(), dl.blueSlider.getProgress());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            };

            dl.redSlider.setOnSeekBarChangeListener(rgbChangeListener);
            dl.greenSlider.setOnSeekBarChangeListener(rgbChangeListener);
            dl.blueSlider.setOnSeekBarChangeListener(rgbChangeListener);

            dl.txtcolor.setOnClickListener(v -> showHexInputDialog(dl));

            dl.photoImage.setOnClickListener(v -> {
                Drawable drawable = ContextCompat.getDrawable(dl.photoImage.getContext(), R.drawable.camera);
                if (dl.photoImage.getDrawable() != null && drawable != null) {
                    if (Objects.equals(dl.photoImage.getDrawable().getConstantState(), drawable.getConstantState())) {
                        checkPermissionsAndCapture();
                    }
                } else {
                    checkPermissionsAndCapture();
                }
            });

            dl.clearImage.setOnClickListener(v -> {

                dl.photoImage.setImageResource( R.drawable.camera);
                dl.photoImage.setDrawingCacheEnabled(false);
                dl.photoImage.buildDrawingCache(false);
                dl.photoImage.setOnTouchListener(null);
                dl.clearImage.setVisibility(View.GONE);

            });

            pickerDialog.show();
        } catch (Exception ignored) {}
    }


    private void updateColorDisplay(PickerDialogBinding dl,int currentRed,int currentGreen,int currentBlue) {
        int color = Color.rgb(currentRed, currentGreen, currentBlue);
        dl.colorDisplay.setBackgroundColor(color);
        String hexCode = rgbToHex(currentRed, currentGreen, currentBlue);
        dl.txtcolor.setText(hexCode);
        double alphaNormalized = 255.0;
        int blendedRed = (int) (currentRed * alphaNormalized + 244 * (1 - alphaNormalized));
        int blendedGreen = (int) (currentGreen * alphaNormalized + 244 * (1 - alphaNormalized));
        int blendedBlue = (int) (currentBlue * alphaNormalized + 244 * (1 - alphaNormalized));
        double brightness = (0.299 * blendedRed + 0.587 * blendedGreen + 0.114 * blendedBlue) / 255;
        if (brightness > 0.5) {
            dl.txtcolor.setTextColor(Color.BLACK);
        } else {
            dl.txtcolor.setTextColor(Color.WHITE);
        }

    }


    private void setupPresetColors(PickerDialogBinding dl) {
        dl.presetColorGrid.removeAllViews();
        for (int color : presetColors()) {
            Button colorButton = new Button(this);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.preset_circle_size),
                    (int) getResources().getDimension(R.dimen.preset_circle_size)
            );
            params.setMargins(
                    (int) getResources().getDimension(R.dimen.preset_circle_margin),
                    (int) getResources().getDimension(R.dimen.preset_circle_margin),
                    (int) getResources().getDimension(R.dimen.preset_circle_margin),
                    (int) getResources().getDimension(R.dimen.preset_circle_margin)
            );
            colorButton.setLayoutParams(params);
            GradientDrawable circleDrawable = (GradientDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.circle_shape, null);
            assert circleDrawable != null;
            circleDrawable.setColor(color);
            colorButton.setBackground(circleDrawable);
            colorButton.setTag(color);
            colorButton.setOnClickListener(v -> {
                int selectedColor = (int) v.getTag();
                setSlidersFromColor(dl, selectedColor);
            });
            dl.presetColorGrid.addView(colorButton);
        }
    }


    private void setSlidersFromColor(PickerDialogBinding dl, int argbColor) {
        dl.redSlider.setProgress(Color.red(argbColor));
        dl.greenSlider.setProgress(Color.green(argbColor));
        dl.blueSlider.setProgress(Color.blue(argbColor));
        updateColorDisplay(dl, Color.red(argbColor), Color.green(argbColor), Color.blue(argbColor));
    }


    private void showHexInputDialog(PickerDialogBinding dl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.enter_hex_color_aarrggbb);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setHint(R.string.aarrggbb);
        input.setTextColor(Color.BLACK);
        input.setHintTextColor(Color.GRAY);
        input.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        input.setText(rgbToHex(dl.redSlider.getProgress(), dl.greenSlider.getProgress(), dl.blueSlider.getProgress()));
        InputFilter[] filters = new InputFilter[3];
        filters[0] = new HexInputFilter();
        filters[1] = new InputFilter.LengthFilter(8);
        filters[2] = new InputFilter.AllCaps();
        input.setFilters(filters);
        builder.setView(input);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.submit, (dialog, which) -> {
            String hexInput = input.getText().toString().trim();
            if (isValidHexCode(hexInput)) {
                setSlidersFromColor(dl, Color.parseColor("#" + hexInput));
            } else {
                showToast(R.string.invalid_hex_code_please_use_aarrggbb_format, Toast.LENGTH_LONG);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        inputDialog = builder.create();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidthPx = displayMetrics.widthPixels;
        float density = getResources().getDisplayMetrics().density;
        int maxWidthDp = 100;
        int maxWidthPx = (int) (maxWidthDp * density);
        int dialogWidthPx = (int) (screenWidthPx * 0.80);
        if (dialogWidthPx > maxWidthPx) {
            dialogWidthPx = maxWidthPx;
        }
        Objects.requireNonNull(inputDialog.getWindow()).setLayout(dialogWidthPx, WindowManager.LayoutParams.WRAP_CONTENT);
        inputDialog.getWindow().setGravity(Gravity.CENTER); // Center the dialog on the screen
        inputDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = inputDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = inputDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(Color.parseColor("#82B1FF"));
            negativeButton.setTextColor(Color.parseColor("#82B1FF"));
        });
        inputDialog.show();
    }


    void setupGradientPicker(PickerDialogBinding dl) {
        dl.gradientPickerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                dl.gradientPickerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = dl.gradientPickerView.getWidth();
                int height = dl.gradientPickerView.getHeight();
                if (width > 0 && height > 0) {
                    gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(gradientBitmap);
                    Paint paint = new Paint();
                    float[] hsv = new float[3];
                    hsv[1] = 1.0f;
                    for (int y = 0; y < height; y++) {
                        hsv[2] = 1.0f - (float) y / height;
                        for (int x = 0; x < width; x++) {
                            hsv[0] = (float) x / width * 360f;
                            paint.setColor(Color.HSVToColor(255, hsv));
                            canvas.drawPoint(x, y, paint);
                        }
                    }
                    dl.gradientPickerView.setBackground(new BitmapDrawable(getResources(), gradientBitmap));
                }
            }
        });
    }


    private void setupCollapsibleSection(PickerDialogBinding dl, LinearLayout header, final ViewGroup content, final ImageView toggleIcon, boolean isExpandedInitially) {
        content.setVisibility(isExpandedInitially ? View.VISIBLE : View.GONE);
        toggleIcon.setImageResource(isExpandedInitially ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
        header.setOnClickListener(v -> {
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
                toggleIcon.setImageResource(R.drawable.ic_arrow_down);
                if (header.getId() == dl.rgbSlidersHeader.getId()) {
                    SaveSetting(this,"RGB_VIEW",false);
                }
                else if (header.getId() == dl.gradientPickerHeader.getId()) {
                    SaveSetting(this,"PICKER_VIEW",false);
                }
                else if (header.getId() == dl.presetColorsHeader.getId()) {
                    SaveSetting(this,"PRESET_VIEW",false);
                }
                else if (header.getId() == dl.photoColorHeader.getId()) {
                    SaveSetting(this,"PHOTO_VIEW",false);
                }
            } else {
                content.setVisibility(View.VISIBLE);
                toggleIcon.setImageResource(R.drawable.ic_arrow_up);
                if (header.getId() == dl.rgbSlidersHeader.getId()) {
                    SaveSetting(this,"RGB_VIEW",true);
                }
                else if (header.getId() == dl.gradientPickerHeader.getId()) {
                    SaveSetting(this,"PICKER_VIEW",true);
                    if (gradientBitmap == null) {
                        setupGradientPicker(dl);
                    }
                }
                else if (header.getId() == dl.presetColorsHeader.getId()) {
                    SaveSetting(this,"PRESET_VIEW",true);
                }
                else if (header.getId() == dl.photoColorHeader.getId()) {
                    SaveSetting(this,"PHOTO_VIEW",true);
                }
            }
        });
    }


    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        colorDialog.photoImage.setImageBitmap(bitmap);
                        setupPhotoPicker(colorDialog.photoImage);
                    } else {
                        // Handle failure or cancellation
                        showToast(R.string.photo_capture_cancelled_or_failed, Toast.LENGTH_SHORT);
                    }
                }
        );
    }


    private void showToast(final Object content, final int duration) {
        mainHandler.post(() -> {
            if (currentToast != null) currentToast.cancel();
            if (content instanceof Integer) {
                currentToast = Toast.makeText(this, (Integer) content, duration);
            } else if (content instanceof String) {
                currentToast = Toast.makeText(this, (String) content, duration);
            } else {
                currentToast = Toast.makeText(this, String.valueOf(content), duration);
            }
            currentToast.show();
        });
    }


    private void checkPermissionsAndCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
        else {
            takePicture();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                showToast(R.string.camera_permission_is_required_to_take_photos, Toast.LENGTH_SHORT);
            }
        }
    }


    private void takePicture() {
        if (cameraLauncher != null) {
            cameraLauncher.launch(null);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setupPhotoPicker(ImageView imageView) {
        colorDialog.clearImage.setVisibility(View.VISIBLE);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);
        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                Bitmap bitmap = imageView.getDrawingCache();
                float touchX = event.getX();
                float touchY = event.getY();
                if (touchX >= 0 && touchX < bitmap.getWidth() && touchY >= 0 && touchY < bitmap.getHeight()) {
                    try {
                        int pixel = bitmap.getPixel((int) touchX, (int) touchY);
                        int r = Color.red(pixel);
                        int g = Color.green(pixel);
                        int b = Color.blue(pixel);
                        colorDialog.colorDisplay.setBackgroundColor(Color.rgb(r, g, b));
                        colorDialog.txtcolor.setText(String.format("%06X", (0xFFFFFF & pixel)));
                        setSlidersFromColor(colorDialog, Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel)));
                    } catch (Exception ignored) {}
                }
            }
            return true;
        });
    }


    void loadTagMemory() {
        try {
            tagDialog = new Dialog(this, R.style.Theme_ElgRFID);
            tagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            tagDialog.setCanceledOnTouchOutside(false);
            tagDialog.setTitle("Tag Memory");
            TagDialogBinding tdl = TagDialogBinding.inflate(getLayoutInflater());
            View rv = tdl.getRoot();
            tagDialog.setContentView(rv);
            tdl.btncls.setOnClickListener(v -> tagDialog.dismiss());
            tdl.btnread.setOnClickListener(v -> readTagMemory(tdl));
            recyclerView = tdl.recyclerView;
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.scrollToPosition(0);
            recyclerView.setLayoutManager(layoutManager);
            tagItems = new tagItem[0];
            recycleAdapter = new tagAdapter(this, tagItems);
            recyclerView.setAdapter(recycleAdapter);
            tagDialog.show();
            readTagMemory(tdl);
        } catch (Exception ignored) {}
    }


    void readTagMemory(TagDialogBinding tdl) {
        if (currentTag == null) {
            showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
            return;
        }
        executorService.execute(() -> {
            try {

                NfcA nfcA = NfcA.get(currentTag);
                if (nfcA != null) {
                    try {
                        int maxPages = (tagType == 216) ? 231 : (tagType == 215) ? 135 : 45;
                        if (tagType == 100) maxPages = 48;
                        mainHandler.post(() -> tdl.lbldesc.setText(tagType == 100 ? "UL-C" : "NTAG" + tagType));
                        tagItems = new tagItem[maxPages];
                        for (int i = 0; i < maxPages; i += 4) {
                            byte[] data = transceive(nfcA, new byte[]{0x30, (byte) i});
                            for (int offset = 0; offset < 4; offset++) {
                                int currentPage = i + offset;
                                if (currentPage >= maxPages) break;
                                byte[] pageData = new byte[4];
                                System.arraycopy(data, offset * 4, pageData, 0, 4);
                                String hexString = bytesToHex(pageData, true);
                                String definition = getPageDefinition(currentPage, tagType);
                                tagItems[currentPage] = new tagItem();
                                tagItems[currentPage].tKey = String.format(Locale.getDefault(), "Page %d | %s", currentPage, definition);
                                tagItems[currentPage].tValue = hexString;
                                if (currentPage < 2) {
                                    tagItems[currentPage].tImage = AppCompatResources.getDrawable(this, R.drawable.locked);
                                } else if (definition.contains("User Data")) {
                                    tagItems[currentPage].tImage = AppCompatResources.getDrawable(this, R.drawable.writable);
                                } else {
                                    tagItems[currentPage].tImage = AppCompatResources.getDrawable(this, R.drawable.internal);
                                }
                            }
                        }
                        mainHandler.post(() -> {
                            recycleAdapter = new tagAdapter(this, tagItems);
                            recycleAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(recycleAdapter);
                        });
                    } catch (Exception ignored) {
                        showToast(R.string.error_reading_tag, Toast.LENGTH_SHORT);
                    } finally {
                        try {
                            if (nfcA.isConnected()) nfcA.close();
                        } catch (Exception ignored) {
                        }
                    }
                } else {
                    showToast(R.string.invalid_tag_type, Toast.LENGTH_SHORT);
                }

            }catch (Exception ignored) {
                showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
            }
        });
    }


    private void formatTag(Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SpannableString titleText = new SpannableString(getString(R.string.format_tag));
        titleText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_brand)), 0, titleText.length(), 0);
        SpannableString messageText = new SpannableString(getString(R.string.this_will_erase_the_data_on_the_tag_and_format_it_for_writing));
        messageText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_main)), 0, messageText.length(), 0);
        builder.setTitle(titleText);
        builder.setMessage(messageText);
        builder.setPositiveButton(R.string.format, (dialog, which) -> {

            if (tag == null) {
                showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
                return;
            }

            executorService.execute(() -> {
                try {
                    NfcA nfcA = NfcA.get(tag);
                    if (nfcA != null) {
                        byte[] empty = new byte[]{0, 0, 0, 0};
                        try {
                            byte[] ccBytes;
                            if (tagType == 216) {
                                ccBytes = new byte[]{(byte) 0xE1, (byte) 0x10, (byte) 0x6D, (byte) 0x00};
                            } else if (tagType == 215) {
                                ccBytes = new byte[]{(byte) 0xE1, (byte) 0x10, (byte) 0x3E, (byte) 0x00};
                            } else if (tagType == 100) {
                                ccBytes = new byte[]{(byte) 0xE1, (byte) 0x10, (byte) 0x06, (byte) 0x00};
                            } else {
                                ccBytes = new byte[]{(byte) 0xE1, (byte) 0x10, (byte) 0x12, (byte) 0x00};
                            }
                            showToast(R.string.formatting_tag, Toast.LENGTH_SHORT);

                            writeTagPage(nfcA, 2, empty);
                            writeTagPage(nfcA, 3, ccBytes);
                            for (int i = 4; i < 32; i++) {
                                writeTagPage(nfcA, i, empty);
                            }
                            showToast(R.string.tag_formatted, Toast.LENGTH_SHORT);
                            if (nfcA.isConnected()) nfcA.close();
                        } catch (Exception e) {
                            showToast(R.string.failed_to_format_tag_for_writing, Toast.LENGTH_SHORT);
                        } finally {
                            try {
                                if (nfcA.isConnected()) nfcA.close();
                            } catch (Exception ignored) {
                            }
                        }
                    } else {
                        showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
                    }
                } catch (Exception ignored) {
                    showToast(R.string.no_nfc_tag_found, Toast.LENGTH_SHORT);
                }
            });
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
        if (alert.getWindow() != null) {
            alert.getWindow().setBackgroundDrawableResource(R.color.background_alt);
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.primary_brand));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary_brand));
        }
    }


}
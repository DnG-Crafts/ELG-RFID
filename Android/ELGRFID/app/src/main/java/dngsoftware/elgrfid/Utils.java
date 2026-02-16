package dngsoftware.elgrfid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.nfc.tech.NfcA;
import android.text.InputFilter;
import android.text.Spanned;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressLint("GetInstance")
public class Utils {

    public static String[] filamentTypes = {
            "PLA",
            "PETG",
            "ABS",
            "TPU",
            "PA",
            "CPE",
            "PC",
            "PVA",
            "ASA",
            "BVOH",
            "EVA",
            "HIPS",
            "PP",
            "PPA",
            "PPS"
    };

    public static List<Filament> getFilamentSubTypes(String filamentType) {
        List<Filament> list = new ArrayList<>();
        switch (filamentType) {
            case "PLA":
                list.add(new Filament(0, "PLA", 190, 230));
                list.add(new Filament(1, "PLA+", 190, 230));
                list.add(new Filament(2, "PLA PRO", 190, 230));
                list.add(new Filament(3, "PLA Silk", 190, 230));
                list.add(new Filament(4, "PLA-CF", 210, 240));
                list.add(new Filament(6, "PLA Matte", 190, 230));
                list.add(new Filament(8, "PLA Wood", 190, 230));
                list.add(new Filament(9, "PLA Basic", 190, 230));
                list.add(new Filament(10, "RAPID PLA+", 190, 230));
                list.add(new Filament(11, "PLA Marble", 190, 230));
                list.add(new Filament(12, "PLA Galaxy", 190, 230));
                list.add(new Filament(13, "PLA Red Copper", 190, 230));
                break;
            case "PETG":
                list.add(new Filament(0, "PETG", 230, 260));
                list.add(new Filament(1, "PETG-CF", 240, 270));
                list.add(new Filament(2, "PETG-GF", 240, 270));
                list.add(new Filament(3, "PETG PRO", 230, 260));
                list.add(new Filament(4, "PETG Translucent", 230, 260));
                list.add(new Filament(5, "RAPID PETG", 230, 260));
                break;
            case "ABS":
                list.add(new Filament(0, "ABS", 240, 280));
                break;
            case "TPU":
                list.add(new Filament(1, "TPU 95A", 220, 240));
                list.add(new Filament(2, "RAPID TPU 95A", 220, 240));
                break;
            case "PA":
                list.add(new Filament(0, "PAHT-CF", 280, 320));
                break;
            case "CPE":
                list.add(new Filament(0, "CPE", 250, 250));
                break;
            case "PC":
                list.add(new Filament(0, "PC", 260, 290));
                list.add(new Filament(2, "PC-FR", 260, 290));
                break;
            case "PVA":
                list.add(new Filament(0, "PVA", 210, 210));
                break;
            case "ASA":
                list.add(new Filament(0, "ASA", 240, 280));
                break;
            case "BVOH":
                list.add(new Filament(0, "BVOH", 210, 210));
                break;
            case "EVA":
                list.add(new Filament(0, "EVA", 220, 220));
                break;

            case "HIPS":
                list.add(new Filament(0, "HIPS", 250, 250));
                break;
            case "PP":
                list.add(new Filament(0, "PP", 260, 260));
                break;
            case "PPA":
                list.add(new Filament(0, "PPA", 310, 310));
                break;
            case "PPS":
                list.add(new Filament(0, "PPS", 350, 350));
                break;
        }
        return list;
    }

    public static String GetMaterialWeight(int materialLength) {
        switch (materialLength) {
            case 1000:
                return "1 KG";
            case 750:
                return "750 G";
            case 600:
                return "600 G";
            case 500:
                return "500 G";
            case 250:
                return "250 G";
        }
        return "1 KG";
    }

    public static int GetMaterialIntWeight(String materialWeight) {
        switch (materialWeight) {
            case "1 KG":
                return 1000;
            case "750 G":
                return 750;
            case "600 G":
                return 600;
            case "500 G":
                return 500;
            case "250 G":
                return 250;
        }
        return 1000;
    }

    public static String[] materialWeights = {
            "1 KG",
            "750 G",
            "600 G",
            "500 G",
            "250 G"
    };

    public static byte[] doubleBE(int firstVal, int secondVal) {
        byte[] data = new byte[4];
        data[0] = (byte) ((firstVal >> 8) & 0xFF);
        data[1] = (byte) (firstVal & 0xFF);
        data[2] = (byte) ((secondVal >> 8) & 0xFF);
        data[3] = (byte) (secondVal & 0xFF);
        return data;
    }

    public static byte[] hexToByte(String hexString) {
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            String subString = hexString.substring(i, i + 2);
            byteArray[i / 2] = (byte) Integer.parseInt(subString, 16);
        }
        return byteArray;
    }

    public static String bytesToHex(byte[] data, boolean space) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            if (space) {
                sb.append(String.format("%02X ", b));
            } else {
                sb.append(String.format("%02X", b));
            }
        }
        return sb.toString();
    }

    public static byte[] subArray(byte[] source, int startIndex, int length) {
        if (source == null) {
            return null;
        }
        int sourceLength = source.length;
        if (startIndex < 0 || startIndex >= sourceLength || length <= 0) {
            return new byte[0];
        }
        int endIndex = Math.min(startIndex + length, sourceLength);
        int actualLength = endIndex - startIndex;
        byte[] result = new byte[actualLength];
        System.arraycopy(source, startIndex, result, 0, actualLength);
        return result;
    }

    public static int getPositionById(List<Filament> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                return i;
            }
        }
        return 0;
    }

    public static byte[] encodeMaterial(String material) {
        byte[] result = new byte[4];
        int length = Math.min(material.length(), 4);
        for (int i = 0; i < length; i++) {
            result[3 - i] = (byte) Integer.parseInt(Integer.toString(material.charAt(material.length() - 1 - i)), 16);
        }
        return result;
    }

    public static String decodeMaterial(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte datum : data) {
            int b = datum & 0xFF;
            if (b == 0) continue;
            String hexString = Integer.toHexString(b);
            int asciiValue = Integer.parseInt(hexString);
            sb.append((char) asciiValue);
        }
        return sb.toString();
    }

    public static void writeTagPage(NfcA nfcA, int page, byte[] data) throws Exception {
        byte[] cmd = new byte[6];
        cmd[0] = (byte) 0xA2;
        cmd[1] = (byte) page;
        System.arraycopy(data, 0, cmd, 2, data.length);
        transceive(nfcA, cmd);
    }

    public static int getTagType(NfcA nfcA) {
        if (probePage(nfcA, (byte) 220)) return 216;
        if (probePage(nfcA, (byte) 125)) return 215;
        if (probePage(nfcA, (byte) 47)) return 100;
        return 213;
    }

    public static byte[] transceive(NfcA nfcA, byte[] data) throws Exception {
        if (!nfcA.isConnected()) nfcA.connect();
        return nfcA.transceive(data);
    }

    public static void writeUrl(String inputUrl, NfcA nfcA, int tagType) {
        try {
            byte sizeByte;
            if (tagType == 215) {
                sizeByte = (byte) 0x3E;
            } else if (tagType == 216) {
                sizeByte = (byte) 0x6D;
            } else {
                sizeByte = (byte) 0xA0;
            }
            String cleanUrl = inputUrl;
            byte uriIdentifier = 0x02;
            if (inputUrl.startsWith("https://www.")) {
                cleanUrl = inputUrl.substring(12);
            } else if (inputUrl.startsWith("http://www.")) {
                uriIdentifier = 0x01;
                cleanUrl = inputUrl.substring(11);
            } else if (inputUrl.startsWith("https://")) {
                uriIdentifier = 0x04;
                cleanUrl = inputUrl.substring(8);
            } else if (inputUrl.startsWith("http://")) {
                uriIdentifier = 0x03;
                cleanUrl = inputUrl.substring(7);
            }
            byte[] urlBytes = cleanUrl.getBytes(StandardCharsets.US_ASCII);
            int payloadLength = urlBytes.length + 1;
            int ndefMsgLength = payloadLength + 4;
            ByteBuffer bb = ByteBuffer.allocate(urlBytes.length + 20);
            bb.put(new byte[]{(byte)0x01, (byte)0x03, sizeByte, (byte)0x0C, (byte)0x34});
            bb.put((byte)0x03);
            bb.put((byte)ndefMsgLength);
            bb.put((byte)0xD1);
            bb.put((byte)0x01);
            bb.put((byte)payloadLength);
            bb.put((byte)0x55);
            bb.put(uriIdentifier);
            bb.put(urlBytes);
            bb.put((byte)0xFE);
            byte[] fullPayload = bb.array();
            int totalLen = bb.position();
            int startPage = 4;
            for (int i = 0; i < totalLen; i += 4) {
                byte[] pageData = new byte[4];
                int bytesToCopy = Math.min(4, totalLen - i);
                System.arraycopy(fullPayload, i, pageData, 0, bytesToCopy);
                writeTagPage(nfcA, startPage + (i / 4), pageData);
            }
        } catch (Exception ignored) {}
    }

    public static void SetPermissions(Context context) {
        String[] REQUIRED_PERMISSIONS = {Manifest.permission.NFC, Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Activity activity = (Activity) context;
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            String[] permsArray = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, permsArray, 200);
        }
    }

    public static void playBeep() {
        new Thread(() -> {
            try {
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300);
                toneGenerator.stopTone();
                toneGenerator.release();
            } catch (Exception ignored) {
            }
        }).start();
    }

    public static void setNfcLaunchMode(Context context, boolean allowLaunch ) {
        ComponentName componentName = new ComponentName(context, LaunchActivity.class);
        PackageManager packageManager = context.getPackageManager();
        if (allowLaunch) {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
        else {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public static String GetSetting(Context context, String sKey, String sDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getString(sKey, sDefault);
    }

    public static boolean GetSetting(Context context, String sKey, boolean bDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getBoolean(sKey, bDefault);
    }

    public static int GetSetting(Context context, String sKey, int iDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getInt(sKey, iDefault);
    }

    public static long GetSetting(Context context, String sKey, long lDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getLong(sKey, lDefault);
    }

    public static void SaveSetting(Context context, String sKey, String sValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(sKey, sValue);
        editor.apply();
    }

    public static void SaveSetting(Context context, String sKey, boolean bValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(sKey, bValue);
        editor.apply();
    }

    public static void SaveSetting(Context context, String sKey, int iValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(sKey, iValue);
        editor.apply();
    }

    public static void SaveSetting(Context context, String sKey, long lValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(sKey, lValue);
        editor.apply();
    }

    public static int getContrastColor(@ColorInt int backgroundColor) {
        int red = Color.red(backgroundColor);
        int green = Color.green(backgroundColor);
        int blue = Color.blue(backgroundColor);
        double luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0;
        return (luminance > 0.5) ? Color.BLACK : Color.WHITE;
    }

    public static void setThemeMode(boolean enabled)
    {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static String rgbToHex(int r, int g, int b) {
        return String.format("%02X%02X%02X", r, g, b);
    }

    public static class HexInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder filtered = new StringBuilder();
            for (int i = start; i < end; i++) {
                char character = source.charAt(i);
                if (Character.isDigit(character) || (character >= 'a' && character <= 'f') || (character >= 'A' && character <= 'F')) {
                    filtered.append(character);
                }
            }
            return filtered.toString();
        }
    }

    public static boolean isValidHexCode(String hexCode) {
        Pattern pattern = Pattern.compile("^[0-9a-fA-F]{8}$");
        Matcher matcher = pattern.matcher(hexCode);
        return matcher.matches();
    }

    public static int[] presetColors() {
        return new int[]{
                Color.parseColor("#25C4DA"),
                Color.parseColor("#0099A7"),
                Color.parseColor("#0B359A"),
                Color.parseColor("#0A4AB6"),
                Color.parseColor("#11B6EE"),
                Color.parseColor("#90C6F5"),
                Color.parseColor("#FA7C0C"),
                Color.parseColor("#F7B30F"),
                Color.parseColor("#E5C20F"),
                Color.parseColor("#B18F2E"),
                Color.parseColor("#8D766D"),
                Color.parseColor("#6C4E43"),
                Color.parseColor("#E62E2E"),
                Color.parseColor("#EE2862"),
                Color.parseColor("#EA2A2B"),
                Color.parseColor("#E83D89"),
                Color.parseColor("#AE2E65"),
                Color.parseColor("#611C8B"),
                Color.parseColor("#8D60C7"),
                Color.parseColor("#B287C9"),
                Color.parseColor("#006764"),
                Color.parseColor("#018D80"),
                Color.parseColor("#42B5AE"),
                Color.parseColor("#1D822D"),
                Color.parseColor("#54B351"),
                Color.parseColor("#72E115"),
                Color.parseColor("#474747"),
                Color.parseColor("#668798"),
                Color.parseColor("#B1BEC6"),
                Color.parseColor("#58636E"),
                Color.parseColor("#F8E911"),
                Color.parseColor("#F6D311"),
                Color.parseColor("#F2EFCE"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#000000")

                /* elegoo list
                // Row 1
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#44F1FF"),
                Color.parseColor("#78F4DC"),
                Color.parseColor("#33D700"),
                Color.parseColor("#487705"),
                Color.parseColor("#84620D"),
                Color.parseColor("#A0E20E"),
                // Row 2
                Color.parseColor("#FFC800"),
                Color.parseColor("#FF8C00"),
                Color.parseColor("#FF3700"),
                Color.parseColor("#893044"),
                Color.parseColor("#F73CA0"),
                Color.parseColor("#DDB1D4"),
                Color.parseColor("#735DF9"),
                // Row 3
                Color.parseColor("#2323F7"),
                Color.parseColor("#004B7C"),
                Color.parseColor("#0080FF"),
                Color.parseColor("#A3C5D3"),
                Color.parseColor("#3379AF"),
                Color.parseColor("#BCBCBC"),
                Color.parseColor("#161616"),
                 */

        };
    }

    public static int getNtagType(NfcA nfcA) {
        if (probePage(nfcA, (byte) 50)) {
            if (probePage(nfcA, (byte) 150)) {
                return 216;
            } else {
                return 215;
            }
        } else {
            return 213;
        }
    }

    public static boolean probePage(NfcA nfcA, byte pageNumber) {
        try {
            nfcA.connect();
            byte[] result = nfcA.transceive(new byte[] {(byte) 0x30, pageNumber});
            if (result != null && result.length == 16) {
                return true;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                nfcA.close();
            } catch (Exception ignored) {}
        }
        return false;
    }

    public static String getPageDefinition(int page, int type) {
        if (page == 0) return "UID 0-2 / Internal";
        if (page == 1) return "UID 3-6";
        if (page == 2) return "Internal / BCC / Lock Bytes";
        if (page == 3) return "Capability Container (CC)";
        if (type == 213 || type == 215 || type == 216) {
            int cfgStart = (type == 213) ? 41 : (type == 215) ? 131 : 227;
            if (page >= 4 && page < cfgStart) return "User Data / NDEF";
            if (page == cfgStart) return "Config (Mirror / Auth0)";
            if (page == cfgStart + 1) return "Access (PROT / CFGLCK)";
            if (page == cfgStart + 2) return "PWD (Password)";
            if (page == cfgStart + 3) return "PACK / Target ID";
            return "End of Memory";
        }
        if (type == 100) {
            if (page >= 4 && page <= 39) return "User Data";
            if (page >= 40 && page <= 43) return "3DES Keys (Write-Only)";
            if (page == 44) return "Auth Start (AUTH0)";
            if (page == 45) return "Auth Config (AUTH1)";
            return "Internal";
        }
        return "Unknown";
    }
}
package vip.frendy.camera.settings;

import android.hardware.Camera;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frendy on 2018/4/10.
 */

public class SettingISO {
    private static final String TAG = "SettingISO";
    //参考值
    public static int[] DEFAULT_VALUES = {-1, 100, 200, 400, 800, 1600};

    public static void setISO(Camera camera, int preferredISO) {
        Camera.Parameters parameters = camera.getParameters();
        Pair<String, String> iso = getISO(parameters, preferredISO);
        parameters.set(iso.first, iso.second);
        camera.setParameters(parameters);

        Log.i(TAG, "** Set ISO values as " + iso.first + " - " + iso.second);
    }


    private static Pair<String, String> getISO(Camera.Parameters parameters, int preferredISO) {
        String keyISO = getISOKey(parameters);

        if(preferredISO < 0) return Pair.create(keyISO, "auto");

        List<Integer> isoValues = getSupportedISOValues(parameters);

        if(isoValues == null) return Pair.create(keyISO, "auto");
        Log.i(TAG, "** Supported ISO values: " + isoValues);

        int iso = Integer.MAX_VALUE;
        int delta = Integer.MAX_VALUE;
        int deltaCurrent;
        int isoCurrent;

        for(int i = 0; i < isoValues.size(); i++) {
            isoCurrent = isoValues.get(i);
            deltaCurrent = Math.abs(isoCurrent - preferredISO);

            if(deltaCurrent == 0) {
                iso = isoCurrent;
                break;
            } else if(deltaCurrent < delta) {
                delta = deltaCurrent;
                iso = isoCurrent;
            }
        }

        if(iso != preferredISO) {
            Log.w(TAG, "** Failed to find the preferred ISO value of " + preferredISO + ", Defaulting to 'auto'");
            return Pair.create(keyISO, "auto");
        }

        return Pair.create(keyISO, Integer.toString(iso));
    }

    private static List<Integer> getSupportedISOValues(Camera.Parameters parameters) {
        List<Integer> isoValues = new ArrayList<Integer>();
        String[] isoValuesStrArray = null;
        String flat = parameters.flatten();
        String keySupportedISOValues = getSupportedISOValuesKey(parameters);

        if(keySupportedISOValues != null) {
            String isoValuesStr = flat.substring(flat.indexOf(keySupportedISOValues));
            isoValuesStr = isoValuesStr.substring(isoValuesStr.indexOf("=") + 1);

            if(isoValuesStr.contains(";"))
                isoValuesStr = isoValuesStr.substring(0, isoValuesStr.indexOf(";"));

            isoValuesStrArray = isoValuesStr.split(",");
            String isoValueStr;

            for(String anIsoValuesStrArray : isoValuesStrArray) {
                try {
                    isoValueStr = anIsoValuesStrArray;
                    isoValueStr = isoValueStr.replaceAll("iso|ISO", "");

                    if (isoValueStr.equalsIgnoreCase("auto")) {
                        isoValueStr = "-1";
                    }

                    isoValues.add(Integer.parseInt(isoValueStr));
                } catch (NumberFormatException e) {
                    Log.i(TAG, "** Skip string ISO values such as iso_hjr");
                }
            }
        }

        if(isoValues.size() == 0) {
            Log.w(TAG, "** Unable to get supported ISO values");
            return null;
        }

        return isoValues;
    }

    private static String getISOKey(Camera.Parameters parameters) {
        String flattenParams = parameters.flatten();

        if(flattenParams.contains("iso-values")) {
            //most used keywords
            return "iso";
        } else if(flattenParams.contains("iso-mode-values")) {
            //google galaxy nexus keywords
            return "iso";
        } else if(flattenParams.contains("iso-speed-values")) {
            //micromax a101 keywords
            return "iso-speed";
        } else if(flattenParams.contains("nv-picture-iso-values")) {
            //LG dual p990 keywords
            return "nv-picture-iso";
        }
        return null;
    }

    private static String getSupportedISOValuesKey(Camera.Parameters parameters) {
        String flattenParams = parameters.flatten();

        if(flattenParams.contains("iso-values")) {
            return "iso-values";
        } else if(flattenParams.contains("iso-mode-values")) {
            return "iso-mode-values";
        } else if(flattenParams.contains("iso-speed-values")) {
            return "iso-speed-values";
        } else if(flattenParams.contains("nv-picture-iso-values")) {
            return "nv-picture-iso-values";
        }
        return null;
    }

}

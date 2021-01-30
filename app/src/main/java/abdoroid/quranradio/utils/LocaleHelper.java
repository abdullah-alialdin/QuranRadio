package abdoroid.quranradio.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static Context onAttach(Context context) {
        StorageUtils storageUtils = new StorageUtils(context);
        return setLocale(context, storageUtils.loadLanguage());
    }

    public static Context setLocale(Context context, String language) {
        StorageUtils storageUtils = new StorageUtils(context);
        storageUtils.storeLanguage(language);
        return updateResources(context, language);
    }

    public static void setLocale(Context context) {
        StorageUtils storageUtils = new StorageUtils(context);
        String language = storageUtils.loadLanguage();
        updateResources(context, language);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        }
        return context.createConfigurationContext(configuration);
    }

}

package abdoroid.quranradio.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.transition.Slide;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.concurrent.TimeUnit;

public class Helper {

    public static void setAnimation(Activity activity) {
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.START);
        slide.setDuration(400);
        slide.setInterpolator(new DecelerateInterpolator());
        activity.getWindow().setExitTransition(slide);
        activity.getWindow().setEnterTransition(slide);
    }

    public static int setDarkMode(Context context){
        StorageUtils storageUtils = new StorageUtils(context);
        boolean darkIsChecked = storageUtils.loadDarkMode();
        if (darkIsChecked){
            return AppCompatDelegate.MODE_NIGHT_YES;
        }else {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
    }

    public static int[] getTimeFromMilliseconds(long millis){
        int hours = (int) TimeUnit.MILLISECONDS.toHours(millis);
        int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        return new int[]{hours, minutes, seconds};
    }

    @SuppressWarnings( "deprecation" )
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null){
                result =  (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                        ||(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                        ||(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            }
        } else {
            result = !(activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting());
        }
        return !result;
    }

}

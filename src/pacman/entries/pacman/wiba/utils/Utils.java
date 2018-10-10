package pacman.entries.pacman.wiba.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Utils {

    public static String getFormattedTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        return sdf.format(date);
    }
}

package com.longway.theme;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by longway on 2015/11/18.
 * Assert中资源管理类
 */
public final class AssertUtils {
    private static final String TAG = "AssertUtils";

    /**
     * 从Assert中复制资源到目标目录中
     *
     * @param descDir    目标目录。只是目录
     * @param descFile   目标文件名，只需要文件名，不需要路径
     * @param assertPath Assert里资源的路径
     * @return 是否复制成功
     */
    public static boolean copyFileFromAssert(final String descDir, final String descFile, final String assertPath) {
        File dir = new File(descDir);
        if (!dir.exists() && !dir.mkdir()) {
            return false; // 插件目录不能访问到，则不能做下一步的事情
        }
        File targetFile = new File(descDir + File.separator + descFile);

        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            InputStream in = ThemeApplication.getInstance().getAssets().open(assertPath);
            inBuff = new BufferedInputStream(in);
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024 * 10];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            outBuff.flush();
            return true;
        } catch (Throwable e) {
            Log.w(TAG, e);
            return false;
        } finally {
            try {
                if (inBuff != null) {
                    inBuff.close();
                }
                if (outBuff != null) {
                    outBuff.close();
                }
            } catch (Throwable e) {
                Log.w(TAG, e);
            }
        }
    }

}

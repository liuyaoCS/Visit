package com.menglvren.visit;

/**
 * Created by Administrator on 2015/12/24 0024.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashSet;

import android.content.Context;


public class IpListCache {
    private Context context;

    public IpListCache(Context _context){
        this.context = _context;
    }

    public void saveWhiteIpList(HashSet<String> datas){
        write(getWhiteIpListCacheFilePath(),datas);
    }
    public void saveBlackIpList(HashSet<String> datas){
        write(getBlackIpListCacheFilePath(),datas);
    }

    public HashSet<String> getWhiteIpList(){
        return read(getWhiteIpListCacheFilePath());
    }
    public HashSet<String> getBlackIpList(){
        return read(getBlackIpListCacheFilePath());
    }

    private String getWhiteIpListCacheFilePath() {
        return context.getCacheDir().getPath() + File.separator
                + "whiteIpList";
    }
    private String getBlackIpListCacheFilePath() {
        return context.getCacheDir().getPath() + File.separator
                + "blackIpList";
    }

    private void write(String fileName, HashSet<String> _datas){
        if (_datas == null) {
            return;
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(_datas);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.flush();
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private HashSet<String> read(String fileName){
        HashSet<String> datas = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(fileName));
            datas = new HashSet<String>();
            datas = (HashSet<String>)ois.readObject();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return datas;
    }
}


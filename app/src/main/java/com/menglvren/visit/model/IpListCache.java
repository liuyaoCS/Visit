package com.menglvren.visit.model;

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

    public void saveIpList(HashSet<Server> datas){
        write(getIpListCacheFilePath(),datas);
    }

    public HashSet<Server> getIpList(){
        return read(getIpListCacheFilePath());
    }

    private String getIpListCacheFilePath() {
        return context.getCacheDir().getPath() + File.separator
                + "ipList";
    }


    private void write(String fileName, HashSet<Server> _datas){
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
    private HashSet<Server> read(String fileName){
        HashSet<Server> datas = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(fileName));
            datas = new HashSet<Server>();
            datas = (HashSet<Server>)ois.readObject();
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


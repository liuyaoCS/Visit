package com.menglvren.visit.model;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashSet;

/**
 * Created by ly on 2015/12/25.
 */
public class VipListCache {
    private Context context;

    public VipListCache(Context _context){
        this.context = _context;
    }

    public void saveVipList(HashSet<Server> datas){
        write(getVipListCacheFilePath(),datas);
    }

    public HashSet<Server> getVipList(){
        return read(getVipListCacheFilePath());
    }

    private String getVipListCacheFilePath() {
        return context.getCacheDir().getPath() + File.separator
                + "vipList";
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

package com.leafpic.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.leafpic.app.utils.string;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class HandlingAlbums {

    public ArrayList<Album> dispAlbums;
    public int last_position_selecte = -1;
    private Context context;
    private ArrayList<Album> selectedAlbums;

    public HandlingAlbums(Context ctx) {
        context = ctx;
        dispAlbums = new ArrayList<Album>();
        selectedAlbums = new ArrayList<Album>();
    }

    public int getSelectedCount() {
        return selectedAlbums.size();
    }

    public void loadHiddenAlbums() {
        DatabaseHandler db = new DatabaseHandler(context);
        if (db.getDataBaseHiddenPhotosCount() == 0)
            db.loadHiddenALbums();
        dispAlbums = db.getHiddenAlbums();
        for (Album dispAlbum : dispAlbums) {
            dispAlbum.setHidden(true);
            dispAlbum.photos = db.getFirstPhotosByAlbum(dispAlbum.Path);
        }
        db.close();
    }

    public void loadPreviewHiddenAlbums() {
        DatabaseHandler db = new DatabaseHandler(context);
        if (db.getDataBaseHiddenPhotosCount() == 0)
            db.loadHiddenALbums();
        dispAlbums = db.getHiddenAlbums();
        for (Album dispAlbum : dispAlbums) {
            dispAlbum.setHidden(true);
            dispAlbum.photos = db.getFirstPhotosByAlbum(dispAlbum.Path);
        }
        db.close();
    }

    public void renameAlbum(String olderPath, String name) {
        try {
            DatabaseHandler db = new DatabaseHandler(context);
            File from = new File(olderPath);
            File to = new File(string.getAlbumPathRenamed(olderPath, name));
            from.renameTo(to);
            db.renameAlbum(olderPath, name);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(from)));
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(to)));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void loadAlbums() {
        DatabaseHandler db = new DatabaseHandler(context);
        dispAlbums = db.getAllAlbums();
        for (Album dispAlbum : dispAlbums)
            dispAlbum.photos = db.getPhotosByAlbum(dispAlbum.Path);

        db.close();
    }

    public void loadPreviewAlbums() {
        DatabaseHandler db = new DatabaseHandler(context);
        dispAlbums = db.getAllAlbums();
        for (Album dispAlbum : dispAlbums)
            dispAlbum.photos = db.getFirstPhotosByAlbum(dispAlbum.Path);

        db.close();
    }


    public void selectAlbum(Album a, boolean val) {
        Album x = dispAlbums.get(dispAlbums.indexOf(a));
        x.setPath();
        x.setSelcted(val);
        if (val) selectedAlbums.add(x);
        else selectedAlbums.remove(x);
    }

    public int selectAlbum(String a, boolean val) {
        Album x = getAlbum(a);
        if (x != null) {
            x.setPath();
            x.setSelcted(val);
            if (val) selectedAlbums.add(x);
            else selectedAlbums.remove(x);
        }
        return last_position_selecte;
    }

    public Album getAlbum(String p) {
        for (int i = 0; i < dispAlbums.size(); i++) {
            if (dispAlbums.get(i).Path.equals(p)) {
                last_position_selecte = i;
                return dispAlbums.get(i);

            }
        }
        return null;
    }

    public void hideSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            hideAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void hideAlbum(Album a) {
        hideAlbum(a.Path);
        dispAlbums.remove(a);
    }

    public void hideAlbum(String path) {
        DatabaseHandler db = new DatabaseHandler(context);
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (!file.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                out.flush();
                out.close();
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.hideAlbum(path);
        db.close();
    }


    public void unHideSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            unHideAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void unHideAlbum(Album a) {
        unHideAlbum(a.Path);
        dispAlbums.remove(a);
    }

    public void unHideAlbum(String path) {
        DatabaseHandler db = new DatabaseHandler(context);
        File dirName = new File(path);
        File file = new File(dirName, ".nomedia");
        if (file.exists()) {
            try {
                file.delete();
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.unHideAlbum(path);
        db.close();
    }



    public void deleteSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            deleteAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void deleteAlbum(Album a) {
        deleteAlbum(a.Path);
        dispAlbums.remove(a);
    }

    public void deleteAlbum(String path) {
        DatabaseHandler db = new DatabaseHandler(context);
        File dir = new File(path);
        deleteFolderRecursive(dir);
        db.deleteAlbum(path);
        db.close();
    }

    public void excludeSelectedAlbums() {
        for (Album selectedAlbum : selectedAlbums)
            excludeAlbum(selectedAlbum);

        clearSelectedAlbums();
    }

    public void excludeAlbum(Album a) {
        excludeAlbum(a.Path);
        dispAlbums.remove(a);
    }

    public void excludeAlbum(String path) {
        DatabaseHandler db = new DatabaseHandler(context);
        db.excludeAlbum(path);
        db.close();
    }

    void deleteFolderRecursive(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File temp = new File(dir, children[i]);
                if (temp.isDirectory())
                    deleteFolderRecursive(temp);
                else {
                    temp.delete();
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(temp)));
                }
            }
        }
        dir.delete();
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dir)));
    }

    public void clearSelectedAlbums() {
        for (Album dispAlbum : dispAlbums) {
            dispAlbum.setSelcted(false);
        }
        selectedAlbums.clear();
    }

    public void Log() {
        Log.i("numero album", "" + dispAlbums.size());
        for (int i = 0; i < dispAlbums.size(); i++) {
            Log.i("album", dispAlbums.get(i).DisplayName + " " + dispAlbums.get(i).getImagesCount());
            for (Photo photo : dispAlbums.get(i).photos) {
                Log.i("foto", "PHOTO_FOLDER_PATH->>" + photo.FolderPath);
                Log.i("foto", "PHOTO_PATH->>" + photo.Path);
            }
        }
    }
}
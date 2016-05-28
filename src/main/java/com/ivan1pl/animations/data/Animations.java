/* 
 *  Copyright (C) 2016 Ivan1pl
 * 
 *  This file is part of Animations.
 * 
 *  Animations is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Animations is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Animations.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ivan1pl.animations.data;

import com.ivan1pl.animations.AnimationsPlugin;
import com.ivan1pl.animations.constants.Messages;
import com.ivan1pl.animations.constants.OperationResult;
import com.ivan1pl.animations.tasks.AnimationTask;
import com.ivan1pl.animations.utils.MessageUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author Ivan1pl
 */
public class Animations {
    
    private static final Map<String, Animation> animations = new HashMap<>();
    
    private static final Map<UUID, Selection> selections = new HashMap<>();
    
    private static final File PLUGIN_DIR = new File(AnimationsPlugin.getPluginInstance().getDataFolder()
            + File.separator + "animations");
    
    @Getter
    private static Material wandMaterial = null;
    
    private static final Set<AnimationTask> runningTasks = new HashSet<>();
    
    private static final int PAGE_SIZE = 10;
    
    static {
        if (!PLUGIN_DIR.exists()) {
            PLUGIN_DIR.mkdirs();
        }
    }
    
    private Animations() { }
    
    public static void reload() {
        FilenameFilter aFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".anim");
            }
            
        };
        
        String wand = AnimationsPlugin.getPluginInstance().getConfig().getString("wand");
        wandMaterial = Material.valueOf(wand);
        
        if (wandMaterial == null) {
            wandMaterial = Material.BLAZE_POWDER;
            AnimationsPlugin.getPluginInstance().getLogger().info(MessageUtil.formatMessage(Messages.INFO_INVALID_MATERIAL, wand, Material.BLAZE_POWDER.toString()));
        }
        
        animations.clear();
        
        for (File f : PLUGIN_DIR.listFiles(aFilter)) {
            String name = f.getName();
            name = name.substring(0, name.length()-5);
            
            reloadAnimation(name);
        }
    }
    
    public static Animation getAnimation(String name) {
        return animations.get(name);
    }
    
    public static void setAnimation(String name, Animation animation) {
        animations.put(name, animation);
    }
    
    public static OperationResult saveAnimation(String name) {
        Animation animation = animations.get(name);
        if (animation != null) {
            if (!saveAnimation(name, animation)) {
                return OperationResult.INTERNAL_ERROR;
            }
        } else {
            return OperationResult.NOT_FOUND;
        }
        return OperationResult.SUCCESS;
    }
    
    private static boolean saveAnimation(String name, Animation animation) {
        FileOutputStream fstream = null;
        ObjectOutputStream ostream = null;
        boolean result = true;
        try {
            File f = new File(PLUGIN_DIR, name + ".anim");
            Files.deleteIfExists(f.toPath());
            
            fstream = new FileOutputStream(f);
            ostream = new ObjectOutputStream(fstream);
            
            ostream.writeObject(animation);
        } catch (IOException ex) {
            Logger.getLogger(Animations.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        } finally {
            try {
                if (ostream != null) {
                    ostream.close();
                }
                if (fstream != null) {
                    fstream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Animations.class.getName()).log(Level.SEVERE, null, ex);
                result = false;
            }
        }
        return result;
    }
    
    public static boolean deleteAnimation(String name) {
        File f = new File(PLUGIN_DIR, name + ".anim");
        boolean retval = f.delete();
        if (retval) {
            animations.remove(name);
        }
        return retval;
    }
    
    public static Selection getSelection(Player p) {
        if (p == null) {
            return null;
        }
        
        Selection s = selections.get(p.getUniqueId());
        if (s == null) {
            selections.put(p.getUniqueId(), new Selection());
            s = selections.get(p.getUniqueId());
        }
        
        return s;
    }
    
    public static void reloadAnimation(String name) {
        File f = new File(PLUGIN_DIR, name + ".anim");
        
        FileInputStream fstream = null;
        ObjectInputStream ostream = null;
        try {
            fstream = new FileInputStream(f);
            ostream = new ObjectInputStream(fstream);

            Animation animation = (Animation) ostream.readObject();
            if (animation != null) {
                animations.put(name, animation);
                AnimationsPlugin.getPluginInstance().getLogger().info(Messages.INFO_ANIMATION_LOADED + name);
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Animations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ostream != null) {
                    ostream.close();
                }
                if (fstream != null) {
                    fstream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Animations.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void registerTask(AnimationTask task) {
        runningTasks.add(task);
    }
    
    public static AnimationTask retrieveTask(Animation animation) {
        for (AnimationTask task : runningTasks) {
            if (task.getAnimation() == animation) {
                return task;
            }
        }
        return null;
    }
    
    public static void deleteTask(AnimationTask task) {
        runningTasks.remove(task);
    }
    
    public static int countPages() {
        return Math.max(1, (animations.size() + PAGE_SIZE - 1)/PAGE_SIZE);
    }
    
    public static List<String> getPage(int page) {
        Set<String> animationSet = animations.keySet();
        List<String> list = new ArrayList<>(animationSet);
        Collections.sort(list);
        int from = (page - 1)*PAGE_SIZE;
        int to = page*PAGE_SIZE;
        if (from > list.size()) {
            from = list.size();
        }
        if (to > list.size()) {
            to = list.size();
        }
        return list.subList(from, to);
    }
    
}
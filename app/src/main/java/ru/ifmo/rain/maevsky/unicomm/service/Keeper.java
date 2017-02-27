package ru.ifmo.rain.maevsky.unicomm.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import dalvik.system.PathClassLoader;
import ru.ifmo.rain.maevsky.unicomm.plugins.Messenger;
import ru.ifmo.rain.maevsky.unicomm.plugins.Plugin;
import ru.ifmo.rain.maevsky.unicomm.plugins.PluginsStack;
import ru.ifmo.rain.maevsky.unicomm.plugins.Preprocessor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ru.ifmo.rain.maevsky.unicomm.utils.Constants.LOG_TAG;
import static ru.ifmo.rain.maevsky.unicomm.utils.Constants.PACK_DESCRIPTOR;

/**
 * Created by faerytea on 19.12.16.
 */
public class Keeper {
    public static final HashMap<String, PathClassLoader> classLoaders
            = new HashMap<>();
    static Keeper instance = null;
    public final HashMap<String, Messenger> messengers
            = new HashMap<>();
    public final HashMap<String, Preprocessor> preprocessors
            = new HashMap<>();
    public final List<PluginsStack> stacks;
    public final List<String> brokenStacks;
    private final File root;

    private Keeper(Context context) {
        root = context.getFilesDir();
        File packagesFile = new File(root, "packages");
        if (!packagesFile.exists())
            try {
                packagesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        List<String> packages = readLines(packagesFile);
        loadPlugins(packages, context.getClassLoader());
        File stacksListFile = new File(root, "stacks");
        List<String> stacksList = readLines(stacksListFile);
        List<String> brokenStacks = new ArrayList<>(stacksList.size());
        List<PluginsStack> stacks = new ArrayList<>(stacksList.size());
        for (String stackName : stacksList) {
            PluginsStack stack = loadStack(stackName);
            if (stack == null) {
                brokenStacks.add(stackName);
            } else {
                stacks.add(stack);
            }
        }
        this.stacks = stacks;
        this.brokenStacks = brokenStacks;
    }

    @SuppressLint("NewApi")
    private static List<String> readLines(File f) {
        String s;
        List<String> lst = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            while ((s = reader.readLine()) != null) {
                lst.add(s);
            }
        } catch (FileNotFoundException e) {
            Log.w(LOG_TAG, "File " + f.getName() + " doesn't exists");
        } catch (IOException e) {
            e.printStackTrace(); // rethrow ?
        }
        return lst;
    }

    @SuppressWarnings("unchecked")
    private static Pair<List<String>, List<String>> scan(
            String packageName,
            String classesPath,
            String libraryPath,
            ClassLoader parent) throws
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        PathClassLoader cl;
        synchronized (classLoaders) {
            cl = classLoaders.get(packageName);
            if (cl == null) {
                cl = new PathClassLoader(classesPath, libraryPath, parent);
                classLoaders.put(packageName, cl);
            }
        }
        Class<?> description = cl.loadClass(packageName + PACK_DESCRIPTOR);
        Method preprocessorsGetter = description.getMethod("getPreprocessors");
        Method messengersGetter = description.getMethod("getMessengers");
        List<String> preprocessors = (List<String>) preprocessorsGetter.invoke(null);
        List<String> messengers = (List<String>) messengersGetter.invoke(null);
        return new Pair<>(messengers, preprocessors);
    }

    public static synchronized Keeper getInstance(Context context) {
        return instance == null ? instance = new Keeper(context) : instance;
    }

    @SuppressLint("NewApi")
    public PluginsStack createStack(String stackName, String messenger, List<String> preprocessors) {
        File stackFile = new File(root, "stack_" + stackName.replace(' ', '_'));
        PluginsStack ps = buildPluginsStack(stackName, messenger, preprocessors);
        if (ps != null) {
            try (BufferedWriter w = new BufferedWriter(new FileWriter(stackFile))) {
                w.write(messenger);
                for (String p : preprocessors) {
                    w.newLine();
                    w.write(p);
                }
                stacks.add(ps);
                syncStacks();
            } catch (IOException e) {
                stackFile.delete();
                e.printStackTrace();
                return null;
            }
        }
        return ps;
    }

    @SuppressLint("NewApi")
    private void syncStacks() {
        File stacksFile = new File(root, "stacks");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(stacksFile))) {
            for (PluginsStack ps : stacks) {
                if (ps != null) {
                    writer.write(ps.getName());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            syncStacks();
        }
    }

    private PluginsStack loadStack(String stackName) {
        File stackFile = new File(root, "stack_" + stackName.replace(' ', '_'));
        List<String> pluginsInStack = readLines(stackFile);
        return buildPluginsStack(
                stackName,
                pluginsInStack.get(0),
                pluginsInStack.subList(1, pluginsInStack.size()));
    }

    private PluginsStack buildPluginsStack(String stackName, String messenger, List<String> preprocessors) {
        boolean fail;
        Messenger msg = messengers.get(messenger);
        fail = msg == null; // fixme ???
        Preprocessor[] pps = new Preprocessor[preprocessors.size()];
        for (int i = 0; i < preprocessors.size() && !fail; ++i) {
            pps[i] = this.preprocessors.get(preprocessors.get(i));
            if (pps[i] == null) fail = true;
            else pps[i] = pps[i].getInstance();
        }
        return fail ? null : new PluginsStack(stackName, msg.getInstance(), pps);
    }

    private void loadPlugins(List<String> packages, ClassLoader parent) {
        for (String line : packages) {
            String[] t = line.split(" ");
            Pair<List<String>, List<String>> classes;
            try {
                classes = scan(t[0], t[1], t[2], parent);
                ClassLoader loader;
                synchronized (classLoaders) {
                    loader = classLoaders.get(t[0]);
                }
                for (String mName : classes.first) {
                    Messenger messenger = loadClass(mName, Messenger.class, loader);
                    if (messenger != null)
                        messengers.put(mName, messenger);
                }
                for (String pName : classes.second) {
                    Preprocessor preprocessor = loadClass(pName, Preprocessor.class, loader);
                    if (preprocessor != null)
                        preprocessors.put(pName, preprocessor);
                }
            } catch (ClassNotFoundException e) {
                Log.v(LOG_TAG, e.getMessage()); // OK, package deleted
            } catch (NoSuchMethodException e) {
                Log.wtf(LOG_TAG, e);
            } catch (InvocationTargetException e) {
                Log.wtf(LOG_TAG, e);
            } catch (IllegalAccessException e) {
                Log.wtf(LOG_TAG, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends Plugin> T loadClass(
            @NonNull final String name,
            @NonNull final Class<T> clazz,
            @NonNull final ClassLoader loader) {
        Class pluginClass = null;
        try {
            pluginClass = loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, "Cannot load plugin " + name);
            return null;
        }
        if (clazz.isAssignableFrom(pluginClass)) {
            try {
                return (T) pluginClass.newInstance();
            } catch (Exception e) { // IllegalAccessException | InstantiationException
                Log.e(LOG_TAG, name + " cannot be instantiated");
            }
        } else {
            Log.e(LOG_TAG, name + " is not a valid " + clazz.getSimpleName());
        }
        return null;
    }
}

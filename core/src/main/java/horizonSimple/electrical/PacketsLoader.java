package horizonSimple.electrical;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import horizonSimple.electrical.packet.ElementsPacket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class PacketsLoader extends SynchronousAssetLoader<ElementsPacket, AssetLoaderParameters<ElementsPacket>> {
    public PacketsLoader(FileHandleResolver resolver) {
        super(fileName -> {
            FileHandle handle = Gdx.files.internal(fileName);
            if (handle.exists()) return handle;
            return Gdx.files.local("packets/");
        });
    }

    @Override
    public ElementsPacket load(AssetManager assetManager, String fileName, FileHandle file, AssetLoaderParameters<ElementsPacket> parameter) {
        URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            return (ElementsPacket)method.invoke(loader, file.file().toURI().toURL());
        } catch (IllegalAccessException | InvocationTargetException | MalformedURLException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Array getDependencies(String fileName, FileHandle file, AssetLoaderParameters parameter) {
        return null;
    }
}

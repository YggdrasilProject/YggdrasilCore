package ru.linachan.yggdrasil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.Semaphore;

@SuppressWarnings("unchecked")
public abstract class YggdrasilGenericManager<T> {

    protected YggdrasilCore core;
    protected Map<Class<? extends T>, T> managedObjects = new HashMap<>();
    private Semaphore writeLock = new Semaphore(1);

    protected static Logger logger = LoggerFactory.getLogger(YggdrasilGenericManager.class);

    public void setUpManager(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;

        onInit();

        core.getEventSystem().registerListener(event -> {
            switch (event.getEventType()) {
                case "packageEnabled":
                    onPackageEnabled((String) event.getEventData().get("packageName"));
                    break;
                case "packageDisabled":
                    onPackageDisabled((String) event.getEventData().get("packageName"));
                    break;
                default:
                    break;
            }
        });

    }

    protected Class<T> getGenericTypeClass() {
        return ((Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAbstract(Class<? extends T> targetClass) {
        return targetClass != null && Modifier.isAbstract(targetClass.getModifiers());
    }

    protected void discoverAll() {
        core.discoverAll(getGenericTypeClass()).stream()
            .filter(discoveredObject -> !managedObjects.containsKey(discoveredObject) && !isAbstract(discoveredObject))
            .forEach(discoveredObject -> {
                lock();
                managedObjects.put(discoveredObject, null);
                unLock();

                onDiscover(discoveredObject);
            });
    }

    protected void discoverEnabled() {
        core.discoverEnabled(getGenericTypeClass()).stream()
            .filter(discoveredObject -> !managedObjects.containsKey(discoveredObject) && !isAbstract(discoveredObject))
            .forEach(discoveredObject -> {
                lock();
                managedObjects.put(discoveredObject, null);
                unLock();

                onDiscover(discoveredObject);
            });
    }

    protected void cleanup() {
        List<Class<? extends T>> objectsToRemove = new ArrayList<>();
        managedObjects.keySet().stream()
            .filter(object -> !core.isPackageEnabled(object.getPackage().getName().split("\\.")[2]))
            .forEach(object -> {
                onCleanup(object);

                objectsToRemove.add(object);
            });

        lock();
        for (Class<? extends T> objectToRemove: objectsToRemove) {
            managedObjects.remove(objectToRemove);
        }
        unLock();
    }

    public Map<Class<? extends T>, Boolean> list() {
        Map<Class<? extends T>, Boolean> objectList = new HashMap<>();

        for (Map.Entry<Class<? extends T>, T> object: managedObjects.entrySet()) {
            objectList.put(object.getKey(), object.getValue() != null);
        }

        return objectList;
    }

    public boolean isEnabled(Class<? extends T> objectClass) {
        return managedObjects.containsKey(objectClass)&&(managedObjects.get(objectClass)!=null);
    }

    public <V extends T> V get(Class<V> objectClass) {
        if (isEnabled(objectClass)) {
            return objectClass.cast(managedObjects.get(objectClass));
        }
        return null;
    }

    public Class<? extends T> getClassByName(String objectClassName) {
        Optional<Class<? extends T>> objectClassOptional =  managedObjects.keySet().stream()
            .filter(objectClass -> objectClass.getSimpleName().equals(objectClassName))
            .findFirst();

        if (objectClassOptional.isPresent()) {
            return objectClassOptional.get();
        }

        return null;
    }

    protected void put(Class<? extends T> objectClass, T objectInstance) {
        lock();
        managedObjects.put(objectClass, objectInstance);
        unLock();
    }

    public void enable(Class<? extends T> objectClass) {
        if (!isEnabled(objectClass)) {
            String packageName = objectClass.getPackage().getName().split("\\.")[2];
            core.enablePackage(packageName);
            onEnable(objectClass);
        }
    }

    public void disable(Class<? extends T> objectClass) {
        if (isEnabled(objectClass)) {
            String packageName = objectClass.getPackage().getName().split("\\.")[2];
            core.disablePackage(packageName);
            onDisable(objectClass);
        }
    }

    protected void lock() {
        try {
            writeLock.acquire();
        } catch (InterruptedException ignored) {}
    }

    protected void unLock() {
        writeLock.release();
    }

    protected abstract void onInit();

    protected abstract void onDiscover(Class<? extends T> discoveredObject);

    protected abstract void onCleanup(Class<? extends T> managedObject);

    protected abstract void onEnable(Class<? extends T> enabledObject);

    protected abstract void onDisable(Class<? extends T> disabledObject);

    protected abstract void onPackageEnabled(String packageName);

    protected abstract void onPackageDisabled(String packageName);

    public abstract void shutdown();
}

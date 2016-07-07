package ru.linachan.virt;

import ru.linachan.virt.schema.VirtTemplate;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.helpers.Plugin;
import ru.linachan.yggdrasil.storage.YggdrasilStorageFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Plugin(name = "VMManager", description = "Provides ability to manage libvirt instances.")
public class VirtPlugin extends YggdrasilPlugin {

    private YggdrasilStorageFile templateStorage;

    @Override
    protected void onInit() {
        try {
            templateStorage = core.getStorage()
                .createStorage(
                    "virtTemplateStorage",
                    new File("virtTemplates.dat"),
                    "virtTemplateStor".getBytes(),
                    false
                );
        } catch (IOException e) {
            logger.error("Unable to initialize template storage: {}", e.getMessage());
        }
    }

    public VirtTemplate getTemplate(String templateName) throws IOException {
        try {
            return templateStorage.getObject(templateName, VirtTemplate.class);
        } catch (ClassNotFoundException e) {
            logger.warn("Unable to retrieve template: {}", e.getMessage());
            return null;
        }
    }

    public void putTemplate(String templateName, VirtTemplate template) throws IOException {
        templateStorage.putObject(templateName, template);
    }

    public Set<String> listTemplates() {
        return templateStorage.listKeys();
    }

    @Override
    protected void onShutdown() {
        try {
            templateStorage.writeStorage();
        } catch (InterruptedException | IOException e) {
            logger.error("Unable to save templates to storage: {}", e.getMessage());
        }
    }
}

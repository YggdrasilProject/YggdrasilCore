package ru.linachan.virt;

import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.ConstructorException;
import ru.linachan.virt.schema.VirtTemplate;
import ru.linachan.yggdrasil.common.console.ANSIUtils;
import ru.linachan.yggdrasil.common.console.ConsoleColor;
import ru.linachan.yggdrasil.common.console.ConsoleTextStyle;
import ru.linachan.yggdrasil.plugin.YggdrasilPluginManager;
import ru.linachan.yggdrasil.shell.YggdrasilShellCommand;
import ru.linachan.yggdrasil.shell.helpers.CommandAction;
import ru.linachan.yggdrasil.shell.helpers.ShellCommand;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ShellCommand(command = "virt", description = "Manage libvirt instances.")
public class VirtCommand extends YggdrasilShellCommand {

    private VirtPlugin virt;

    @Override
    protected void init() throws IOException {
        virt = core.getManager(YggdrasilPluginManager.class).get(VirtPlugin.class);
    }

    @CommandAction("Add instance template")
    public void add_template() throws IOException {
        if (args.size() > 0) {
            String templateName = args.get(0);
            console.writeLine("Reading template from stdin. Write 2 empty lines to finish input.");

            StringBuilder template = new StringBuilder();
            boolean newLine = false;

            while (true) {
                String templateLine = console.read();
                if (templateLine.isEmpty()) {
                    if (newLine) {
                        break;
                    } else {
                        newLine = true;
                    }
                } else {
                    newLine = false;
                    template.append(templateLine).append("\r\n");
                }
            }

            try {
                Yaml templateReader = new Yaml(new Constructor(VirtTemplate.class));
                VirtTemplate templateData = (VirtTemplate) templateReader.load(template.toString());
                virt.putTemplate(templateName, templateData);
            } catch (ConstructorException e) {
                console.writeLine("Unable to parse template: {}", e.getMessage());
                exit(1);
            }
        } else {
            console.write(ANSIUtils.RenderString("No template name provided", ConsoleColor.BRIGHT_RED, ConsoleTextStyle.BOLD));
        }
    }

    @CommandAction("List instance templates")
    public void list_templates() throws IOException {
        List<Map<String, String>> templates = new ArrayList<>();

        for (String templateName: virt.listTemplates()) {
            Map<String, String> templateInfo = new HashMap<>();
            VirtTemplate templateData = virt.getTemplate(templateName);

            templateInfo.put("name", templateName);

            templateInfo.put("memory", String.valueOf(templateData.memory));
            templateInfo.put("vcpu", String.valueOf(templateData.vcpu));
            templateInfo.put("disk_size", String.valueOf(templateData.disk_size));
            templateInfo.put("image", templateData.image);

            templates.add(templateInfo);
        }

        List<String> templateFields = new ArrayList<>();

        templateFields.add("name");
        templateFields.add("memory");
        templateFields.add("vcpu");
        templateFields.add("disk_size");
        templateFields.add("image");

        console.writeTable(templates, templateFields);
    }

    @CommandAction("Delete instance template")
    public void delete_template() throws IOException {

    }

    @CommandAction("Show instance template")
    public void show_template() throws IOException {

    }

    @CommandAction("Dump instance template to YAML")
    public void dump_template() throws IOException {

    }

    @CommandAction("Render instance template to XML")
    public void render_template() throws IOException {
        VirtTemplate templateData = virt.getTemplate(args.get(0));

        try {
            VirtXMLTemplateBuilder templateBuilder = new VirtXMLTemplateBuilder(templateData, args.get(1));
            console.writeLine(templateBuilder.toString());
        } catch (SAXException | XPathExpressionException | ParserConfigurationException e) {
            console.writeException(e);
        }
    }

    @Override
    protected void onInterrupt() {

    }
}

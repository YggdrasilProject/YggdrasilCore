package ru.linachan.yggdrasil.common.console;

import com.google.common.base.Joiner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsoleUtils {

    private InputStreamReader inputStream;
    private OutputStreamWriter outputStream;
    private OutputStreamWriter errorStream;

    private InterruptHandler interruptHandler;

    private ConsoleColor textColor = null;
    private ConsoleColor bgColor = null;
    private ConsoleTextStyle textStyle = null;
    private boolean isBright = false;

    private List<String> autoCompleteList = new ArrayList<>();

    private List<String> commandHistory = new ArrayList<>();
    private Integer commandHistoryID = -1;
    private String lastCommand = null;

    public ConsoleUtils(InputStreamReader in, OutputStreamWriter out, OutputStreamWriter err) {
        inputStream = in;
        outputStream = out;
        errorStream = err;
    }

    public ConsoleUtils(InputStream in, OutputStream out, OutputStream err) {
        inputStream = new InputStreamReader(in);
        outputStream = new OutputStreamWriter(out);
        errorStream = new OutputStreamWriter(err);
    }

    public void addCompletion(String completion) {
        autoCompleteList.add(completion);
    }

    public void addCompletions(List<String> completions) {
        autoCompleteList.addAll(completions);
    }

    public void addHistoryItem(String historyRecord) {
        commandHistory.add(historyRecord);
        commandHistoryID = -1;
    }

    public void setInterruptHandler(InterruptHandler handler) {
        interruptHandler = handler;
    }

    private List<String> autoComplete(String inputString) {
        return autoCompleteList.stream()
            .filter(completeName -> completeName.startsWith(inputString))
            .collect(Collectors.toList());
    }

    public String readLine() throws IOException {
        return (new BufferedReader(inputStream)).readLine();
    }

    public String read(String query) throws IOException {
        return read(query, false);
    }

    public String readPassword(String query) throws IOException {
        return read(query, true);
    }

    public boolean readYesNo(String query) throws IOException {
        while (true) {
            String result = read(String.format(
                "%1$s (y/N): ", query
            ), false);

            switch (result.toLowerCase()) {
                case "y":
                case "yes":
                    return true;
                case "n":
                case "no":
                    return false;
            }
        }
    }

    private String read(String query, boolean secret) throws IOException {
        StringBuilder inputData = new StringBuilder();
        char[] charBuffer = new char[1];

        boolean inputInProgress = true;
        int cursorPosition = 0;

        write(query);

        while (inputInProgress) {
            inputStream.read(charBuffer);
            switch (charBuffer[0]) {
                case '\r':
                case '\n':
                    inputInProgress = false;
                    write("\r\n");
                    break;
                case '\t':
                    List<String> autoCompleteList = autoComplete(inputData.toString());
                    if (autoCompleteList.size() == 1) {
                        delete(cursorPosition);
                        inputData = new StringBuilder(autoCompleteList.get(0));
                        cursorPosition = inputData.length();
                        write(inputData.toString());
                    } else if (autoCompleteList.size() > 1) {
                        write("\r\n");
                        for (String commandName: autoCompleteList) {
                            write(String.format("\t%1$s\r\n", commandName));
                        }
                        write(query);
                        write(inputData.toString());
                    }
                    break;
                case '\b':
                case (char)0x7F:
                    if (inputData.length() > 0) {
                        delete(cursorPosition);
                        inputData.deleteCharAt(cursorPosition - 1);
                        cursorPosition--;
                        write(inputData.toString());
                    }
                    break;
                case (char)0x03:
                    if (interruptHandler != null) interruptHandler.onETXEvent();
                    inputInProgress = false;
                    write("\r\n");
                    break;
                case (char)0x04:
                    if (interruptHandler != null) interruptHandler.onEOTEvent();
                    inputInProgress = false;
                    write("\r\n");
                    break;
                case (char)0x1A:
                    if (interruptHandler != null) interruptHandler.onSUBEvent();
                    inputInProgress = false;
                    write("\r\n");
                    break;
                case (char)0x1B:
                    char[] subCharBuffer = new char[2];
                    inputStream.read(subCharBuffer);
                    if (subCharBuffer[0] == (char)0x5B) {
                        switch (subCharBuffer[1]) {
                            case (char)0x44: // Left Arrow
                                if (cursorPosition > 0) {
                                    cursorPosition--;
                                    moveCarriage(-1);
                                }
                                break;
                            case (char)0x43: // Right Arrow
                                if (cursorPosition < inputData.length()) {
                                    cursorPosition++;
                                    moveCarriage(1);
                                }
                                break;
                            case (char)0x42: // Down Arrow
                                if (commandHistoryID != -1) {
                                    if (commandHistoryID < commandHistory.size() - 1) {
                                        commandHistoryID++;
                                        inputData = new StringBuilder(commandHistory.get(commandHistoryID));
                                    } else {
                                        inputData = new StringBuilder(lastCommand);
                                        commandHistoryID = -1;
                                        lastCommand = null;
                                    }
                                }

                                delete(cursorPosition);
                                write(inputData.toString());
                                cursorPosition = inputData.length();
                                break;
                            case (char)0x41: // Up Arrow
                                if (commandHistoryID == -1) {
                                    lastCommand = inputData.toString();
                                    commandHistoryID = commandHistory.size();
                                }

                                if (commandHistoryID > 0) {
                                    commandHistoryID--;
                                    inputData = new StringBuilder(commandHistory.get(commandHistoryID));

                                    delete(cursorPosition);
                                    write(inputData.toString());
                                    cursorPosition = inputData.length();
                                }
                                break;
                        }
                    }
                    break;
                default:
                    delete(cursorPosition);
                    inputData.insert(cursorPosition, (secret) ? new char[] {'*'} : charBuffer);
                    cursorPosition++;
                    write(inputData.toString());
                    moveCarriage(cursorPosition - inputData.length());
                    break;
            }
        }

        return inputData.toString();
    }

    private void delete(int positions) throws IOException {
        for(int position = 0; position < positions; position++)
            write("\b \b");
    }

    private void moveCarriage(int positions) throws IOException {
        for (int position = 0; position < Math.abs(positions); position++) {
            write(String.valueOf((positions > 0) ? new char[]{0x1B, 0x5B, 0x43} : new char[]{0x1B, 0x5B, 0x44}));
        }
    }

    private String generateBorder(List<Integer> fields) {
        List<String> borders = new ArrayList<>();

        for (Integer field: fields) {
            String border = "";
            for (int i = 0; i < field + 2; i++) {
                border += "-";
            }
            borders.add(border);
        }

        return "+" + Joiner.on("+").join(borders) + "+";
    }

    public void setTextColor(ConsoleColor color) {
        textColor = color;
    }

    public void setBGColor(ConsoleColor color) {
        bgColor = color;
    }

    public void setBright(boolean bright) {
        isBright = bright;
    }

    public void writeMap(Map<?, ?> mapObject) throws IOException {
        writeMap(mapObject, null, null);
    }

    public void writeMap(Map<?, ?> mapObject, String keyHeader, String valueHeader) throws IOException {
        final int[] maxKeyLength = { (keyHeader != null) ? keyHeader.length() : 0 };
        final int[] maxValueLength = { (valueHeader != null) ? valueHeader.length() : 0 };

        if (mapObject != null) {
            mapObject.keySet().stream()
                .filter(key -> key != null)
                .filter(key -> String.valueOf(key).length() > maxKeyLength[0])
                .forEach(key -> maxKeyLength[0] = String.valueOf(key).length());

            mapObject.values().stream()
                .filter(value -> value != null)
                .filter(value -> String.valueOf(value).length() > maxValueLength[0])
                .forEach(value -> maxValueLength[0] = String.valueOf(value).length());

            List<Integer> fields = new ArrayList<>();

            fields.add(maxKeyLength[0]);
            fields.add(maxValueLength[0]);

            String formatString = String.format("| %%-%ds | %%-%ds |", maxKeyLength[0], maxValueLength[0]);
            String borderString = generateBorder(fields);

            writeLine(borderString);

            if ((keyHeader != null) && (valueHeader != null)) {
                writeLine(formatString, keyHeader, valueHeader);
                writeLine(borderString);
            }

            for (Object key: mapObject.keySet()) {
                writeLine(formatString, key, mapObject.get(key));
            }

            writeLine(borderString);
        }
    }

    public void writeTable(List<Map<String, String>> table, List<String> headers) throws IOException {
        if ((table != null)&&(headers != null)) {
            Map<String, Integer> fieldMap = new HashMap<>();
            headers.stream().forEach(header -> {
                fieldMap.put(header, header.length());
                table.stream()
                    .filter(row -> row.containsKey(header))
                    .filter(row -> String.valueOf(row.get(header)).length() > fieldMap.get(header))
                    .forEach(row -> fieldMap.replace(header, String.valueOf(row.get(header)).length()));
            });

            List<Integer> fields = new ArrayList<>();
            headers.stream().forEach(header -> fields.add(fieldMap.get(header)));

            String formatString = "| %-" + Joiner.on("s | %-").join(fields) + "s |";
            String borderString = generateBorder(fields);

            writeLine(borderString);
            writeLine(formatString, headers.toArray());
            writeLine(borderString);

            for(Map<String, String> row: table) {
                List<String> rowData = new ArrayList<>();
                headers.stream().forEach(header -> rowData.add(row.getOrDefault(header, "")));
                writeLine(formatString, rowData.toArray());
            }

            writeLine(borderString);
        }
    }


    public void writeLine(String format, Object... args) throws IOException {
        write(format + "\r\n", args);
    }

    public void write(String format, Object... args) throws IOException {
        outputStream.write(
            String.format(
                String.format(
                    "\033[%d;%d;%dm%s\033[0m",
                    (textStyle != null) ? textStyle.ordinal() : 0,
                    (bgColor != null) ? bgColor.ordinal() + ((isBright) ? 100 : 40) : 49,
                    (textColor != null) ? textColor.ordinal() + ((isBright) ? 90 : 30) : 39,
                    format
                ), args
            )
        );
        outputStream.flush();
    }

    public String format(String format, Object... args) {
        return String.format(
            String.format(
                "\033[%d;%d;%dm%s\033[0m",
                (textStyle != null) ? textStyle.ordinal() : 0,
                (bgColor != null) ? bgColor.ordinal() + ((isBright) ? 100 : 40) : 49,
                (textColor != null) ? textColor.ordinal() + ((isBright) ? 90 : 30) : 39,
                format
            ), args
        );
    }

    public String format(String format, ConsoleColor textColor, ConsoleColor bgColor, ConsoleTextStyle textStyle, boolean isBright) {
        return String.format(
            "\033[%d;%d;%dm%s\033[0m",
            (textStyle != null) ? textStyle.ordinal() : 0,
            (bgColor != null) ? bgColor.ordinal() + ((isBright) ? 100 : 40) : 49,
            (textColor != null) ? textColor.ordinal() + ((isBright) ? 90 : 30) : 39,
            format
        );
    }

    public void error(String format, Object... args) throws IOException {
        errorStream.write(
            String.format(
                String.format(
                    "\033[%d;%d;%dm%s\033[0m",
                    (textStyle != null) ? textStyle.ordinal() : 0,
                    (bgColor != null) ? bgColor.ordinal() + ((isBright) ? 100 : 40) : 49,
                    (textColor != null) ? textColor.ordinal() + ((isBright) ? 90 : 30) : 39,
                    format
                ), args
            )
        );
        errorStream.flush();
    }
}
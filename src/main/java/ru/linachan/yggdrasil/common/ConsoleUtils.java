package ru.linachan.yggdrasil.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

    public void setInterruptHandler(InterruptHandler handler) {
        interruptHandler = handler;
    }

    private List<String> autoComplete(String inputString) {
        return autoCompleteList.stream()
            .filter(completeName -> completeName.startsWith(inputString))
            .collect(Collectors.toList());
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
                            case (char)0x44:
                                if (cursorPosition > 0) {
                                    cursorPosition--;
                                    moveCarriage(-1);
                                }
                                break;
                            case (char)0x43:
                                if (cursorPosition < inputData.length()) {
                                    cursorPosition++;
                                    moveCarriage(1);
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

    public void setTextColor(ConsoleColor color) {
        textColor = color;
    }

    public void setBGColor(ConsoleColor color) {
        bgColor = color;
    }

    public void setBright(boolean bright) {
        isBright = bright;
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

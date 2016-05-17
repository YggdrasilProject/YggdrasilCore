package ru.linachan.cheat;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;

import java.util.ArrayList;
import java.util.List;


public class CheatPlugin extends YggdrasilPlugin {

    static Kernel32 kernel32 = Kernel32.INSTANCE;

    private CheatProcess attachedProcess = null;

    @Override
    protected void onInit() {

    }

    @Override
    protected void onShutdown() {

    }

    public List<CheatProcess> getProcessesByName(String processName) {
        List<CheatProcess> processes = new ArrayList<>();

        WinNT.HANDLE snapshot = null;

        try {
            snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
            Tlhelp32.PROCESSENTRY32 entry = new Tlhelp32.PROCESSENTRY32();
            kernel32.Process32First(snapshot, entry);

            do {
                String processEXEName = Native.toString(entry.szExeFile);
                if (processEXEName.equals(processName)) {
                    processes.add(new CheatProcess(Native.toString(entry.szExeFile), entry.th32ProcessID.intValue()));
                }
            } while(kernel32.Process32Next(snapshot, entry));
        } finally {
            kernel32.CloseHandle(snapshot);
        }

        return processes;
    }

    public List<CheatProcess> getAllProcesses() throws LastErrorException {
        List<CheatProcess> processes = new ArrayList<>();
        WinNT.HANDLE snapshot = null;

        try {
            snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
            Tlhelp32.PROCESSENTRY32 entry = new Tlhelp32.PROCESSENTRY32();
            kernel32.Process32First(snapshot, entry);

            do {
                processes.add(new CheatProcess(Native.toString(entry.szExeFile), entry.th32ProcessID.intValue()));
            } while(kernel32.Process32Next(snapshot, entry));
        } finally {
            kernel32.CloseHandle(snapshot);
        }

        return processes;
    }

    public void attachProcess(CheatProcess process) {
        process.openProcess(
            CheatProcess.PROCESS_VM_READ | CheatProcess.PROCESS_VM_WRITE | CheatProcess.PROCESS_VM_OPERATIONS | CheatProcess.PROCESS_QUERY_INFO
        );

        attachedProcess = process;
    }

    public void detachProcess() {
        attachedProcess = null;
    }

    public CheatProcess getAttachedProcess() {
        return attachedProcess;
    }

    public boolean isAttached() {
        return attachedProcess != null;
    }
}

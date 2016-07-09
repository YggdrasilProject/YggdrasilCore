package ru.linachan.mmo.auth.user;

import java.util.Arrays;

public class Token {

    private byte[] token;

    public Token(byte[] tokenBytes) {
        token = tokenBytes;
    }

    public byte[] getBytes() {
        return token;
    }

    @Override
    public boolean equals(Object target){
        if (target == null) return false;
        if (target == this) return true;
        if (!(target instanceof Token)) return false;

        byte[] targetToken = ((Token) target).getBytes();
        return Arrays.equals(token, targetToken);
    }
}

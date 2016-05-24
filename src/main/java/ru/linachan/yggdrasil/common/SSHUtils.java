package ru.linachan.yggdrasil.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.security.KeyFactory.getInstance;

public class SSHUtils {

    private static BigInteger readAsnInteger(DataInputStream in) throws IOException {
        checkArgument(in.read() == 2, "no INTEGER marker");

        int length = in.read();
        if (length >= 0x80) {
            byte[] extended = new byte[length & 0x7f];
            in.readFully(extended);
            length = new BigInteger(extended).intValue();
        }

        byte[] data = new byte[length];
        in.readFully(data);

        return new BigInteger(data);
    }

    public static PrivateKey readPrivateKey(byte[] bytes) throws GeneralSecurityException, IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            checkArgument(in.read() == 48, "no id_rsa SEQUENCE");
            checkArgument(in.read() == 130, "no Version marker");
            in.skipBytes(5);

            BigInteger n = readAsnInteger(in);
            readAsnInteger(in);
            BigInteger e = readAsnInteger(in);

            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(n, e);
            return getInstance("RSA").generatePrivate(spec);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static PublicKey readPublicKey(byte[] bytes) throws GeneralSecurityException, IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            byte[] sshRsa = new byte[in.readInt()];
            in.readFully(sshRsa);

            checkArgument(new String(sshRsa).equals("ssh-rsa"), "no RFC-4716 ssh-rsa");

            byte[] exp = new byte[in.readInt()];
            in.readFully(exp);

            byte[] mod = new byte[in.readInt()];
            in.readFully(mod);

            BigInteger e = new BigInteger(exp);
            BigInteger n = new BigInteger(mod);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
            return getInstance("RSA").generatePublic(spec);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

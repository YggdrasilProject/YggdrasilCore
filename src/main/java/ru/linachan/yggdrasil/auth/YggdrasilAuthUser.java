package ru.linachan.yggdrasil.auth;

import ru.linachan.yggdrasil.common.SSHUtils;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.*;

public class YggdrasilAuthUser implements Serializable {

    private String userName;
    private Map<String, Object> attributes;
    public static final long serialVersionUID = 1L;

    public YggdrasilAuthUser(String authUserName) {
        userName = authUserName;
        attributes = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public Object getAttribute(String attribute) {
        return attributes.containsKey(attribute) ? attributes.get(attribute) : null;
    }

    public void setAttribute(String attribute, Object value) {
        attributes.put(attribute, value);
    }

    public Set<String> listAttributes() {
        return attributes.keySet();
    }

    @SuppressWarnings("unchecked")
    public void addPublicKey(String publicKeyString) throws GeneralSecurityException, IOException {
        String[] rawPublicKey = publicKeyString.split(" ");

        PublicKey publicKey = SSHUtils.readPublicKey(Base64.getDecoder().decode(rawPublicKey[1]));

        List<String> authorizedKeys = (List<String>) getAttribute("publicKey");
        authorizedKeys = (authorizedKeys != null) ? authorizedKeys : new ArrayList<>();

        String publicKeyData = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
        if (!authorizedKeys.contains(publicKeyData)) {
            authorizedKeys.add(publicKeyData);
        }

        setAttribute("publicKey", authorizedKeys);
    }

    @SuppressWarnings("unchecked")
    public void deletePublicKey(int keyIndex) {
        List<String> authorizedKeys = (List<String>) getAttribute("publicKey");
        authorizedKeys = (authorizedKeys != null) ? authorizedKeys : new ArrayList<>();

        authorizedKeys.remove(keyIndex - 1);

        setAttribute("publicKey", authorizedKeys);
    }
}

/*
 * Copyright 2026 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.butterfly_format;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Patrik Karlström
 */
public class CryptoHelper {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTE = 12;
    private static final int TAG_LENGTH_BIT = 128;

    public static String decrypt(String encryptedBase64, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        var secretKey = buildKey(password);
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);

        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTE);
        byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH_BYTE, combined.length);

        var cipher = Cipher.getInstance(ALGORITHM);
        var gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static String encrypt(String plainText, String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        var secretKey = buildKey(password);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        new SecureRandom().nextBytes(iv);

        var cipher = Cipher.getInstance(ALGORITHM);
        var gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] cipherText = cipher.doFinal(Objects.toString(plainText, "").getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + cipherText.length];

        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String rot13(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                c = (char) (((c - 'a' + 13) % 26) + 'a');
            } else if (c >= 'A' && c <= 'Z') {
                c = (char) (((c - 'A' + 13) % 26) + 'A');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static SecretKey buildKey(String password) throws NoSuchAlgorithmException {
        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);
        var sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes);

        return new SecretKeySpec(Arrays.copyOf(keyBytes, 16), "AES");
    }
}

package com.github.f9c.message.encryption;

import com.github.f9c.message.ByteBufferHelper;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import static com.github.f9c.message.ByteBufferHelper.encodedSize;

public class Crypt {
    public static final String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    public static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String AES_KEY_FORMAT = "AES";
    private static final SecureRandom random = new SecureRandom();
    public static final int AES_KEY_SIZE = 256;
    public static final int AES_IV_SIZE = 16;
    public static final int MAX_SIZE = 100000;

    public static PublicKey decodeKey(byte[] keyData) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyData));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt(PublicKey key, byte[] data) throws GeneralSecurityException {
        SecretKey messageKey = generateMessageKey();

        byte[] encryptedKey = encryptMessageKey(key, messageKey);
        byte[] iv = createAesInitialVector();
        byte[] encryptedMessage = aesEncrypt(data, messageKey, iv);
        byte[] encryptedData = new byte[encodedSize(encryptedKey) + encodedSize(iv) + encodedSize(encryptedMessage)];

        ByteBuffer bb = ByteBuffer.wrap(encryptedData);

        ByteBufferHelper.put(encryptedKey, bb);
        ByteBufferHelper.put(iv, bb);
        ByteBufferHelper.put(encryptedMessage, bb);

        return encryptedData;
    }

    private static byte[] createAesInitialVector() {
        byte[] iv = new byte[AES_IV_SIZE];
        random.nextBytes(iv);
        return iv;
    }

    private static byte[] aesEncrypt(byte[] data, SecretKey messageKey, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher aesInstance = Cipher.getInstance(AES_ALGORITHM);
        aesInstance.init(Cipher.ENCRYPT_MODE, messageKey, ivSpec);
        return aesInstance.doFinal(data);
    }

    private static SecretKey generateMessageKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance(AES_KEY_FORMAT);
        kgen.init(AES_KEY_SIZE);
        return kgen.generateKey();
    }

    private static byte[] encryptMessageKey(PublicKey key, SecretKey messageKey) throws GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        return rsaCipher.doFinal(messageKey.getEncoded());
    }

    public static byte[] decrypt(PrivateKey key, byte[] encryptedData) throws GeneralSecurityException {
        ByteBuffer bb = ByteBuffer.wrap(encryptedData);

        byte[] encryptedKey = ByteBufferHelper.get(256, bb);
        byte[] iv = ByteBufferHelper.get(AES_IV_SIZE, bb);
        byte[] encryptedMessage = ByteBufferHelper.get(MAX_SIZE, bb);

        byte[] aesKey = decryptMessageKey(key, encryptedKey);

        return aesDecrypt(iv, encryptedMessage, aesKey);
    }

    private static byte[] aesDecrypt(byte[] iv, byte[] encryptedMessage, byte[] aesKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec messageKey = new SecretKeySpec(aesKey, AES_KEY_FORMAT);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher ci = Cipher.getInstance(AES_ALGORITHM);
        ci.init(Cipher.DECRYPT_MODE, messageKey, ivSpec);

        return ci.doFinal(encryptedMessage);
    }

    private static byte[] decryptMessageKey(PrivateKey key, byte[] encryptedKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        cipher.update(encryptedKey);

        return cipher.doFinal();
    }

    public static void verifySignature(PublicKey publicKey, ByteBuffer byteBuffer, byte[] signatureBytes) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(byteBuffer);
            if (!signature.verify(signatureBytes)) {
                throw new IllegalStateException("Signature validation failed.");
            }
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sign(PrivateKey privateKey, byte[] data) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}

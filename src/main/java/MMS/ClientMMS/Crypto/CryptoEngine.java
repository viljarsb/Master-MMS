package MMS.ClientMMS.Crypto;


import MMS.ClientMMS.Exceptions.DecryptionException;
import MMS.ClientMMS.Exceptions.EncryptionException;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;


/**
 * CryptoEngine is a utility class that provides cryptographic functionalities such as encryption,
 * decryption, key agreement, and signature generation and verification using various cryptographic
 * algorithms. Specifically, it uses the Bouncy Castle library for the following:
 * <ul>
 *   <li>AES encryption and decryption in CBC mode with PKCS7 padding</li>
 *   <li>ECDH key agreement for generating shared secrets</li>
 *   <li>ECDSA for signing and verifying signatures</li>
 *   <li>SHA-512 for hashing during the signature and key derivation processes</li>
 *   <li>HKDF for deriving encryption and decryption parameters (key and IV) from a shared secret</li>
 * </ul>
 * <p>
 * This class is designed to be used within its package only and all its methods and the constructor
 * are package-private.
 * </p>
 */
public class CryptoEngine
{
    private CryptoEngine()
    {
    }


    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }


    public static byte[] encryptMessage(ECPrivateKey privateKey, ECPublicKey publicKey, byte[] message) throws EncryptionException
    {
        try
        {
            // Generate a shared secret using ECDH key agreement
            byte[] sharedSecret = generateSecret(privateKey, publicKey);

            // Derive encryption parameters (key and IV) from the shared secret
            Pair<byte[], byte[]> encryptionParameters = deriveParameters(sharedSecret);
            byte[] key = encryptionParameters.getLeft();
            byte[] iv = encryptionParameters.getRight();

            // Encrypt the message using AES in CBC mode with PKCS7 padding
            return encrypt(key, iv, message);
        }

        catch (Exception e)
        {
            throw new EncryptionException("Error encrypting message", e);
        }
    }


    public static byte[] decryptMessage(ECPrivateKey privateKey, ECPublicKey publicKey, byte[] message) throws DecryptionException
    {
        try
        {
            // Generate a shared secret using ECDH key agreement
            byte[] sharedSecret = generateSecret(privateKey, publicKey);

            // Derive encryption parameters (key and IV) from the shared secret
            Pair<byte[], byte[]> encryptionParameters = deriveParameters(sharedSecret);
            byte[] key = encryptionParameters.getLeft();
            byte[] iv = encryptionParameters.getRight();

            // Decrypt the message using AES in CBC mode with PKCS7 padding
            return decrypt(key, iv, message);
        }

        catch (Exception ex)
        {
            throw new DecryptionException("Error decrypting message", ex);
        }
    }


    /**
     * Encrypts the given data using AES in CBC mode with PKCS7 padding.
     *
     * @param key  The encryption key as a byte array (256 bits).
     * @param iv   The initialization vector as a byte array (must be 128 bits).
     * @param data The byte array to encrypt.
     * @return The encrypted data as a byte array.
     * @throws NoSuchPaddingException             If the specified padding is not available.
     * @throws NoSuchAlgorithmException           If the specified algorithm is not available.
     * @throws NoSuchProviderException            If the specified provider is not available.
     * @throws InvalidAlgorithmParameterException If the given algorithm parameters are invalid.
     * @throws InvalidKeyException                If the given key is invalid.
     * @throws IllegalBlockSizeException          If the given data length is not a multiple of the block size.
     * @throws BadPaddingException                If the specified padding is incorrect.
     */
    private static byte[] encrypt(byte[] key, byte[] iv, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        return cipher.doFinal(data);
    }


    /**
     * Decrypts the given data using AES in CBC mode with PKCS7 padding.
     *
     * @param key  The decryption key as a byte array (must be 128, 192 or 256 bits).
     * @param iv   The initialization vector as a byte array (must be 128 bits).
     * @param data The byte array to decrypt.
     * @return The decrypted data as a byte array.
     * @throws IllegalBlockSizeException          If the given data length is not a multiple of the block size.
     * @throws BadPaddingException                If the specified padding is incorrect.
     * @throws NoSuchPaddingException             If the specified padding is not available.
     * @throws NoSuchAlgorithmException           If the specified algorithm is not available.
     * @throws NoSuchProviderException            If the specified provider is not available.
     * @throws InvalidAlgorithmParameterException If the given algorithm parameters are invalid.
     * @throws InvalidKeyException                If the given key is invalid.
     */
    private static byte[] decrypt(byte[] key, byte[] iv, byte[] data) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException
    {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        return cipher.doFinal(data);
    }


    /**
     * Generates a shared secret using ECDH key agreement.
     *
     * @param privateKey The private key of the party generating the secret.
     * @param publicKey  The public key of the other party.
     * @return The generated shared secret as a byte array.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     * @throws InvalidKeyException      If the given key is invalid.
     * @throws NoSuchProviderException  If the specified provider is not available.
     */
    private static byte[] generateSecret(ECPrivateKey privateKey, ECPublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException
    {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }


    /**
     * Derives encryption and decryption parameters (key and IV) from a shared secret.
     *
     * @param secret The shared secret from which to derive the parameters.
     * @return A pair containing the derived key (left) and IV (right) as byte arrays.
     */
    private static Pair<byte[], byte[]> deriveParameters(byte[] secret)
    {
        Digest digest = new SHA512Digest();

        byte[] ikm = new byte[32];
        byte[] salt = new byte[16];
        System.arraycopy(secret, 0, ikm, 0, 32);
        System.arraycopy(secret, 32, salt, 0, 16);

        HKDFParameters hkdfParameters = new HKDFParameters(ikm, salt, "SMMPv1".getBytes());
        HKDFBytesGenerator hkdfBytesGenerator = new HKDFBytesGenerator(digest);
        hkdfBytesGenerator.init(hkdfParameters);

        byte[] key = new byte[32];
        byte[] iv = new byte[16];

        hkdfBytesGenerator.generateBytes(key, 0, 32);
        hkdfBytesGenerator.generateBytes(iv, 0, 16);

        return Pair.of(key, iv);
    }


    /**
     * Signs the given data using the specified EC private key.
     *
     * @param data The data to sign as a byte array.
     * @return The generated signature as a byte array.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     * @throws NoSuchProviderException  If the specified provider is not available.
     * @throws InvalidKeyException      If the given key is invalid.
     * @throws SignatureException       If an error occurs during the signing process.
     */
    public static byte[] sign(byte[] data, ECPrivateKey key) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException
    {
        Signature signer = Signature.getInstance("SHA512withECDSA", "BC");
        signer.initSign(key);
        signer.update(data);
        return signer.sign();
    }


    /**
     * Verifies the given signature for the given data using the specified EC public key.
     *
     * @param key       The EC public key to use for verifying the signature.
     * @param data      The data for which the signature needs to be verified, as a byte array.
     * @param signature The signature to verify, as a byte array.
     * @return True if the signature is valid, false otherwise.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     * @throws NoSuchProviderException  If the specified provider is not available.
     * @throws InvalidKeyException      If the given key is invalid.
     * @throws SignatureException       If an error occurs during the verification process.
     */
    public static boolean verifySignature(ECPublicKey key, byte[] data, byte[] signature) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException
    {
        Signature verifier = Signature.getInstance("SHA512withECDSA", "BC");
        verifier.initVerify(key);
        verifier.update(data);
        return verifier.verify(signature);
    }
}


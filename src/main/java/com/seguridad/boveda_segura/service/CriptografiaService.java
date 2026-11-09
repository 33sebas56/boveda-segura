package com.seguridad.boveda_segura.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class CriptografiaService {

    private static final int TAMANO_NONCE = 12;

    private final SecretKeySpec clave;
    private final SecureRandom aleatorio = new SecureRandom();

    public CriptografiaService(
            @Value("${boveda.clave}") String claveBase64
    ) {
        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(claveBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "La clave de la bóveda no tiene Base64 válido.",
                    e
            );
        }

        if (bytes.length != 32) {
            throw new IllegalArgumentException(
                    "La clave de la bóveda debe contener 32 bytes."
            );
        }

        this.clave = new SecretKeySpec(bytes, "AES");

        Arrays.fill(bytes, (byte) 0);
    }

    public byte[] cifrar(byte[] contenido, String identificador)
            throws GeneralSecurityException {

        byte[] nonce = new byte[TAMANO_NONCE];
        aleatorio.nextBytes(nonce);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                clave,
                new GCMParameterSpec(128, nonce)
        );

        cipher.updateAAD(identificador.getBytes(StandardCharsets.UTF_8));

        byte[] cifrado = cipher.doFinal(contenido);

        byte[] resultado = new byte[nonce.length + cifrado.length];

        System.arraycopy(nonce, 0, resultado, 0, nonce.length);

        System.arraycopy(
                cifrado,
                0,
                resultado,
                nonce.length,
                cifrado.length
        );

        return resultado;
    }

    public byte[] descifrar(byte[] contenido, String identificador)
            throws GeneralSecurityException {

        if (contenido.length < TAMANO_NONCE + 16) {
            throw new GeneralSecurityException(
                    "El archivo cifrado está incompleto."
            );
        }

        byte[] nonce = Arrays.copyOfRange(contenido, 0, TAMANO_NONCE);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.DECRYPT_MODE,
                clave,
                new GCMParameterSpec(128, nonce)
        );

        cipher.updateAAD(identificador.getBytes(StandardCharsets.UTF_8));

        return cipher.doFinal(
                contenido,
                TAMANO_NONCE,
                contenido.length - TAMANO_NONCE
        );
    }

    public String sha256(byte[] contenido)
            throws GeneralSecurityException {

        byte[] hash = MessageDigest
                .getInstance("SHA-256")
                .digest(contenido);

        return HexFormat.of().formatHex(hash);
    }
}
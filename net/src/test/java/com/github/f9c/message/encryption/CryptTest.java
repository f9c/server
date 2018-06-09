package com.github.f9c.message.encryption;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CryptTest {
    @Test
    public void shouldEncryptAndDecrypt() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        keyGen.initialize(1024, random);
        KeyPair keys = keyGen.generateKeyPair();

        String msg =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer dapibus scelerisque pharetra. Morbi lobortis, mauris in facilisis lacinia, tortor mauris tempus libero, ut pellentesque ex nulla at justo. Duis ac risus ut purus imperdiet sodales ac eget diam. Etiam mauris odio, ultricies eu interdum vitae, sollicitudin a metus. Proin placerat erat sed erat congue, lacinia ultrices ipsum ullamcorper. Curabitur scelerisque, risus vitae viverra sollicitudin, massa arcu aliquam justo, eu fringilla tortor mauris vitae sem. Donec tempus accumsan massa sed volutpat. Nulla id ante eget diam facilisis aliquet ac sit amet dui. Fusce gravida ligula ex, in dictum lectus congue eu. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Duis eu est eget mauris blandit tempor. Maecenas non leo eu felis congue volutpat in sed quam. Duis auctor, lectus sed condimentum cursus, urna arcu interdum mi, et posuere neque quam sed tellus. Curabitur auctor turpis in ligula malesuada consectetur. Ut eu suscipit ipsum. Etiam id ornare arcu.\n" +
                        "Nulla consectetur ligula vitae lectus posuere sodales. Donec pulvinar lacus odio, eu eleifend justo dictum at. Vestibulum ac lorem nec elit ultricies laoreet ut ut eros. Ut tempus vehicula justo. In eget quam at mi gravida vulputate vitae vitae diam. Etiam justo neque, varius quis venenatis id, molestie eget leo. Praesent fringilla viverra rutrum. Sed cursus aliquet sem nec viverra. Nulla sollicitudin, nisl id varius convallis, purus arcu porttitor felis, vitae auctor nisi dui eget lacus. Praesent fermentum eu dolor et imperdiet. Curabitur justo odio, auctor id venenatis varius, bibendum id leo.\n" +
                        "Ut suscipit arcu vitae pulvinar luctus. Integer commodo magna vitae erat posuere accumsan. Sed pretium ut dui a tempus. Aliquam pharetra iaculis enim. Etiam nec libero ipsum. Morbi et mollis nulla, id scelerisque nisi. Morbi posuere vehicula massa, eu tristique risus lacinia et. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aenean ac maximus dui. Nullam lacinia dictum dui, eget feugiat nisl posuere sed. Donec et sodales felis.\n" +
                        "In sit amet sodales ipsum, ac varius turpis. Aliquam tempus pharetra fermentum. Fusce tempus ipsum eu suscipit pharetra. Vestibulum sit amet tempor neque. In porta auctor massa vitae sollicitudin. In accumsan lectus non mauris egestas tincidunt vitae quis ligula. Quisque vel turpis nisl. Pellentesque eu est eu ante ultrices tristique in efficitur nulla. Duis elementum dolor eros, at commodo ex semper sed.\n" +
                        "Mauris ligula massa, rutrum vitae scelerisque sit amet, vestibulum et sem. Nullam tempor ex non ex tincidunt, non molestie ante blandit. Vestibulum ante arcu, consequat sit amet aliquam vitae, vulputate cursus magna. Curabitur sed felis velit. Sed ac libero at risus consequat semper sit amet id purus. Mauris posuere orci nec suscipit lacinia. Suspendisse tempus nulla metus, ut convallis arcu ullamcorper nec. Praesent vestibulum mi vitae ligula venenatis volutpat. Nunc laoreet turpis non sapien facilisis, eu posuere lacus vestibulum. Sed condimentum non felis vel imperdiet. Quisque auctor imperdiet est vel commodo. Aliquam placerat turpis non molestie consectetur. Aliquam erat volutpat. ";

        byte[] encrypted = Crypt.encrypt(keys.getPublic(), msg.getBytes());
        byte[] decrypted = Crypt.decrypt(keys.getPrivate(), encrypted);

        assertEquals(new String(decrypted), msg);
    }

}
package util;

import java.util.UUID;

//Classe utilitária para gerar tokens seguros
public class TokenGenerator {

/**
* Gera um token seguro e único usando UUID.
* @return Uma String representando o token gerado.
*/
public static String generateToken() {
   // UUID (Universally Unique Identifier) é uma forma robusta de gerar IDs únicos.
   // Remove os hífens para ter uma string mais compacta.
   return UUID.randomUUID().toString().replace("-", "");
}
}

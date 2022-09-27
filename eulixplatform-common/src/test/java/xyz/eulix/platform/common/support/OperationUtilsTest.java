package xyz.eulix.platform.common.support;

import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.common.support.serialization.OperationUtils;

@QuarkusTest
public class OperationUtilsTest {

  final String publicKey = "-----BEGIN PUBLIC KEY-----\n"
      + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqzoDJnhzFSdeTxi4d2hs\n"
      + "siJUVymXCayRpE291Dk+5hyRCGa4XuzRNK99naqsKha7Y3qL1NWjoVmf7JGdU7t2\n"
      + "SCMkqO2xH6T4dSoqp5BMVK7/HZW+Air4/yJKVRSCbbwL5NOLb66VoVGJPl0WQb7z\n"
      + "NNfYocc7FSVWIyPPX59n+P0dJiA7nVKYVN8wZdSEMerjkDQLmiq0aB9rXgIAzDJl\n"
      + "v8tZliNyaqIUfSCn5qFiOLH927qIxux3krlpqvwtz4GhkcAJuZzLt3wqegDuOWLS\n"
      + "B7xogjr/j310W7byjDbvlUD06cJ8v2RHjLPWNza5Tfgg9a5Cdt7MbheIQugeKdhU\n"
      + "rwIDAQAB\n"
      + "-----END PUBLIC KEY-----";

  final String privateKey = "-----BEGIN PRIVATE KEY-----\n"
      + "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCrOgMmeHMVJ15P\n"
      + "GLh3aGyyIlRXKZcJrJGkTb3UOT7mHJEIZrhe7NE0r32dqqwqFrtjeovU1aOhWZ/s\n"
      + "kZ1Tu3ZIIySo7bEfpPh1KiqnkExUrv8dlb4CKvj/IkpVFIJtvAvk04tvrpWhUYk+\n"
      + "XRZBvvM019ihxzsVJVYjI89fn2f4/R0mIDudUphU3zBl1IQx6uOQNAuaKrRoH2te\n"
      + "AgDMMmW/y1mWI3JqohR9IKfmoWI4sf3buojG7HeSuWmq/C3PgaGRwAm5nMu3fCp6\n"
      + "AO45YtIHvGiCOv+PfXRbtvKMNu+VQPTpwny/ZEeMs9Y3NrlN+CD1rkJ23sxuF4hC\n"
      + "6B4p2FSvAgMBAAECggEADxyXmpWW0o8e66wq258DuNT+zJKHGft4/x89WbWTr4tl\n"
      + "4W/vZbFQ5W13kKq3dx96elUecjJItrzKOH7Be9pXnhY77gzMTRaqNuz5xsAdfHvV\n"
      + "WC8XDens/9du6ANfX3/xLMjezYSDADHKAw8EH9lSbZ9/JwgGVt9ssr0+WBohev/2\n"
      + "1UqQjjlYteEBQhYuQ4AoyYZE3OeWUvrbZYP/z8ztsyGslO9r9x1o5O5nfWNdJHqH\n"
      + "VzPQUkP77W8NtBKy28C8WRGn4PxeuWvPsdwVNf1gpw0hugD9UM1xjKIgZgNC21GG\n"
      + "Hmv/T8M3m0zKPW82mlWc4o4PgUGvbMvRq7oA/rS5wQKBgQDO/oZndyCFwde5ntMd\n"
      + "eKwlJusomFOSEKDEYr2XLjM/I9Wy6ntHQ6JT+P/FkO1FyhV41NLEuQ954uladJD4\n"
      + "Oyja3zprKTWrTb78EwIwPGp8A+G65FDEKqf9+FWfeaiMi5vb08/VSsOKNBQsNTHU\n"
      + "5zCj/PkQMcfS8ITz17dbGALAlQKBgQDTw7A7JtHPVVfg3VZdcW/FaxjNnzWH2WPQ\n"
      + "ZHSvjo+552gIGqkw4eR8yH86VTkiGrV9g8GL1yTzAGk8wCNg2IZys1GHE+7AjhjW\n"
      + "sYnv0l2pvG2WSStpsvakU6NQlndVSpoO0d7tCwAD37APh8wPh+BO7wRrTpjkUBtS\n"
      + "lHSJUrtbMwKBgEN9SWVCuh/ia+vhlQPu7HHQlqvNvrHJKiHd7+Ly9VKI5IxJV5iM\n"
      + "vMiUTxCjiDlRAl87RN1PWXRsP5l2mC3XXCLA1dNKxGzuFG8r43LGwBFfUqIU/yB2\n"
      + "p/9ukLxGdmYcvtyV2xW4IrlU8Z7YWT8Jghp1yN822+gy86ZtlyLkWUFhAoGAW88C\n"
      + "T5LT8ZyhfdbYA6kTeTamSTdWIPCJyqZSxG39N+7wdlsAsTWuNr9CEYszOurzdlgy\n"
      + "CCudVFEATxtJ+0nEeGPv260znH2+nQ3ZrIS6oMUVicttK+Uv8yxjqKOjVvIB+pzY\n"
      + "llJnjkZjcUhBmxYglHghpVgtBwcH4XSFVaftnGsCgYBMIU3YG4ii0YVio3nVCooZ\n"
      + "9Ju2oWsBtGru7LiZG/8PGUogkr84dn83R0q5T766kgQTMQPVpRRUpmj59uULddCK\n"
      + "Wk4MKeteW/Bdek0/t34gUodyR+P5QExrUnJbDQSxtGWBQUfIG7QCPdGXdtkx+les\n"
      + "+1G+x47evyhocksSMb+Xvg==\n"
      + "-----END PRIVATE KEY-----";

  @Inject
  OperationUtils operationUtils;

  @Test
  public void EncryptionAndDecryption(){
    var enKey = operationUtils.encryptUsingPublicKey("test", publicKey);

    var deKey = operationUtils.decryptUsingPrivateKey(enKey, privateKey);

    Assertions.assertEquals("test", deKey);
  }

}

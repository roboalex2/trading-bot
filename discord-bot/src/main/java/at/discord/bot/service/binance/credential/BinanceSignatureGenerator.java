package at.discord.bot.service.binance.credential;

import com.binance.connector.client.utils.ParameterChecker;
import com.binance.connector.client.utils.signaturegenerator.SignatureGenerator;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

public class BinanceSignatureGenerator implements SignatureGenerator {

    private final Ed25519PrivateKeyParameters privateKey;
    private final int offset = 0;

    public BinanceSignatureGenerator(byte[] secretApiKey) throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        PemReader pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(secretApiKey), StandardCharsets.UTF_8));
        PemObject pemObject = pemReader.readPemObject();
        byte[] privateKeyBytes = pemObject.getContent();
        this.privateKey = (Ed25519PrivateKeyParameters) PrivateKeyFactory.createKey(privateKeyBytes);
        pemReader.close();
    }

    @Override
    public String getSignature(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, this.privateKey);
        signer.update(dataBytes, offset, dataBytes.length);
        byte[] signatureBytes = signer.generateSignature();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }
}

package networkio;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Random;

import javax.crypto.Mac;

import org.bouncycastle.util.encoders.Base64;

import message.response.MessageIntegrityErrorResponse;

public class HMACChannel extends Channel {

	private Channel parent;
	private Mac hMac;

	private boolean correctChecksum;

	public HMACChannel(Channel parent, Mac hmac) {
		this.parent = parent;
		this.hMac = hmac;
	}

	@Override
	public void write(byte[] data) throws IOException {

		byte[] hash = hMac.doFinal(Serializer.decode(data).toString().getBytes());

		// simulate error
		if (0 == new Random().nextInt(9)) {
			data[0] = 0;
		}

		HMACWrapped w = new HMACWrapped(data, hash);
		parent.write(Serializer.encode(w));
	}

	@Override
	public byte[] readBytes() throws IOException {

		byte[] data = parent.readBytes();

		Object response;
		try {
			HMACWrapped w = (HMACWrapped) Serializer.decode(data);
			correctChecksum = w.isChecksumCorrect(hMac);
			response = w.getObject();

			if (!correctChecksum)
				response = new MessageIntegrityErrorResponse();
		} catch (StreamCorruptedException e) {
			response = new MessageIntegrityErrorResponse();
		}

		return Serializer.encode(response);

	}

	public boolean isChecksumCorrect() {
		return correctChecksum;
	}

	@Override
	public void close() throws IOException {
		parent.close();
	}

}

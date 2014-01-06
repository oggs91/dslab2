package networkio;

import java.io.IOException;

public abstract class Channel {
	public void write(Object o) throws IOException {
		write(Serializer.encode(o));
	}

	public abstract void write(byte[] data) throws IOException;

	public Object read() throws IOException {
		return Serializer.decode(readBytes());
	}

	public abstract byte[] readBytes() throws IOException;

	public abstract void close();
}

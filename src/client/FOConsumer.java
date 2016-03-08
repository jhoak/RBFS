package client;

import java.io.IOException;
import java.net.SocketTimeoutException;

interface FOConsumer {
	void accept(String s) throws IOException, SocketTimeoutException;
}
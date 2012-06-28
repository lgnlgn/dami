package org.dami.common.io;

import java.io.IOException;

public abstract class DataConverter<FROM, TO> {
	DataReader<FROM> reader;
	DataWriter<TO> writer;
	
	public abstract void convert() throws IOException;
}

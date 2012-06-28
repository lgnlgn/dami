package org.dami.common.io;

import java.io.IOException;

public interface DataWriter<T> {
	public Class<? extends DataReader<T>> getDeserClass(); 
	
	public void open() throws IOException;
	public void close() throws IOException;
	
	public void write(T sample) throws IOException;
	
	
}

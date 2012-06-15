package org.lttng.studio.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Test;

public class CtfImportTest {

	@Test
	public void testDummyImport() throws CTFReaderException, IOException {
		CTFReaderException ex = new CTFReaderException();
		assertTrue(ex.toString().contains("CTFReaderException"));
	}
	
}

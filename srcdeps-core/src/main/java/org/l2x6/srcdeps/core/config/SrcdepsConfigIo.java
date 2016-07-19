package org.l2x6.srcdeps.core.config;

import java.io.Reader;
import java.io.Writer;

public interface SrcdepsConfigIo {
    SrcdepsConfig read(Reader url);
    void write(SrcdepsConfig config, Writer writer);
}

package org.l2x6.srcdeps.config.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.inject.Named;

import org.l2x6.srcdeps.core.config.SrcdepsConfig;
import org.l2x6.srcdeps.core.config.SrcdepsConfigIo;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;

@Named
public class YamlConfigIo implements SrcdepsConfigIo {

    private static class SrcdepsConfigConstructor extends Constructor {



        @Override
        protected Construct getConstructor(Node node) {
            System.out.println("Constructor for node "+ node);
            Construct result = super.getConstructor(node);
            System.out.println("    "+ result);
            return result;
        }

    }

    @Override
    public SrcdepsConfig read(Reader in) {
        Yaml yaml = new Yaml(new SrcdepsConfigConstructor());
        yaml.load(in);
        return null;
    }

    @Override
    public void write(SrcdepsConfig config, Writer writer) {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) throws IOException {
        try (Reader in = new InputStreamReader(new FileInputStream(new File("/home/ppalaga/git/srcdeps-maven-plugin-quickstart/.mvn/srcdeps.yaml")), "utf-8")) {
            new YamlConfigIo().read(in);
        }
    }


}
